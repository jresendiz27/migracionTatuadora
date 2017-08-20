package mx.blick.migracion

import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.SQLException

@Singleton(strict = false)
@Log4j
class DatabaseTool {

    private String url = "jdbc:mysql://localhost:3306/test";
    private String user = "root";
    private String password = "n0m3l0s3";
    private String driver = "com.mysql.jdbc.Driver";
    String defaultPassword = '$2a$10$iqLb4ON3lZXuG818y9u5Nez1f4LYbAgApzwOCiIZEOO9nWAL/2nFO' //latatuadora-2017
    private Sql sql;

    private DatabaseTool() throws SQLException, ClassNotFoundException {
        sql = Sql.newInstance(url, user, password, driver);
    }

    void truncateUserRelatedTables() {
        this.truncateUser()
        this.truncateStudio()
        this.truncateFreelance()
    }

    void truncateUser() {
        String truncateUserQuery = "TRUNCATE TABLE latatuadora_core.User"
        log.error(truncateUserQuery)
        sql.execute(truncateUserQuery);
    }

    void truncateStudio() {
        String truncateStudioQuery = "TRUNCATE TABLE latatuadora_core.Studio"
        log.error(truncateStudioQuery);
        sql.execute(truncateStudioQuery);
    }

    void truncateFreelance() {
        String truncateFreelance = "TRUNCATE TABLE latatuadora_core.Freelance"
        log.error(truncateFreelance)
        sql.execute(truncateFreelance);
    }

    void truncateStudioStyles() {
        String truncateStudioStyle = "TRUNCATE TABLE latatuadora_core.StudioStyle"
        log.error(truncateStudioStyle);
        sql.execute(truncateStudioStyle);
    }

    void insertUser(Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.User(name, lastname, email, password, telephone, userType, createdAt, updatedAt) 
                    values 
                        (:name, :lastname, :email, '$defaultPassword', :telephone, :userType, NOW(), NOW())"""
        this.executeSimpleQuery(insertUserSQL, values)
    }

    void insertStudio(Integer userId, Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.Studio(name, certCofepris, addressId, publication, titleImgUrl, logoUrl, profileImgUrl, about, userId, status, createdAt, updatedAt)
                    values 
                        (:name, :certCofepris, :addressId, TRUE, :titleImgUrl, :logoUrl, :profileImgUrl, :about, :userId, ${
            Constants.STUDIO_STATUS.publicate
        }, NOW(), NOW())"""
        this.executeSimpleQuery(insertUserSQL, values)
    }

    void insertFreelance(Integer userId, Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.Freelancer(user, about, published, profileImgUrl, canGoHome, name, rank, createdAt, updatedAt)
                    values 
                        ($userId, :about, TRUE, :profileImgUrl, :canGoHome, :name, 5, NOW(), NOW())"""
        this.executeSimpleQuery(insertUserSQL, values)
    }

    Map findByUserIdOrEmail(Integer id, String email) {
        String findUserByIdOrEmailQuery = """
                SELECT 
                    id, name, lastname, email
                FROM
                    latatuadora_core.USER
                WHERE
                    id = $id
                OR
                    TRIM(LOWER(email)) like ('%${email.toLowerCase().trim()}%')
            """
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
                    TRIM(LOWER(email)) like ('%${email.toLowerCase().trim()})%')
            """
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

    void executeSimpleQuery(String query, Map params) {
        this.logQuery(query, params)
        sql.execute(query, params)
    }

    Map queryFirstRow(String query, Map params = null) {
        this.logQuery(query, params)
        return sql.firstRow(query) as Map;
    }

    void logQuery(String query, Map params) {
        String sanitizedQuery = ""
        String auxQuery = "${query}"

        for (key in params.keySet()) {
            String namedParam = ":${key}"
            if (auxQuery.indexOf(namedParam)) {
                auxQuery = auxQuery.replace(namedParam, params.get(key) as String)
            }
        }
        log.error(sanitizedQuery)
    }
}
