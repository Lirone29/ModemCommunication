package GUI;

import App.ModemComm;
import App.MySQLConnection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class GUIForm extends JFrame implements ActionListener {

    boolean connection = false;

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

    static InputStream inputStream;

    ModemComm modemComm;
    App.MySQLConnection con;

    String portName = "";

    String dataBaseIp = "";
    ArrayList<String> serialPortList;

        public GUIForm(String title){
        super(title);
        setResizable(true);
        setContentPane(panelGUI);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        modemComm = new ModemComm();
        serialPortList = modemComm.getAllSerialPorts();


        for(int i = 0 ; i < serialPortList.size(); i++) {
            serialPortComboBox.addItem(serialPortList.get(i));
        }


        modemTextArea.setText("");
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
        pack();
        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object a = e.getSource();

        if(a==executeButton){
            //DONE
            modemComm.writeCommand(this.commandTextField.getText());
            this.modemTextArea.setText("" + modemComm.getModemResponse());

       }else if (a==clearButton){
            //DONE!!
           this.modemTextArea.setText("");
           this.commandTextField.setText("");

       }else if(a==connectButton)
       {
           //DONE!!
            portName = serialPortComboBox.getSelectedItem().toString();
            modemComm.setPortName(portName);
            connection = modemComm.connect();
            if (connection == true) this.modemLabel.setText(modemLabel.getText() + " CONNECTED");
            else this.modemStatusLabel.setText(modemLabel.getText()+" DISCONNECTED");

            this.lteLabel.setText(modemComm.checkLTE());

        }else if(a==compareIPAddressButton){
            //--------------TO CHECK------------------
            try {
                dataBaseIp = con.getIP();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            String modemIP =modemComm.getIP();
            if(dataBaseIp.equals(modemIP))this.modemTextArea.setText("The same IP address: \n" +modemIP);
            else this.modemTextArea.setText("Different IP Addres: \nDataBase IP: " + dataBaseIp + "\n Modem IP: "+modemIP);
            //this.modemTextArea.setText("" + modemComm.getModemResponse());

       }else if(a== serialPortComboBox){
            //DONE
            portName = serialPortComboBox.getSelectedItem().toString();

       }else if(a==checkSIMSecurityButton){

            //Reads how secured is SIM
            modemComm.readSecurity();
            this.modemTextArea.setText("" + modemComm.getModemResponse());
            this.modemTextArea.setText(modemTextArea.getText() +"\n" + modemComm.checkSecurity(modemTextArea.getText()));

       }else if(a== getSerialNumberButton){
            //DONE - konieczność najpierw odczytu
            modemComm.readSimCardSerialNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse());
            modemComm.setSerialNumberFromInput();

       }else if(a==modemInfoButton){
            //DONE
            modemComm.getModemInfo();
            this.modemTextArea.setText("" + modemComm.getModemResponse());

       }else if (a== clearNumberButton){
            //Check one more time
            modemComm.clearNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse());
            this.modemTextArea.setText(this.modemTextArea.getText() + modemComm.getModemResponse());

       }else if (a == writeNumberButton){
            //DONE
            modemComm.enablePhonebookMemoryStore();
            modemComm.writePhoneNumber();
            this.modemTextArea.setText("" + modemComm.getModemResponse());
            this.modemTextArea.setText(this.modemTextArea.getText() + modemComm.getModemResponse());
       }else if(a== displaySIMNumberButton) {
            //DONE
            modemComm.checkNumbersOnSIM();
            this.modemTextArea.setText("" + modemComm.getModemResponse());

        }else if(a== DBConnectButton){

            if(modemComm.getSerialNumber().equals(null)) {
                modemComm.readSimCardSerialNumber();
                modemComm.getModemResponse();
                modemComm.setSerialNumberFromInput();
            }
            try {
                con = new MySQLConnection(modemComm.getSerialNumber());
                SQLLabel.setText("DataBase: CONNECTED");
            }catch (SQLException er) {
                SQLLabel.setText("DataBase: DISCONNECTED");
                er.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        new GUIForm("Modem communication");
    }

}
