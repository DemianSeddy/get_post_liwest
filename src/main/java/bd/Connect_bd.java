package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;



public class Connect_bd {

    private static final String DB_DRIVER = "org.firebirdsql.jdbc.FBDriver";
    private static final String DB_URL = "jdbc:firebirdsql:localhost:E:\\runliwest\\base\\work\\LIWEST.FDB";
    private static Connection connection =  null;
    private static boolean flag = false;
    private static final Logger LOGGER = Logger.getLogger(Connect_bd.class.getName());

    public Connect_bd() throws ClassNotFoundException {

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("connectToDB: Firebird JCA-JDBC драйвер не найден");

        }

        try {
            String conString = DB_URL;
            Properties paramConnection = new Properties();
            paramConnection.setProperty("user", "SYSDBA");
            paramConnection.setProperty("password", "masterkey");
            paramConnection.setProperty("encoding", "WIN1251");
            connection = DriverManager.getConnection(conString, paramConnection);
            flag = true;
            LOGGER.info("Подключены к базе" + conString.toString());
        }
        catch (SQLException e1) {
            e1.printStackTrace();
             LOGGER.warning("Не подключены к базе" + e1.toString());
        }
    }

    public Connection getConnection() {
        return connection;
    }


    public Statement getStatementForFBConnect(){
        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            LOGGER.warning("AccessToFB: "+e.getMessage());
            return null;
        }
        return statement;
    }


    public boolean closeAccess() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}

