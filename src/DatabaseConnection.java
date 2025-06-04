import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dental_clinic";
    private static final String USER = "root";
    private static final String PASSWORD = "Alexandru2004";

    private static Connection connection = null;

    private DatabaseConnection() {} // Private constructor to prevent instantiation

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Create the connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully.");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Connection failed.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error while closing the connection.");
                e.printStackTrace();
            }
        }
    }
}