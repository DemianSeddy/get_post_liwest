package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class Connect_bd {

    private static final String DB_DRIVER="org.firebirdsql.jdbc.FBDriver";
    private static final String DB_URL="jdbc:firebirdsql:localhost:E:\\runliwest\\base\\30062019\\LIWEST.FDB";
    private static Connection connection = null;
    private static Statement statement = null;


    public static boolean Connect_bd() throws ClassNotFoundException {

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("connectToDB: Firebird JCA-JDBC драйвер не найден");
        }
        try {
             String conString = DB_URL;
             Properties paramConnection = new Properties();
             paramConnection.setProperty("user", "SYSDBA");
             paramConnection.setProperty("password", "ktybdsq,fnjy");
             paramConnection.setProperty("encoding", "WIN1251");
             System.out.println(DriverManager.getConnection(conString, paramConnection).getCatalog().toString());
             connection =  DriverManager.getConnection(conString, paramConnection);
             return true;
            }
        catch (SQLException e1) {
            e1.printStackTrace();
            System.out.println("Не подключены к базе"+e1.toString());
            return false;
            }
    }

    public  Connection getConnection() {
        return connection;
    }

    public static Statement getStatementForFBConnect(){
        Statement statement;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("Не создан staitment : "+e.getMessage());
            return null;
        }
        return statement;
    }




   }

