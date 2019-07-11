import sun.security.x509.SerialNumber;

import java.sql.*;
import java.util.Enumeration;


public class MySQLConnection {

    String serialNmber = "";
    public void listDrivers(){
        Enumeration driverList = DriverManager.getDrivers();
        System.out.println("\nList of drivers:");
        while (driverList.hasMoreElements()) {
            Driver driverClass = (Driver) driverList.nextElement();
            System.out.println("   "+driverClass.getClass().getName());
        }
    }

    MySQLConnection(String tmpSerialNumber) throws SQLException {

        this.serialNmber = tmpSerialNumber;
        String user = "msisdn";
        String password = "a05msisdn";
        DataBaseQuery dbQuery = new DataBaseQuery(tmpSerialNumber);

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://172.23.6.95";
            Connection con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed()) System.out.println("Connected to DataBase");
            else System.out.println("No Connection");

            //Creating statement
            Statement stmt=con.createStatement();

            //Executing SQL statement
            ResultSet rs=stmt.executeQuery(dbQuery.getSelectTopQuery());
            //ResultSet rs=stmt.executeQuery("select * from emp");

            //Writing on console statement
            while(rs.next())
                System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}


