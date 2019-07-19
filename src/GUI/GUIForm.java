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
    int timeout = 100;

    public class MyThread extends Thread {

        public void run(){
            threatStatus=true;
            while(threatStatus){
                CSQLabel.setText("CSQ: "+modemComm.getCSQ());
                IpLabel.setText("IP " + modemComm.getIP());

                try {
                    this.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setSecurity() {
        if(con.isConnectionStatus()==true && modemComm.connect()==true){
            try {
                modemComm.setAPN(con.getAPN());
                modemComm.setPin1Number(Integer.valueOf(con.getPin1()));
                modemComm.setPin2Number(Integer.valueOf(con.getPin2()));
                modemComm.setPuk1Number(Integer.valueOf(con.getPuk1()));
                modemComm.setPuk2Number(Integer.valueOf(con.getPuk2()));
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

        modemComm = new ModemComm();
        serialPortList = modemComm.getAllSerialPorts();


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

        if(a==executeButton) {
            if (!this.commandTextField.getText().equals("")) {
                modemComm.writeCommand(this.commandTextField.getText());
                this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            }
        }else if (a==clearButton) {
            this.modemTextArea.setText("");
            this.commandTextField.setText("");

        }else if(a==connectButton) {
            this.modemTextArea.setText("");
            if (connection == false) {
                portName = serialPortComboBox.getSelectedItem().toString();
                modemComm.setPortName(portName);
                connection = modemComm.connect();
                if (connection == true) {
                    this.modemLabel.setText("Modem: CONNECTED, " + portName);
                    this.lteLabel.setText(modemComm.checkLTE());
                    if(con.isConnectionStatus())thread.start();
                } else {
                    this.modemLabel.setText("Modem Status: DISCONNECTED");
                    this.lteLabel.setText("Lte Status: NULL");
                }

            } else {
                JOptionPane.showMessageDialog(null, "App is already connected to serial port. \n Disconnect first to change port\n", "Connection Warning!", JOptionPane.INFORMATION_MESSAGE);
            }
        }else if(a==compareIPAddressButton) {
            try {
                dataBaseIp = con.getIP();
                System.out.println(dataBaseIp);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            modemComm.checkPDPContext();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            String modemIP = modemComm.messageIP();
            if (!(modemIP == null)) {
                this.modemTextArea.setText(this.modemTextArea.getText() + modemComm.getModemResponse());
            }

            if (dataBaseIp.equals(modemIP))
                this.modemTextArea.setText(this.modemTextArea.getText() + "\n------------------------------------------\nThe same IP address: \n" + modemIP);
            else
                this.modemTextArea.setText(this.modemTextArea.getText() + "\n------------------------------------------\nIP Addres: \nDataBase IP: " + dataBaseIp + "\nModem IP: " + modemIP);

        }else if(a== serialPortComboBox) {
            portName = serialPortComboBox.getSelectedItem().toString();

        }else if(a==checkSIMSecurityButton) {
            //Reads how secured is SIM
            modemComm.readSecurity();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            this.modemTextArea.setText(modemTextArea.getText() + "\n------------------------------------------" + modemComm.checkSecurity(modemTextArea.getText()));

        }else if(a== getSerialNumberButton) {
            modemComm.readSimCardSerialNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
            modemComm.setSerialNumberFromInput();

        }else if(a==modemInfoButton) {
            modemComm.getModemInfo();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));

        }else if (a== clearNumberButton) {
            modemComm.clearNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));

        }else if (a == writeNumberButton) {
            modemComm.enablePhonebookMemoryStore();
            modemComm.writePhoneNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));

        }else if(a== displaySIMNumberButton) {
            modemComm.checkNumbersOnSIM();
            this.modemTextArea.setText("" + modemComm.getModemResponse().replaceFirst("\n", ""));
        }else if(a== DBConnectButton) {

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

            if(threatStatus==false && con.isConnectionStatus() && modemComm.connect()){
                thread.start();
            }

            if (con.isConnectionStatus()) SQLLabel.setText("DataBase: CONNECTED!");
            else SQLLabel.setText("DataBase: DISCONNECTED!");

        }else if(a==disconnectButton){
            connection = modemComm.disconnect();
            threatStatus = false;

            try {
                con.closeConnection();
                thread.join();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            if (connection == true){
                this.modemLabel.setText("Modem : CONNECTED ," + portName);
                this.lteLabel.setText(modemComm.checkLTE());
                setSecurity();
            }
            else{
                this.modemLabel.setText("Modem Status: DISCONNECTED");
                this.lteLabel.setText("Lte Status: NULL");
            }
        }

    }


    public static void main(String[] args){
        new GUIForm("Modem Communication App");
    }

}
