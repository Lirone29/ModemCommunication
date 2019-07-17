package App;

import java.sql.*;
import java.util.Enumeration;


public class MySQLConnection {

    String sqlMenu = "Menu for SQL Query Execution: \n" +
            "1. Get number for card \n" +
            "2. Get PIN1 \n" +
            "3. Get PUK1 \n" +
            "4. Get apn \n" +
            "5. Get IP Address \n" +
            "0. Exit \n";

    String serialNmber = "";

    Statement stmt;
    Connection con;
    DataBaseQuery dbQuery;

    public void listDrivers() {
        Enumeration driverList = DriverManager.getDrivers();
        System.out.println("\nList of drivers:");
        while (driverList.hasMoreElements()) {
            Driver driverClass = (Driver) driverList.nextElement();
            System.out.println("   " + driverClass.getClass().getName());
        }
    }


    public void closeConnection() throws SQLException {
        con.close();
    }

    public String getIP() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.IPAddrQuery);
        return rs.getString(0);
        //while (rs.next())
        //    System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
    }
    public MySQLConnection(String tmpSerialNumber) throws SQLException {

        this.serialNmber = tmpSerialNumber;
        String user = "msisdn";
        String password = "a05msisdn";
        dbQuery = new DataBaseQuery(tmpSerialNumber);

        try {

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://172.23.6.95";
            con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed()) System.out.println("Connected to DataBase");
            else System.out.println("No Connection");

            //Creating statement
            //Statement stmt = con.createStatement();

            //Executing SQL statement
            //ResultSet rs = stmt.executeQuery(dbQuery.getSelectTopQuery());

            //Writing on console statement
           // while (rs.next())
            //    System.out.println(rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
            //con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}


