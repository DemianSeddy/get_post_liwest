package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Connect_bd {
    public static String DB_DRIVER="org.firebirdsql.jdbc.FBDriver";
    public static String DB_URL="jdbc:firebirdsql:lao:F:\\test\\02\\LiWest.fdb";
    public static Connection Conn() throws ClassNotFoundException {
        try {
             Class.forName(DB_DRIVER);
             String conString = DB_URL;
             Properties paramConnection = new Properties();
             paramConnection.setProperty("user", "SYSDBA");
             paramConnection.setProperty("password", "ktybdsq,fnjy");
             paramConnection.setProperty("encoding", "WIN1251");
             Connection con = DriverManager.getConnection(conString, paramConnection);
             return con;
            }
        catch (SQLException e1) {
            e1.printStackTrace();
                                }
        return null;
    }
                          }

