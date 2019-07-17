package App;

import gnu.io.*;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class ModemComm {

    public enum SIMSecurity {
        READY, //no password
        PIN,
        PUK,
        PIN2,
        PUK2,
    }

    static boolean flag = false;
    static Enumeration<CommPortIdentifier> portList;
    static ArrayList<String> serialPortList;
    static CommPortIdentifier portId;

    static SerialPort serialPort;
    static OutputStream outputStream;
    static InputStream inputStream;

    static String finalAnswer = "";

    //test data
    static String modemResponse = "";
    //-------FOR TEST--------- static String IMSI = "9508828297039";

    //PORT NAME
    volatile String modemPort = "COM13";
    public String serialNumber = null;

    static String PIN = "";
    static String PUK = "";
    static int phoneBookUserNumber = 1;
    String phoneNumber = "+48000000000";

    //Messages:
    static String CR = "\r\n";
    String ownMessage = "";
    static String messageModemId = "ATI" + CR;
    static String messageAskPIN = "AT+CPIN?" + CR;
    static String messagePDPContext = "AT+CGACT?" + CR;
    static String messageIP = "AT+CGPADDR=1" + CR;
   // static String messageWritePIN = "AT+CPIN=" + CR;
    static String messageIMSI = "AT+CIMI" + CR;

    //static String messageConnection = "AT+CGATT?" + CR;
    static String messageLTE = "AT^SYSINFOEX" + CR;
    static String messageCheckIPVersion = "AT^IPV6CAP?" + CR;
    int ipVersion = 0;

    //returns all numbers Currently written in PhoneBook| the MSISDN
    static String messageCheckNumbers = "AT+CNUM" + CR;
    static String messageSelectPhonebookMemoryStorage = "AT+CPBS=\"ON\"" + CR;
    static String messageWritePhonebook = "AT+CPBW=";

    //read all available ports
    public static void getAllPorts() {
        portList = CommPortIdentifier.getPortIdentifiers();
        serialPortList = new ArrayList<>();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portList.nextElement();
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL){
                serialPortList.add(portIdentifier.getName());
            }
        }
    }

    public ArrayList getAllSerialPorts(){
        return serialPortList;
    }

    public String getOwnMessage() {
        return ownMessage;
    }

    public void setOwnMessage(String ownMessage) {
        this.ownMessage = ownMessage;
    }

    public boolean connect() {
        if (portId.isCurrentlyOwned()) {
            System.out.println("Port in use!");
            return false;
        } else {
            if (portId.getName().equals(modemPort)) {
                try {
                    // Port owner and connection timeout
                    serialPort = (SerialPort) portId.open(modemPort, 2000);

                    // Configure connection parameters - baudrate,  dataBits,   stopBits,   parity
                    serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                    // setup port writer
                    outputStream = serialPort.getOutputStream();

                    // setup port reader
                    inputStream = serialPort.getInputStream();

                    serialPort.addEventListener(new SerialReader(inputStream));
                    serialPort.notifyOnDataAvailable(true);
                    return true;

                } catch (PortInUseException e) {
                    e.printStackTrace();
                } catch (UnsupportedCommOperationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TooManyListenersException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

    //-------------Class to read modem response ----------------
    public static class SerialReader implements SerialPortEventListener {
        private InputStream in;
        private byte[] buffer = new byte[1024];

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void serialEvent(SerialPortEvent arg0) {
            int data;
            String endAnswer = "";
            finalAnswer = "";
            try {
                int len = 0;
                while ((data = in.read()) > -1) {
                    buffer[len++] = (byte) data;
                    if (data == 'O') {
                        endAnswer = String.valueOf((byte)data);
                    }
                    if ((data == 'K') && (endAnswer.equals("79"))) {
                        modemResponse = new String(buffer, 0, len);
                        flag = true;
                        break;
                    }
            }
                finalAnswer = new String(buffer, 0, len);
                System.out.print(new String(buffer, 0, len));
                if(finalAnswer.contains("ERROR")){
                    flag = true;
                    modemResponse = finalAnswer;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

//-------------Class to write on commands to modem ----------------
    public static class SerialWriter implements Runnable {
        OutputStream out;
        String commandToWrite = "";

        public SerialWriter(OutputStream out, String tmpCommand) {
            this.commandToWrite = tmpCommand;
            this.out = out;
        }

        public void run() {
            byte[] b = commandToWrite.getBytes(StandardCharsets.UTF_8);
            try {
                int i = 0;
                byte[] c = b;
                while ((i) < (c.length)) {
                    this.out.write(c[i]);
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    public void setPortName(String portName){
        modemPort = portName;
        setModemPort();
    }

    //setting up modem as current port
    public void setModemPort() {
            CommPortIdentifier tmpPortId = null;
            portList = null;
            portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                tmpPortId = (CommPortIdentifier) portList.nextElement();
                if (tmpPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (tmpPortId.getName().equals(modemPort)) {
                        portId = tmpPortId;
                        return;
                    }
                }
            }
    }

    //GOOD
    public String getModemResponse(){
       while (!flag){
       }
        flag = false;
        return modemResponse;
    }

    //GOOD
    public void enablePhonebookMemoryStore(){
        Thread t1 = new Thread(new SerialWriter(outputStream, messageSelectPhonebookMemoryStorage));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //GOOD
    public void writePhoneNumber()
    {
        int type =Integer.valueOf(JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have", 145));
        phoneNumber =  JOptionPane.showInputDialog("Write phone number: \n");
        String text = JOptionPane.showInputDialog("User alias/name:");
        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));
        String mesWritePhonebook = messageWritePhonebook += phoneBookUserNumber + ",\"" + phoneNumber + "\"," + type + ",\"" + text + "\"" + CR;

        Thread t2 = (new Thread(new SerialWriter(outputStream, mesWritePhonebook)));
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //GOOD
    public void clearNumber() {

        Thread t1 = new Thread(new SerialWriter(outputStream, messageSelectPhonebookMemoryStorage));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID to clear:", 1));

        Thread t2 = new Thread(new SerialWriter(outputStream, "AT+CPBW=" + phoneBookUserNumber + CR));
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    //GOOD
    public void setSerialNumberFromInput(){
        this.serialNumber = modemResponse.replaceAll("[^0-9?!\\.]","");
    }

    public void setSerialNumber(String tmpSerialNumber) {
        this.serialNumber = tmpSerialNumber;
    }

    //GOOD
    public String checkSecurity(String security) {

        String result = "";
        if (security.contains(String.valueOf(SIMSecurity.READY))) {
            result = String.valueOf(SIMSecurity.READY) + "\n";
            PIN = "PIN  -  ";
            PUK = "PUK  -  ";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PIN + " "))) {
            result = String.valueOf(SIMSecurity.PIN) + "\n";
            PIN = "SIM PIN";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PUK + " "))) {
            result += String.valueOf(SIMSecurity.PUK) + "\n";
            PUK = "SIM PUK";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PIN2))) {
            result += String.valueOf(SIMSecurity.PIN2) + "\n";
            PIN = "SIM PIN2";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PUK2))) {
            result += String.valueOf(SIMSecurity.PUK2) + "\n";
            PUK = "SIM PUK2";
        }
        return "\n" +PIN + " \n" + PUK;
    }

    public String checkSecurity() {

        String result = "";
        String security = finalAnswer;
        if (security.contains(String.valueOf(SIMSecurity.READY))) {
            result = String.valueOf(SIMSecurity.READY) + "\n";
            PIN = null;
            PUK = null;
        }

        if (security.contains(String.valueOf("SIM " + SIMSecurity.PIN + ""))) {
            result = String.valueOf(SIMSecurity.PIN) + "\n";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PUK+"")))
            result += String.valueOf(SIMSecurity.PUK) + "\n";
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PIN2)))
            result += String.valueOf(SIMSecurity.PIN2) + "\n";
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PUK2)))
            result += String.valueOf(SIMSecurity.PUK2) + "\n";
        return result;
    }

    //GOOD
    public void getModemInfo() {
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageModemId)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //GOOD
    public void readSimCardSerialNumber() {
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageIMSI)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //GOOD
    public void readSecurity() {
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageAskPIN)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //GOOD
    public void writeCommand(String command){
        Thread t1 = (new Thread(new SerialWriter(outputStream, command +CR)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void checkNumbersOnSIM() {
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageCheckNumbers)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //GOOD
    public String checkLTE(){
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageLTE)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result =getModemResponse();

        if(result.contains("6,\"LTE\""))return new String("LTE - Supported");
        else if(result.contains("101,\"LTE\"")) return new String("LTE- sub-mode Supported");
        else return new String("LTE - Not Supported");
    }

    //----------------to CHECK---------------------
    public String getIP(){

        Thread t1 = new Thread(new SerialWriter(outputStream,messagePDPContext));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = finalAnswer;
        if(result.contains("1,1")){

            Thread t2 = new Thread(new SerialWriter(outputStream,messageIP));
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return finalAnswer;
        }

        return null;
    }

    public String checkIPVersion() {
        Thread t1 = new Thread(new SerialWriter(outputStream, messageCheckIPVersion));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return finalAnswer;
    }

    public ModemComm() {
       getAllPorts();
    }

}
