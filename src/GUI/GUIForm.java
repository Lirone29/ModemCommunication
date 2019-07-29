package GUI;

import App.ModemComm;
import App.MySQLConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

public class GUIForm extends JFrame implements ActionListener, Serializable {

    private JPanel panelGUI;
    private JComboBox serialPortComboBox;
    private JButton connectButton;
    private JButton getSerialNumberButton;
    private JButton displaySIMNumberButton;
    private JButton clearNumberButton;
    private JButton writeNumberButton;
    private JButton checkSIMSecurityButton;
    private JButton compareIPAddressButton;
    private JButton modemInfoButton;
    private JTextField commandTextField;
    private JButton executeButton;
    private JTextArea modemTextArea;
    private JButton clearButton;
    private JLabel SQLLabel;
    private JLabel modemStatusLabel;
    private JLabel serialPortsLabel;
    private JLabel modemResponseLabel;
    private JLabel modemLabel;
    private JLabel lteLabel;
    private JButton DBConnectButton;
    private JButton disconnectButton;
    private JLabel CSQLabel;
    private JLabel IpLabel;
    private JLabel ModemDataLabel;

    boolean connection = false;
    ModemComm modemComm;
    App.MySQLConnection con;
    String portName = "";
    String dataBaseIp = "";
    ArrayList<String> serialPortList;
    MyThread thread;

    boolean threatStatus = false;
    int timeout = 2000;

    public class MyThread extends Thread {

        public void run() {
            threatStatus = true;
            while (threatStatus) {
                CSQLabel.setText("CSQ: " + modemComm.getCSQ());
                IpLabel.setText("IP: " + modemComm.checkIPAddr());

                try {
                    this.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setSecurity() {
        if (con.isConnectionStatus() == true && modemComm.getConnection() == true) {
            try {
                modemComm.setAPN(con.getAPN());
                modemComm.setPin1Number(con.getPin1());
                modemComm.setPin2Number(con.getPin2());
                modemComm.setPuk1Number(con.getPuk1());
                modemComm.setPuk2Number(con.getPuk2());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public GUIForm(String title) {
        super(title);
        setResizable(true);
        setContentPane(panelGUI);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        threatStatus = false;
        modemComm = new ModemComm();
        serialPortList = modemComm.getAllSerialPorts();
        thread = new MyThread();

        for (int i = 0; i < serialPortList.size(); i++) {
            serialPortComboBox.addItem(serialPortList.get(i));
        }

        modemTextArea.setText("");
        modemTextArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        commandTextField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        commandTextField.setText("");

        clearButton.addActionListener(this);
        connectButton.addActionListener(this);
        serialPortComboBox.addActionListener(this);
        checkSIMSecurityButton.addActionListener(this);
        clearNumberButton.addActionListener(this);
        compareIPAddressButton.addActionListener(this);
        displaySIMNumberButton.addActionListener(this);
        executeButton.addActionListener(this);
        getSerialNumberButton.addActionListener(this);
        writeNumberButton.addActionListener(this);
        modemInfoButton.addActionListener(this);
        DBConnectButton.addActionListener(this);
        disconnectButton.addActionListener(this);

        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object a = e.getSource();

        if (a == executeButton) {
            if (!this.commandTextField.getText().equals("")) {
                modemComm.writeCommand(this.commandTextField.getText());
                this.modemTextArea.setText("" + modemComm.getModemResponse());
            }
        } else if (a == clearButton) {
            this.modemTextArea.setText("");
            this.commandTextField.setText("");

        } else if (a == connectButton) {
            this.modemTextArea.setText("");
            connection = modemComm.getConnection();

            if (connection == false) {
                portName = serialPortComboBox.getSelectedItem().toString();
                modemComm.setPortName(portName);
                connection = modemComm.connect();
                if (connection == true) {
                    this.modemLabel.setText("Modem: CONNECTED, " + portName);
                    modemComm.turnSpamOff();
                    this.lteLabel.setText(modemComm.checkLTE());
                    modemComm.getModemInfo();
                } else {
                    this.modemLabel.setText("Modem Status: DISCONNECTED");
                    this.lteLabel.setText("Lte Status: NULL");
                }

            } else {
                JOptionPane.showMessageDialog(null, "App is already connected to serial port. \n Disconnect first to change port\n", "Connection Warning!", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (a == compareIPAddressButton) {
            if (con.isConnectionStatus() == false) {
                this.modemTextArea.setText("NOT CONNECTED TO DATABASE!");
                return;
            }
            try {
                dataBaseIp = con.getIP();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            String modemIP = modemComm.getIPAddress();
            if (dataBaseIp.equals(modemIP))
                this.modemTextArea.setText("The same IP address: \n" + modemIP);
            else
                this.modemTextArea.setText("IP Addres: \nDataBase IP: " + dataBaseIp + "\nModem IP: " + modemIP);

        } else if (a == serialPortComboBox) {
            portName = serialPortComboBox.getSelectedItem().toString();

        } else if (a == checkSIMSecurityButton) {
            //Reads how secured is SIM
            setSecurity();
            modemComm.readSecurity();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            this.modemTextArea.setText(modemTextArea.getText() + "\n------------------------------------------" + modemComm.checkSecurity(modemTextArea.getText()));

        } else if (a == getSerialNumberButton) {
            modemComm.readSimCardSerialNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            modemComm.setSerialNumberFromInput();

        } else if (a == modemInfoButton) {
            modemComm.getModemInfo();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));

        } else if (a == clearNumberButton) {
            modemComm.clearNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));

        } else if (a == writeNumberButton) {
            modemComm.enablePhonebookMemoryStore();
            modemComm.writePhoneNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse());

        } else if (a == displaySIMNumberButton) {
            modemComm.checkNumbersOnSIM();
            this.modemTextArea.setText("" + modemComm.getModemResponse());
        } else if (a == DBConnectButton) {

            if (modemComm.getSerialNumber() == null) {
                modemComm.readSimCardSerialNumber();
                modemComm.getModemResponse();
                modemComm.setSerialNumberFromInput();
            }

            try {
                con = new MySQLConnection(modemComm.getSerialNumber());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            String configureConnection = JOptionPane.showInputDialog("Do you want to configure connection? [Yes/No]", "No");
            if (configureConnection.contains("Yes") || configureConnection.contains("yes") || configureConnection.contains("Y")) {
                con.configureConnection();
            }

            con.connect();

            if (con.isConnectionStatus()) SQLLabel.setText("DataBase: CONNECTED!");
            else SQLLabel.setText("DataBase: DISCONNECTED!");

            String resultTMP = modemComm.getIP();

            if (threatStatus == false && con.isConnectionStatus() && modemComm.getConnection()) {
                //System.out.println("INN");
                if (resultTMP != null) thread.start();
                else resultTMP = modemComm.getIP();
            }

        } else if (a == disconnectButton) {
            connection = modemComm.disconnect();
            threatStatus = false;

            if (con != null) {
                try {
                    con.closeConnection();
                    thread.join();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }

            if (connection = modemComm.getConnection()) {
                this.modemLabel.setText("Modem: CONNECTED ," + portName);
                //this.lteLabel.setText(modemComm.checkLTE());
            } else {
                this.modemLabel.setText("Modem Status: DISCONNECTED");
                this.lteLabel.setText("Lte Status: NULL");
                this.IpLabel.setText("IP: NULL");
                this.CSQLabel.setText("CSQ: NULL");
            }

        }

    }


    public static void main(String[] args) {
        new GUIForm("Modem Communication App");
    }

}
