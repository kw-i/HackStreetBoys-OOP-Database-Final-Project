package roadwatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


 //DatabaseConnection – Singleton pattern.
 //Manages a single shared MySQL connection to localhost/roadwatch_db.

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/roadwatch_db";
    private static final String DB_USER  = "root";
    private static final String DB_PASS  = "";           // change if your MySQL has a password

    private static Connection connection;

    // Private constructor – prevent instantiation
    private DatabaseConnection() {}

    //Returns the shared Connection, (re)creating it if closed or null.
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, DB_USER, DB_PASS);
        }
        return connection;
    }

    // Closes the shared connection gracefully. 
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
