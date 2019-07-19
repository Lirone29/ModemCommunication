package App;


import gnu.io.*;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class ModemComm {

    public enum APN_Version{
        APN_database,
        APN_Internet,
    }
    public enum SIMSecurity {
        READY, //no password
        PIN,
        PUK,
        PIN2,
        PUK2,
    }

    boolean connection = false;
    public static boolean flag = false;
    static Enumeration<CommPortIdentifier> portList;
    static ArrayList<String> serialPortList;
    static CommPortIdentifier portId;

    APN_Version apn_version = APN_Version.APN_database;

    SerialReader reader;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;

    static String finalAnswer = "";
    static String modemResponse = "";

    //-----FOR TEST----------
    static String IMSI = "9508828297039";

    //PARAMETERS:
    volatile String modemPort = "COM13";
    public String serialNumber = null;
    static String PIN = "";
    static String PUK = "";
    static int phoneBookUserNumber = 1;
    String phoneNumber = "+48000000000";
    int ipVersion = 0;
    String APN = "";
    public int PinNumber=0000;
    public int Pin1Number = 0000;
    public int Pin2Number = 0000;
    public int Puk1Number = 0000;
    public int Puk2Number = 0000;
    String defaultAPN = "INTERNET";
    String user = "INTERNET";
    String password = "INTERNET";
    String IP = null;

    public void setPin1Number(int pin1Number) {
        Pin1Number = pin1Number;
    }

    public void setPin2Number(int pin2Number) {
        Pin2Number = pin2Number;
    }

    public void setPuk1Number(int puk1Number) {
        Puk1Number = puk1Number;
    }

    public void setPuk2Number(int puk2Number) {
        Puk2Number = puk2Number;
    }

    //Messages:
    static String CR = "\r\n";
    static String messageModemId = "ATI" + CR;
    static String messageAskPIN = "AT+CPIN?" + CR;
    static String messagePDPContext = "AT+CGACT?" + CR;
    static String messageIP = "AT+CGPADDR=1" + CR;
    static String messageWritePIN = "AT+CPIN=";
    String messageAttachContext = "AT+CGATT=1"+CR;
    String messageCSQ = "AT+CSQ"+CR;
    //pytam o IP i CSQ - połącznei IP + BAR DO CSQ
    // static String messageWritePIN = "AT+CPIN=" + CR;
    static String messageIMSI = "AT+CIMI" + CR;
    static String messageLTE = "AT^SYSINFOEX" + CR;
   // static String messageCheckIPVersion = "AT^IPV6CAP?" + CR;
    static String messageCheckNumbers = "AT+CNUM" + CR;
    static String messageSelectPhonebookMemoryStorage = "AT+CPBS=\"ON\"" + CR;
    static String messageWritePhonebook = "AT+CPBW=";

    public String getAPN() {
        return APN;
    }

    public void setAPN(String APN) {
        this.APN = APN;
    }

    //GOOD
    public String getCSQ(){
        writeCommandToModem(messageCSQ);
        String result = finalAnswer;
        if(!result.contains("CSQ"))result=getModemResponse();
        result = result.replaceAll("[^0-9?!\\.]", "");
        return result;
    }

    public String apnDatabase(){
        return null;
    }

    public String apnInternet(){
        return null;
    }

    //GOOD
    public String getIP(){
        String result="";
        switch (apn_version){
            case APN_database:{
                result = apnDatabase();
                break;
            }
            case APN_Internet:{
                result = apnDatabase();
                break;
            }
            default: return null;
        }

        if(result==null){
            if(apn_version==APN_Version.APN_database)
                apn_version = APN_Version.APN_Internet;
            else apn_version = APN_Version.APN_database;
        }

        return result;
    }
    //odpytywanie przez wątek z modemu o IP i CSQ - wątek synchronizowany


    public void threadFunction(){
        //AT+PIN
        //apn odczytane z bazy lub domyślnie INTERNET,U: INTERNET, P: INTERNET
        //AT+CGDCONT=1, "IP", "apn", inne parametry
        //
        //POTEM: AT+CGATT=1
        //Zapytanie o IP

    }

    //Czy mam odczytywać PIN/PUK z bazy danych ???
    //Czy mam z automatu wpisywać 0000
    //OD RAZU PRZY POŁĄCZENIU Z modemem ma rozpocząc się odpytywanie!!

    public void unlockPIN(){
        String result="";
        while (!result.contains("READY")) {
           writeCommandToModem(messageAskPIN);

            if (!finalAnswer.contains("+CPIN:")) result = finalAnswer;
            else result = getModemResponse();

            if (result.contains("SIM PIN")){
                writeCommandToModem(messageWritePIN + Pin1Number + CR);
                continue;
            }

            if (result.contains("SIM PIN2")){
                writeCommandToModem(messageWritePIN + Pin2Number + CR);
                continue;
            }
            if (result.contains("SIM PUK")){
                writeCommandToModem(messageWritePIN + Puk1Number + CR);
                continue;
            }
            if (result.contains("SIM PUK2")){
                writeCommandToModem(messageWritePIN + Puk2Number + CR);
                continue;
            }

        }
    }


    //first time
    //attach context only the first tima !!!
    public void checkIP(){

        String result = "";
        writeCommandToModem(messageAskPIN);

        if(!finalAnswer.contains("+CPIN:"))result = finalAnswer;
        else result = getModemResponse();

        if(!result.contains("READY"))unlockPIN();

        String messageSetContex = "AT+CGDCONT=1,\"IP\",\""+this.APN+"\""+CR;

        writeCommandToModem(messageSetContex);

        writeCommandToModem(messageAttachContext);


    }

    //read all available ports
    public static void getAllPorts() {
        portList = CommPortIdentifier.getPortIdentifiers();
        serialPortList = new ArrayList<>();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portList.nextElement();
            if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                serialPortList.add(portIdentifier.getName());
            }
        }
    }

    public ArrayList getAllSerialPorts() {
        return serialPortList;
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
                    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                    // setup port writer
                    outputStream = serialPort.getOutputStream();

                    // setup port reader
                    inputStream = serialPort.getInputStream();

                    reader = new SerialReader(inputStream);

                    serialPort.addEventListener(reader);
                    serialPort.notifyOnDataAvailable(true);
                    connection = true;
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

    public synchronized void writeCommandToModem(String command){
        Thread t1 = new Thread(new SerialWriter(outputStream, command));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //-------------Class to read modem response ----------------
    public static class SerialReader implements SerialPortEventListener {
        private InputStream in;
        private byte[] buffer = new byte[1024];

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public synchronized void serialEvent(SerialPortEvent arg0) {
            int data;
            String endAnswer = "";
            finalAnswer = "";
            modemResponse = "";

            try {
                int len = 0;
                while ((data = this.in.read()) > -1) {
                    buffer[len++] = (byte) data;
                    if (data == 'O') {
                        endAnswer = String.valueOf((byte) data);
                    }
                    if ((data == 'K') && (endAnswer.equals("79"))) {
                        modemResponse = new String(buffer, 0, len);
                        flag = true;
                        break;
                    }
                    if (new String(buffer, 0, len).contains("ERROR")) {
                        modemResponse = new String(buffer, 0, len);
                        break;
                    }
                }

                if (modemResponse.contains("ERROR")) {
                    for (int i = 0; i < 4; i++) {
                        data = in.read();
                        buffer[len++] = (byte) data;
                    }
                    modemResponse = new String(buffer, 0, len);
                }

                finalAnswer = new String(buffer, 0, len);
                if (finalAnswer.contains("ERROR:")) {
                    flag = true;
                }
                this.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //-------------Class to write on commands to modem ----------------
    public static class SerialWriter implements Runnable {
        OutputStream out;
        String commandToWrite = "";

        protected void finalize() {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public SerialWriter(OutputStream out, String tmpCommand) {
            this.commandToWrite = tmpCommand;
            this.out = out;
        }

        public synchronized void run() {
            flag = false;
            byte[] b = commandToWrite.getBytes(StandardCharsets.UTF_8);
            try {
                int i = 0;
                byte[] c = b;
                while ((i) < (c.length)) {
                    out.write(c[i]);
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                this.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    public void setPortName(String portName) {
        modemPort = portName;
        setModemPort();
    }

    public String getPortName() {
        return this.modemPort;
    }

    class CloseThread extends Thread {
        public synchronized void run() {
            serialPort.close();
            serialPort.removeEventListener();
            if(portId.isCurrentlyOwned())System.out.println("IN USE IN CLOSE THREAD");
            connection = false;
            System.out.println("PORT CLOSED!!!! SUCCESS!!");
        }
    }

    //setting up modem as current port
    public void setModemPort() {
        CommPortIdentifier tmpPortId = null;
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

    public synchronized String getModemResponse() {
        if(connection==false) return null;
        while (!flag) {
            if (modemResponse.endsWith("OK")) break;
            if (finalAnswer.contains("ERROR")) break;
        }
        flag = false;
        return modemResponse;
    }

    public void enablePhonebookMemoryStore() {
        if(connection==false) return;
        writeCommandToModem(messageSelectPhonebookMemoryStorage);
    }

    public void writePhoneNumber() {
        if(connection==false) return;
        int type = Integer.valueOf(JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have + ", 145));
        phoneNumber = JOptionPane.showInputDialog("Write phone number: \n");
        String text = JOptionPane.showInputDialog("User alias/name:");
        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));
        String mesWritePhonebook = messageWritePhonebook += phoneBookUserNumber + ",\"" + phoneNumber + "\"," + type + ",\"" + text + "\"" + CR;

        writeCommandToModem(mesWritePhonebook);
    }

    public void clearNumber() {
        if(connection==false) return;
        writeCommandToModem(messageSelectPhonebookMemoryStorage);

        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID to clear:", 1));

        writeCommandToModem("AT+CPBW=" + phoneBookUserNumber + CR);
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumberFromInput() {
        //this.serialNumber = IMSI;
        this.serialNumber = modemResponse.replaceAll("[^0-9?!\\.]", "");
    }

    public void setSerialNumber(String tmpSerialNumber) {
        this.serialNumber = tmpSerialNumber;
    }

    public String checkSecurity(String security) {
        if(connection==false) return null;

        PIN = null;
        PUK = null;
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
            if (PIN != null && !PIN.equals("PIN  -  ")) PIN += ", SIM PIN2";
            PIN = "SIM PIN2";
        }
        if (security.contains(String.valueOf("SIM " + SIMSecurity.PUK2))) {
            result += String.valueOf(SIMSecurity.PUK2) + "\n";
            if (PUK != null && !PUK.equals("PUK  -  ")) PUK += ", SIM PUK2";
            PUK = "SIM PUK2";
        }
        return "\n" + PIN + " \n" + PUK;
    }

    public void getModemInfo() {
        if(connection==false) return;
        writeCommandToModem(messageModemId);
    }

    public void readSimCardSerialNumber() {
        if(connection==false) return;
        writeCommandToModem(messageIMSI);
    }

    public void readSecurity() {
        if(connection==false) return;
        PIN = null;
        PUK = null;
        writeCommandToModem(messageAskPIN);
    }

    public void writeCommand(String command) {
        if(connection==false) return;
        writeCommandToModem(command + CR);
    }

    public void checkNumbersOnSIM() {
        if(connection==false) return;
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageCheckNumbers)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //if (finalAnswer.contains("ERROR")) flag = true;
    }

    public String checkLTE() {

        if(connection==false) return null;
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageLTE)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result;
        if(finalAnswer.contains("LTE")) result = finalAnswer;
        else result = this.getModemResponse();

        if (portId == null) return "Lte Status: NULL";
        if (result.contains("6,\"LTE\"")) return new String("LTE - Full Supported");
        else if (result.contains("101,\"LTE\"")) return new String("LTE - Sub-mode Supported");
        else return new String("LTE - Not Supported");
    }

    public void checkPDPContext() {
        if(connection==false) return;
        writeCommandToModem(messagePDPContext);

    }

    public String messageIP() {
        if(connection==false) return null;
        String result = finalAnswer;
        if (result.contains("1,1")) {
            Thread t2 = new Thread(new SerialWriter(outputStream, messageIP));
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

    public boolean disconnect() {
        if(connection==false) return true;

        if (this.serialPort != null) {
            this.serialPort.notifyOnDataAvailable(false);
            try {
                this.serialPort.getOutputStream().close();
                this.serialPort.getInputStream().close();
                this.reader.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("After stream");
        new CloseThread().start();

        if (portId.isCurrentlyOwned()) {
            System.out.println(portId + " in use!");
            connection = true;
            return false;
        }

        return true;
    }

    public ModemComm() {
        getAllPorts();

    }

}
