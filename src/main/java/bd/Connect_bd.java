package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Connect_bd {

    public static final String DB_DRIVER="org.firebirdsql.jdbc.FBDriver";
    public static final String DB_URL="jdbc:firebirdsql:lao:F:\\test\\02\\LiWest.fdb";

    public static Connection Connect_bd() throws ClassNotFoundException {

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
             return DriverManager.getConnection(conString, paramConnection);
            }
        catch (SQLException e1) {
            e1.printStackTrace();
            return null;
            }
    }

                          }

