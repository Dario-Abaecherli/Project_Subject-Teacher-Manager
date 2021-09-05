package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Buildt like Singleton, only one connection can exist
public class SQLConnection {
    private static Connection sqlConnection = null;

    private SQLConnection()
    {
    }

    //Request of connection
    public static Connection getConnection(){
        if(sqlConnection == null) {
            try
            {
                sqlConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/lmsystem","root","");
            }
            catch(SQLException exc)
            {
                System.out.println("SQLException: " + exc.getMessage());
            }
        }
        return sqlConnection;
    }

    //Reset (Delete) Connection
    public static void deleteConnection(){
        sqlConnection = null;
    }


}
