package App;

import javax.swing.*;
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
    String serwerIP = "172.23.6.95";
    String user = "msisdn";
    String password = "a05msisdn";

    static Statement stmt;
    static Connection con;
    static DataBaseQuery dbQuery;
    boolean connectionStatus = false;

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

    public String getPuk1() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getPUKQuery());
        String result = "";
        while (rs.next()) {
            result = rs.getString("puk1");
        }
        return result;
    }

    public String getPuk2() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getPUK2Query());
        String result = "";
        while (rs.next()) {
            result = rs.getString("puk2");
        }
        return result;
    }

    public String getPin2() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getPIN2Query());
        String result = "";
        while (rs.next()) {
            result = rs.getString("pin2");
        }
        return result;
    }

    public String getPin1() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getPINQuery());
        String result = "";
        while (rs.next()) {
            result = rs.getString("pin1");
        }
        return result;
    }

    public String getAPN() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getApnQuery());
        String result = "";
        while (rs.next()) {
            result = rs.getString("apn");
        }
        return result;
    }

    public static String getIP() throws SQLException {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(dbQuery.getIPAddrQuery());
        String result = "";
        while (rs.next()) {
            result = rs.getString("ip_addr");
        }
        return result;
    }

    public void configureConnection() {
        serwerIP = JOptionPane.showInputDialog("Serwer IP: ", serwerIP);
        user = JOptionPane.showInputDialog("User: ", user);
        password = JOptionPane.showInputDialog("Password: ", password);
    }

    public boolean isConnectionStatus() {
        return connectionStatus;
    }

    public void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String url = "jdbc:sqlserver://" + serwerIP;
            con = DriverManager.getConnection(url, user, password);

            if (!con.isClosed()) connectionStatus = true;
            else connectionStatus = false;

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public MySQLConnection(String tmpSerialNumber) throws SQLException {

        //this.serialNmber = tmpSerialNumber;
        //dbQuery = new DataBaseQuery(tmpSerialNumber);

        //-----------FOR TESTS-----------------------
        this.serialNmber = "9508828297039";
        dbQuery = new DataBaseQuery("9508828297039");

    }
}


