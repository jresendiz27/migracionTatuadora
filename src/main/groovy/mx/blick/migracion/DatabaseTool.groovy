package mx.blick.migracion

import groovy.sql.Sql
import groovy.util.logging.Log4j

import java.sql.SQLException

@Singleton(strict = false)
@Log4j
public class DatabaseTool {

    private String url = "jdbc:mysql://localhost:3306/test";
    private String user = "root";
    private String password = "n0m3l0s3";
    private String driver = "com.mysql.jdbc.Driver";
    String defaultPassword = '$2a$10$iqLb4ON3lZXuG818y9u5Nez1f4LYbAgApzwOCiIZEOO9nWAL/2nFO' //latatuadora-2017
    private Sql sql;

    private DatabaseTool() throws SQLException, ClassNotFoundException {
        sql = Sql.newInstance(url, user, password, driver);
    }

    void insertUser(Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.User(name, lastname, email, password, telephone, userType, createdAt, updatedAt) 
                    values 
                        (:name, :lastname, :email, '$defaultPassword', :telephone, :userType, NOW(), NOW())"""
        sql.execute(insertUserSQL, values)
    }

    void insertStudio(Integer userId, Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.Studio(name, certCofepris, addressId, publication, titleImgUrl, logoUrl, profileImgUrl, about, userId, status, createdAt, updatedAt)
                    values 
                        (:name, :certCofepris, :addressId, TRUE, :titleImgUrl, :logoUrl, :profileImgUrl, :about, :userId, ${
            Constants.STUDIO_STATUS.publicate
        }, NOW(), NOW())"""
        sql.execute(insertUserSQL, values)
    }

    void insertFreelance(Integer userId, Map values) {
        String insertUserSQL = """
                    INSERT INTO 
                        latatuadora_core.Freelancer(user, about, published, profileImgUrl, canGoHome, name, rank, createdAt, updatedAt)
                    values 
                        ($userId, :about, TRUE, :profileImgUrl, :canGoHome, :name, 5, NOW(), NOW())"""
        sql.execute(insertUserSQL, values)
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
                    email like ('$email')
            """
        return sql.firstRow(findUserByIdOrEmailQuery) as Map;
    }

}
