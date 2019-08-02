package ModemComm.App;

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

    Object[] options = {"Internet", "cellbox.pl", "Write own APN","imgw.pl", "public cellbox", "intermobile.pl", "aquard.pl", "telemeria.pl", "m2m.plusgsm.pl"};
    Object[] networkOptions = {"0-2G: GSM", "1-2G: GMS Compact", "2-3G: UTRAN", "3-2G: GSM w/EGPRS", "4-3G: UTRAN w/HSDPA", "5-3G: UTRAN w/HSUPA ", "6-3G: UTRAN w/HSDPA and HSUPA", "7-LTE: E~UTRAN"};
    boolean connection = false;
    public static boolean flag = false;
    static Enumeration<CommPortIdentifier> portList;
    static ArrayList<String> serialPortList;
    static CommPortIdentifier portId;
    SerialReader reader;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;

    //-----FOR TEST----------
    // static String IMSI = "9508828297039";
    //PARAMETERS:
    static String modemResponse = "";
    volatile String modemPort = "COM13";
    public String serialNumber = null;
    String PIN, PUK = "";
    int phoneBookUserNumber = 1;
    String phoneNumber = "+48000000000";
    boolean answer = false;
    String databaseAPN = "";
    public String Pin1Number,Pin2Number,Puk1Number,Puk2Number = "0000";
    public String APN = "";
    String internetAPN = "INTERNET";

    boolean attached = false;
    String userAPN,passwordAPN  = "INTERNET";

    String IP,CSQ,operator,wirelessComm, wirelessCommLabel = null;
    String fileName = "APN History";

    //Messages:
    static String CR = "\r\n";
    static String messageModemId = "ATI" + CR;
    static String messageCheckPIN = "AT+CPIN?" + CR;
    static String messageCheckPDPContext = "AT+CGACT?" + CR;
    static String messageIP = "AT+CGPADDR=1" + CR;
    static String messageWritePIN = "AT+CPIN=";
    static String messageCheckOperator = "AT+COPS?" + CR;
    static String messageDetachContext = "AT+CGACT=0,1" + CR;
    static String messageChangeWirelessComm = "AT+COPS=1,0";
    static String messageTurnOffPeriodic = "AT^CURC=0" + CR;
    static String messageAttachGPRSService = "AT+CGATT=1" + CR;
    static String messageDetachGPRSService = "AT+CGATT=0" + CR;
    static String messageAttachContext = "AT+CGACT=1" + CR;
    static String messageCSQ = "AT+CSQ" + CR;
    static String messageEnableRegistration = "AT+CREG=1" + CR;
    static String messageDefineContext = "AT+CGDCONT=1,\"IP\",\"";
    static String messageCheckNetworkRegistration = "AT+CREG?" + CR;
    static  String messageCheckStateOfContex = "AT+CGDCONT?" + CR;
    static String messageIMSI = "AT+CIMI" + CR;
    static String messageLTE = "AT^SYSINFOEX" + CR;
    static String messageCheckNumbersOnSIM = "AT+CNUM" + CR;
    static String messageSelectPhonebookMemoryStorage = "AT+CPBS=\"ON\"" + CR;
    static String messageWritePhonebook = "AT+CPBW=";

    public void setPin1Number(String pin1Number) {
        Pin1Number = pin1Number;
    }

    public void setPin2Number(String pin2Number) {
        Pin2Number = pin2Number;
    }

    public void setPuk1Number(String puk1Number) {
        Puk1Number = puk1Number;
    }

    public void setPuk2Number(String puk2Number) {
        Puk2Number = puk2Number;
    }

    public String getAPN() {
        return APN;
    }

    public String getWirelessCommLabel() {
        return wirelessCommLabel;
    }

    public void turnSpamOff() {
        writeCommandToModem(messageTurnOffPeriodic);
    }

    public void setFromDatabaseAPN(String APN) {
        this.databaseAPN = APN;
    }

    public String getCSQ() {
        writeCommandToModem(messageCSQ);
        String result = modemResponse;
        if (!result.contains("CSQ:")) {
            while (!result.contains("CSQ:"))
                result = getModemResponse();
        }
        String[] result2 = result.split(":");
        result = (result2[1].replaceAll("OK", ""));
        return result;
    }

    public synchronized String checkIPAddr() {
        String result = null;
        modemResponse="";
        writeCommandToModem(messageIP);
        result = getModemResponse();

        while (!result.contains("+CGPADDR:")){
            writeCommandToModem(messageIP);
            result = getModemResponse();
            if (result.contains("ERROR")) return null;
        }

        if(!result.contains("\"")) {
            IP="NULL";
            return "NULL";
        }

        String[] value = result.split("\"");
        return (IP = value[1]);
    }

    public boolean getConnection() {
        return connection;
    }

    public String getIPAddress() {
        return IP;
    }

    public void detachConnection(){
        String result="";
        modemResponse = "";
        writeCommandToModem("AT+CGACT=0,1"+CR);
        result = getModemResponse();

        modemResponse = "";
        writeCommandToModem("AT+CGATT=0"+CR);
        result = getModemResponse();
        if(!result.contains("OK")) result = getModemResponse();
    }
    //IF APN OR WIrelessConn IS Changed
    public String changeIP() throws IOException {

        BufferedWriter writer = null;
        int j = 0;
        try {
            writer = new BufferedWriter(new FileWriter("testChangeIP.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i =0;
        String result = "";

        //detachConnection();

        do {
            modemResponse = "";
            writeCommandToModem(messageEnableRegistration);
            result = getModemResponse();
            writer.write("\n"+(j++)+result);
            if (result.contains("ERROR")) break;
        } while (!(result.contains("AT+CREG")));

        //System.out.println("#2 " + result);
        do {
            modemResponse = "";
            writeCommandToModem(messageCheckNetworkRegistration);
            result = getModemResponse();
            writer.write("\n"+(j++)+result);
            //if (result.contains("ERROR")) break;
        } while (!result.contains("1,1"));

        //System.out.println("#3 " + result);
        do{
            modemResponse = "";
            writeCommandToModem(messageAttachGPRSService);
            result = getModemResponse();
            writer.write("\n" + (j++) + result);
        } while (!result.contains("AT+CGATT"));

        //System.out.println("#4 " + result);
        modemResponse = "";
        writeCommandToModem("AT+CGATT?"+CR);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        //System.out.println("#5 " + result);
        modemResponse = "";
        writeCommandToModem(messageDefineContext + APN + "\"" + CR);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        //System.out.println("#6 " + result);
        modemResponse = "";
        writeCommandToModem(messageAttachContext);
        result = getModemResponse();
        //System.out.println("#7 " + result);
        writer.write("\n"+(j++)+result);
        writer.close();

        return result;
    }

    //---------------------getIPFUNCTION---------
    public String getIP() throws IOException {

        if(attached==true)detachConnection();

        BufferedWriter writer = null;
        int j = 0;
        try {
            writer = new BufferedWriter(new FileWriter("test1.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = "";
        if (answer == false) answer = unlockPIN();
        Object selectedValue = JOptionPane.showInputDialog(null, "Choose APN to check IP", "Connect to APN",
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        APN = selectedValue.toString();

        if(APN.contains("Write own APN")){ APN= JOptionPane.showInputDialog("Please input APN value: "); }

        writeCommandToModem(messageCheckOperator);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        while (!result.contains("+COPS:")){
            writeCommandToModem(messageCheckOperator);
            result = getModemResponse();
            writer.write("\n"+(j++)+result);
        }

        String values[] = result.split(",");
        operator = values[2].trim();
        wirelessComm = values[3].replace("OK", "").trim();
        for(int i = 0; i < networkOptions.length; i++) if(i == Integer.parseInt(wirelessComm)) wirelessCommLabel = (networkOptions[i].toString()).split("-")[1];

        modemResponse = "";
        writeCommandToModem(messageEnableRegistration);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);


        do {
            modemResponse = "";
            writeCommandToModem(messageCheckNetworkRegistration);
            result = getModemResponse();
            writer.write("\n"+(j++)+result);
            if (result.contains("ERROR")) break;
        } while (!result.contains("1,1"));

        //System.out.println("2th " +result);
        modemResponse = "";

        while (!result.contains("AT+CGATT")) {
            writeCommandToModem(messageAttachGPRSService);
            result = getModemResponse();
            writer.write("\n" + (j++) + result);
        }

        //System.out.println("3th");
            modemResponse = "";
        writeCommandToModem("AT+CGATT?"+CR);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        //System.out.println("4th" + result);
        modemResponse = "";
        String a = messageDefineContext + selectedValue + "\"" + CR;
        writeCommandToModem(a);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        modemResponse = "";
        //System.out.println("4.5th" +result);
        writeCommandToModem(messageAttachContext);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        modemResponse = "";
        //System.out.println("5th" + result);
        writeCommandToModem(messageCheckStateOfContex);
        result = getModemResponse();
        writer.write("\n"+(j++)+result);

        writer.close();
        attached = true;
        return result;
    }

    public boolean unlockPIN() {
        String result = "";
        while (!result.contains("READY")) {
            writeCommandToModem(messageCheckPIN);

            if (modemResponse.contains("+CPIN:")) result = modemResponse;
            else result = getModemResponse();

            if (result.contains("SIM PIN")) {
                writeCommandToModem(messageWritePIN + Pin1Number + CR);
                continue;
            }

            if (result.contains("SIM PIN2")) {
                writeCommandToModem(messageWritePIN + Pin2Number + CR);
                continue;
            }
            if (result.contains("SIM PUK")) {
                writeCommandToModem(messageWritePIN + Puk1Number + CR);
                continue;
            }
            if (result.contains("SIM PUK2")) {
                writeCommandToModem(messageWritePIN + Puk2Number + CR);
                continue;
            }

        }
        return (result.contains("READY"));
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

    //WriteCommanFunction
    public synchronized void writeCommandToModem(String command) {
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
        private byte[] buffer = new byte[2048];

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public synchronized void serialEvent(SerialPortEvent arg0) {
            int data;
            String endAnswer = "";
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

    class CloseThread extends Thread {
        public synchronized void run() {
            serialPort.close();
            serialPort.removeEventListener();
            if (portId.isCurrentlyOwned()) System.out.println("CL Port In Use!");
            connection = false;
            System.out.println("PORT CLOSED!");
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

    //-------------------getModemResponseFUNCTION
    public synchronized String getModemResponse() {
        String result = modemResponse;
        int i = 0;
        if (connection == false) return null;
        while (!flag ) {
            if (result.endsWith("OK")) break;
            if (result.contains("ERROR")) break;
            if (result.contains("NOT SUPPORT")) break;
            result = modemResponse;
            i++;
            if(i>200000000)break;
        }
        flag = false;
        return result;
    }

    public void enablePhonebookMemoryStore() {
        if (connection == false) return;
        writeCommandToModem(messageSelectPhonebookMemoryStorage);
    }

    public void writePhoneNumber() {
        if (connection == false) return;
        int type = Integer.valueOf(JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have + ", 145));
        phoneNumber = JOptionPane.showInputDialog("Write phone number: \n");
        String text = JOptionPane.showInputDialog("User alias/name:");
        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));
        String mesWritePhonebook = messageWritePhonebook += phoneBookUserNumber + ",\"" + phoneNumber + "\"," + type + ",\"" + text + "\"" + CR;
        writeCommandToModem(mesWritePhonebook);
    }

    public void clearNumber() {
        if (connection == false) return;
        writeCommandToModem(messageSelectPhonebookMemoryStorage);
        phoneBookUserNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID to clear:", 1));
        writeCommandToModem("AT+CPBW=" + phoneBookUserNumber + CR);
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumberFromInput() {
        this.serialNumber = modemResponse.replaceAll("[^0-9?!\\.]", "");
    }

    public String checkSecurity(String security) {
        if (connection == false) return null;
        String compare = "\nSIM security in database: \nPIN1 " + Pin1Number + "\tPIN2 " + Pin2Number + "\nPUK1 " + Puk1Number + "\tPUK2 " + Puk2Number;
        PIN = null;
        PUK = null;
        String result = "";

        if (security.contains(String.valueOf(SIMSecurity.READY))) {
            result = String.valueOf(SIMSecurity.READY) + "\n";
            PIN = "PIN  NULL  ";
            PUK = "PUK  NULL  ";
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
        return compare + "\nIn SIM Card: \n" + PIN + " \n" + PUK;
    }

    public void getModemInfo() {
        if (connection == false) return;
        writeCommandToModem(messageModemId);
    }

    public void readSimCardSerialNumber() {
        if (connection == false) return;
        writeCommandToModem(messageIMSI);
    }

    public void readSecurity() {
        if (connection == false) return;
        PIN = null;
        PUK = null;
        writeCommandToModem(messageCheckPIN);
    }

    public void writeCommand(String command) {
        if (connection == false) return;
        writeCommandToModem(command + CR);
    }

    public void checkNumbersOnSIM() {
        if (connection == false) return;
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageCheckNumbersOnSIM)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String changeWirelessComm() {
        String result = " ";
        String oldWirelessComm = wirelessComm;
        String oldWirelessCommLabel = wirelessCommLabel;

        detachConnection();

        //writeCommandToModem(messageDetachContext);
        //result = getModemResponse();

        //writeCommandToModem("AT+CGATT=0"+CR);
        //result = getModemResponse();


        writeCommandToModem(messageCheckOperator);
        result = getModemResponse();

        while (!result.contains("+COPS:")) {
            writeCommandToModem(messageCheckOperator);
            result = getModemResponse();
        }

        String values[] = result.split(",");
        operator = values[2].trim();

        Object selectedValue = JOptionPane.showInputDialog(null, "Choose Wireless Connection", "Wireless Type of Connection",
                JOptionPane.INFORMATION_MESSAGE, null, networkOptions, networkOptions[0]);

        String[] tmpWirelessComm = selectedValue.toString().split("-");
        wirelessComm = tmpWirelessComm[0];
        wirelessCommLabel = tmpWirelessComm[1];

        writeCommandToModem(messageChangeWirelessComm + ","+ operator + "," + wirelessComm +CR);
        result =getModemResponse();

        if(!result.contains("AT+COPS"))result = getModemResponse();
        if(result.contains("ERROR")){
            wirelessCommLabel = oldWirelessCommLabel;
            wirelessComm = oldWirelessComm;
            return result;
        }

        //try {
            //this.changeIP();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        return result;

    }

    public String checkLTE() {

        if (connection == false) return null;
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageLTE)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String result;
        if (modemResponse.contains("LTE")) result = modemResponse;
        else result = this.getModemResponse();

        if (portId == null) return "Lte Status: NULL";
        if (result.contains("6,\"LTE\"")) return new String("LTE - Full Supported");
        else if (result.contains("101,\"LTE\"")) return new String("LTE - Sub-mode Supported");
        else return new String("LTE - Not Supported or NO info");
    }

    public boolean disconnect() {
        if (connection == false) return true;
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
        new CloseThread().start();
        return true;
    }

    public ModemComm() {
        getAllPorts();
        answer = false;
    }

}
