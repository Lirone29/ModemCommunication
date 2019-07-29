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

    public enum APN_Version {
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
    APN_Version apn_version;
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
    static String PIN = "";
    static String PUK = "";
    static int phoneBookUserNumber = 1;
    String phoneNumber = "+48000000000";
    boolean answer = false;
    String databaseAPN = "";
    public String Pin1Number = "0000";
    public String Pin2Number = "0000";
    public String Puk1Number = "0000";
    public String Puk2Number = "0000";
    String internetAPN = "INTERNET";
    String user = "INTERNET";
    String password = "INTERNET";
    String IP = null;
    String CSQ = null;

    //Messages:
    static String CR = "\r\n";
    static String messageModemId = "ATI" + CR;
    static String messageAskPIN = "AT+CPIN?" + CR;
    static String messageAskPDPContext = "AT+CGACT?" + CR;
    static String messageIP = "AT+CGPADDR=1" + CR;
    static String messageWritePIN = "AT+CPIN=";

    String messageTurnOffPeriodic = "AT^CURC=0" + CR;
    String messageAttachGPRSService = "AT+CGATT=1" + CR;
    String messageAttachContext = "AT+CGACT=1" + CR;
    String messageCSQ = "AT+CSQ" + CR;
    String messageEnableRegistration = "AT+CREG=1" + CR;
    String messageDefineContext = "AT+CGDCONT=1,\"IP\",\"";
    String messageCheckNetworkRegistration = "AT+CREG?" + CR;

    static String messageIMSI = "AT+CIMI" + CR;
    static String messageLTE = "AT^SYSINFOEX" + CR;
    static String messageCheckNumbers = "AT+CNUM" + CR;
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
        return databaseAPN;
    }

    public void turnSpamOff() {
        writeCommandToModem(messageTurnOffPeriodic);
    }

    public void setAPN(String APN) {
        this.databaseAPN = APN;
    }

    //GOOD
    public String getCSQ() {
        writeCommandToModem(messageCSQ);
        String result = modemResponse;
        if (!result.contains("CSQ")) result = getModemResponse();
        String[] result2 = result.split(":");
        result = result2[1].replaceAll("OK", "");
        return result;
    }

    public String checkIPAddr() {
        String result = null;
        writeCommandToModem(messageIP);
        do {
            result = getModemResponse();
            if (result.contains("ERROR")) return null;
        } while (!result.contains("CGPADDR:"));
        setIPAddress(result);
        return IP;
    }

    public boolean getConnection() {
        return connection;
    }

    public void setIPAddress(String tmp) {
        String[] value = tmp.split("\"");
        String result = value[1];
        System.out.println(result);
        IP = result;
    }

    public String getIPAddress() {
        return IP;
    }

    public String getIP() {
        String result = "";
        if (answer == false) answer = unlockPIN();
        switch (apn_version) {
            case APN_database: {
                {
                    writeCommandToModem(messageEnableRegistration);
                    do {
                        result = getModemResponse();
                        if (result.contains("ERROR")) break;
                    } while (!(result.contains(messageEnableRegistration.trim())));

                    do {
                        writeCommandToModem(messageCheckNetworkRegistration);
                        result = getModemResponse();
                        if (result.contains("ERROR")) break;
                    } while (!result.contains("1,1"));

                    writeCommandToModem(messageAttachGPRSService);
                    result = getModemResponse();
                    while (!result.contains(messageAttachGPRSService.trim())) {
                        result = getModemResponse();
                    }

                    writeCommandToModem(messageDefineContext + databaseAPN + "\"" + CR);
                    result = getModemResponse();
                    result = getModemResponse();

                    writeCommandToModem(messageAskPDPContext);
                    do {
                        result = getModemResponse();
                    } while (!result.contains("1,1"));

                    writeCommandToModem(messageAttachContext);
                    result = getModemResponse();
                    result = checkIPAddr();

                }
                break;
            }
            case APN_Internet: {
                {
                    writeCommandToModem(messageEnableRegistration);
                    do {
                        result = getModemResponse();
                        if (result.contains("ERROR")) break;
                    } while (!(result.contains(messageEnableRegistration.trim())));

                    do {
                        writeCommandToModem(messageCheckNetworkRegistration);
                        result = getModemResponse();
                        if (result.contains("ERROR")) break;
                    } while (!result.contains("1,1"));

                    writeCommandToModem(messageAttachGPRSService);
                    result = getModemResponse();
                    while (!result.contains(messageAttachGPRSService.trim())) {
                        result = getModemResponse();
                    }

                    writeCommandToModem(messageDefineContext + internetAPN + "\"" + CR);
                    result = getModemResponse();
                    result = getModemResponse();
                    //writeCommandToModem(messageDefineContext + internetAPN + "ppp\"" + CR);

                    writeCommandToModem(messageAskPDPContext);
                    do {
                        result = getModemResponse();
                    } while (!result.contains("1,1"));

                    writeCommandToModem(messageAttachContext);
                    result = getModemResponse();
                    result = checkIPAddr();
                    //System.out.println(result);
                }
                break;
            }
            default:
                return null;
        }


        if (result == null) {
            if (apn_version == APN_Version.APN_database)
                apn_version = APN_Version.APN_Internet;
            else apn_version = APN_Version.APN_database;
        }

        return result;
    }

    public boolean unlockPIN() {
        String result = "";
        while (!result.contains("READY")) {
            writeCommandToModem(messageAskPIN);

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
        private byte[] buffer = new byte[1024];

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

    public String getPortName() {
        return this.modemPort;
    }

    class CloseThread extends Thread {
        public synchronized void run() {
            serialPort.close();
            serialPort.removeEventListener();
            if (portId.isCurrentlyOwned()) System.out.println("IN USE IN CLOSE THREAD");
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
        if (connection == false) return null;
        while (!flag) {
            if (modemResponse.endsWith("OK")) break;
            if (modemResponse.contains("ERROR")) break;
            if (modemResponse.contains("NOT SUPPORT")) break;
        }
        flag = false;
        return modemResponse;
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

    public void setSerialNumber(String tmpSerialNumber) {
        this.serialNumber = tmpSerialNumber;
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
        writeCommandToModem(messageAskPIN);
    }

    public void writeCommand(String command) {
        if (connection == false) return;
        writeCommandToModem(command + CR);
    }

    public void checkNumbersOnSIM() {
        if (connection == false) return;
        Thread t1 = (new Thread(new SerialWriter(outputStream, messageCheckNumbers)));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public void checkPDPContext() {
        if (connection == false) return;
        writeCommandToModem(messageAskPDPContext);

    }

    public String messageIP() {
        if (connection == false) return null;
        String result = modemResponse;
        if (result.contains("1,1")) {
            Thread t2 = new Thread(new SerialWriter(outputStream, messageIP));
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return modemResponse;
        }
        return null;
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

        System.out.println("After stream");
        new CloseThread().start();
        return true;
    }

    public void setInternetApn() {
        apn_version = APN_Version.APN_Internet;
    }

    public ModemComm() {
        getAllPorts();
        //apn_version = APN_Version.APN_database;
        apn_version = APN_Version.APN_Internet;
        answer = false;
    }

}
