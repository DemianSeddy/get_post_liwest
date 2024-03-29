package bd;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;



public class Connect_bd {

    private static Connection connection =  null;
    private static boolean flag = false;
    private static final Logger LOGGER = Logger.getLogger(Connect_bd.class.getName());

    public static Properties paramConnection = null;


    public Connect_bd(String path) throws ClassNotFoundException, IOException {

        //String conString = DB_URL;

        InputStream inStream = new FileInputStream(path);

        paramConnection = new Properties();

        paramConnection.loadFromXML(inStream);

        for (String key : paramConnection.stringPropertyNames())
        {
            System.out.print(key);
            System.out.print("=");
            System.out.println(paramConnection.get(key));
        }

        try {
            Class.forName(paramConnection.get("DB_DRIVER").toString());
        } catch (ClassNotFoundException e) {
            LOGGER.warning("connectToDB: Firebird JCA-JDBC драйвер не найден");

        }

        try {
            connection = DriverManager.getConnection(paramConnection.get("DB_URL").toString(), paramConnection);
            flag = true;
            LOGGER.info("Подключены к базе" + paramConnection.get("DB_URL").toString().toString());
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

