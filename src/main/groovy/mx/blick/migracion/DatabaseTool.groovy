package mx.blick.migracion

import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.SQLException

@Singleton(strict = false)
@Log4j
class DatabaseTool {

    private String url = "jdbc:mysql://35.161.232.194:3306/latatuadora_core";
    private String user = "latatuadora";
    private String password = "latatuadora";
    private String driver = "com.mysql.jdbc.Driver";
    String defaultPassword = '$2a$10$iqLb4ON3lZXuG818y9u5Nez1f4LYbAgApzwOCiIZEOO9nWAL/2nFO' //latatuadora-2017
    private Sql sql;

    private DatabaseTool() throws SQLException, ClassNotFoundException {
        sql = Sql.newInstance(url, user, password, driver);
    }

    void truncateUserRelatedTables() {
        this.truncateAddress()
        this.truncateUser()
        this.truncateStudio()
        this.truncateFreelance()
    }

    void truncateAddress() {
        String truncateAddressQuery = "TRUNCATE TABLE latatuadora_core.Address"
        log.debug(truncateAddressQuery)
        sql.execute(truncateAddressQuery);
    }

    void truncateUser() {
        String truncateUserQuery = "TRUNCATE TABLE latatuadora_core.User"
        log.debug(truncateUserQuery)
        sql.execute(truncateUserQuery);
    }

    void truncateStudio() {
        String truncateStudioQuery = "TRUNCATE TABLE latatuadora_core.Studio"
        log.debug(truncateStudioQuery);
        sql.execute(truncateStudioQuery);
    }

    void truncateFreelance() {
        String truncateFreelance = "TRUNCATE TABLE latatuadora_core.Freelance"
        log.debug(truncateFreelance)
        sql.execute(truncateFreelance);
    }

    void truncateStudioStyles() {
        String truncateStudioStyle = "TRUNCATE TABLE latatuadora_core.StudioStyle"
        log.debug(truncateStudioStyle);
        sql.execute(truncateStudioStyle);
    }

    List insertUserClient(Map values) {
        String insertUserSQL = """
            INSERT INTO 
                latatuadora_core.User(name, lastname, email, password, telephone, userType, addressId, createdAt, updatedAt) 
            values 
                (:name, :lastname, :email, '$defaultPassword', :telephone, ${Constants.USER_TYPE.user}, :addressId, NOW(), NOW())"""
        this.executeInsert(insertUserSQL, values)
    }

    List insertUserStudio(Map values) {
        String insertUserSQL = """
            INSERT INTO 
                latatuadora_core.User(name, lastname, email, password, telephone, userType, addressId,createdAt, updatedAt) 
            values a
                (:name, :lastname, :email, '$defaultPassword', :telephone, ${Constants.USER_TYPE.studio}, :addressId, NOW(), NOW())"""
        this.executeInsert(insertUserSQL, values)
    }

    List insertUserFreelance(Map values) {
        String insertUserSQL = """
            INSERT INTO 
                latatuadora_core.User(name, lastname, email, password, telephone, userType, addressId, createdAt, updatedAt) 
            values 
                (:name, :lastname, :email, '$defaultPassword', :telephone, ${Constants.USER_TYPE.freelance}, :addressId, NOW(), NOW())"""
        this.executeInsert(insertUserSQL, values)
    }

    List createStudio(Integer userId, Map values) {
        String insertUserSQL = """
            INSERT INTO 
                latatuadora_core.Studio(name, certCofepris, addressId, publication, titleImgUrl, logoUrl, profileImgUrl, about, userId, status, createdAt, updatedAt)
            values 
                (:name, :certCofepris, :addressId, TRUE, :titleImgUrl, :logoUrl, :profileImgUrl, :about, :userId, ${
            Constants.STUDIO_STATUS.publicate
        }, NOW(), NOW())"""
        this.executeInsert(insertUserSQL, values)
    }

    List createFreelance(Integer userId, Map values) {
        String insertUserSQL = """
            INSERT INTO 
                latatuadora_core.Freelancer(user, about, published, profileImgUrl, canGoHome, name, rank, createdAt, updatedAt)
            values 
                ($userId, :about, TRUE, :profileImgUrl, :canGoHome, :name, 5, NOW(), NOW())"""
        this.executeInsert(insertUserSQL, values)
    }

    Map findByUserIdOrEmail(Integer id, String email) {
        String findUserByIdOrEmailQuery = """
            SELECT 
                id, email
            FROM
                latatuadora_core.USER
            WHERE
                id = $id
            OR
                TRIM(LOWER(email)) like ('%${email.toLowerCase().trim()}%')"""
        this.queryFirstRow(findUserByIdOrEmailQuery)
    }

    Map findStudioByIdOrEmail(Integer id, String email) {
        String findStudioByIdOrEmailQuery = """
            SELECT 
                Studio.id, Studio.name, User.email
            FROM
                latatuadora_core.Studio 
            INNER JOIN
                latatuadora_core.User in Studio.userId = User.id
            WHERE
                User.id = $id
            OR
                TRIM(LOWER(email)) like ('%${email.toLowerCase().trim()})%')"""
        return this.queryFirstRow(findStudioByIdOrEmailQuery)
    }

    Map findStyleByName(String styleName) {
        String styleByName = """
            SELECT 
                id, name 
            FROM 
                latatuadora_core.Style 
            WHERE 
                TRIM(LOWER(name)) like  (%${styleName.toLowerCase().trim()}%)"""
        return this.queryFirstRow(styleByName)
    }

    Long findOrCreateTown(String townName) {
        String findTownIfExists = """
            SELECT
                id, name
            FROM
                latatuadora_core.Town
            WHERE                
                lower(name) like ('%${townName.toLowerCase()}%')
            """
        String insertQuery = """
            INSERT INTO
                latatuadora_core.Town(name, createdAt, updatedAt)
            VALUES
                ('${StringUtil.capitalizeString(townName)}', NOW(), NOW())"""
        Map result = this.queryFirstRow(findTownIfExists)
        if (result && !result?.isEmpty()) {
            return result.id as Long
        } else {
            List insertedIds = this.executeInsert(insertQuery)
            return (insertedIds.first()[0]) as Long
        }
    }

    Long findSuburb(Long stateId, String suburbName) {
        String normalizedSuburbName = ""
        Constants.NORMALIZED_SUBURB_MAP.each { key, value ->
            if(suburbName.toLowerCase().trim() =~ key) {
                normalizedSuburbName = value
            }
        }

        String findSuburbLikeName = """
            SELECT
                id, name
            FROM
                latatuadora_core.Suburb
            WHERE
                stateId = $stateId
            AND
                lower(name) like ('%${normalizedSuburbName.toLowerCase()}%')
            """
        return this.queryFirstRow(findSuburbLikeName).id
    }

    Long findState(String state) {
        Long stateId = 0
        Constants.MEXICO_STATES.each { String key, Long value ->
            if (key.toLowerCase() =~ "${state.trim().toLowerCase()}") {
                stateId = value;
            }
        }
        return stateId
    }

    List createAddress(Map addressValues) {
        String stateName = StringUtil.sanitizeString(addressValues.state ?: "")
        String suburbName = addressValues.suburb ?: ""
        String townName = StringUtil.sanitizeString(addressValues.town ?: "")

        Long stateId = this.findState(stateName ?: "Ciudad de México")
        Long suburbId = this.findSuburb(stateId, suburbName ?: "Cuauhtémoc")
        Long townId = this.findOrCreateTown(townName) ?: null

        String createAddress = """
                INSERT INTO 
                    latatuadora_core.Address(street, numInt, numExt, lat, `long`, stateId, suburbId, townId, zc, createdAt, updatedAt)
                VALUES
                    (:street, :numInt, :numExt, :lat, :long, $stateId, $suburbId, $townId, :zipCode, NOW(), NOW())"""

        this.executeInsert(createAddress, addressValues)
    }

    List executeInsert(String query, Map params = null) {
        this.logQuery(query, params)
        if (!params) {
            return sql.executeInsert(query)
        } else {
            return sql.executeInsert(params, query)
        }
    }

    Map queryFirstRow(String query, Map params = null) {
        this.logQuery(query, params)
        try {
            return sql.firstRow(query) as Map;
        } catch (Exception e) {
            log.error(e)
            return [:]
        }
    }

    void logQuery(String query, Map params) {
        String auxQuery = "${query}"
        if (params) {
            for (key in params.keySet()) {
                String namedParam = ":${key}"
                if (auxQuery.indexOf(namedParam)) {
                    auxQuery = auxQuery.replace(namedParam, params.get(key) as String)
                }
            }
        }

        log.debug(auxQuery)
    }
}
