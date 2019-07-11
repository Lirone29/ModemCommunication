import gnu.io.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class ModemComm {

    public enum PINSecurity{
        READY, //no password
        PIN,
        PUK,
        PIN2,
        PUK2,
        NONE
    }

    String menu = "-----Choose action----- \n" +
            "1. Read information about modem \n" +
            "2. Request IMSI \n"+
            "3. Ask if PIN | PUCK required \n" +
            "4. Write own commend \n" +
            "5. Check available numbers \n"+
            "6. Write number \n"+
            "7. Clear number \n"+
            "0. EXIT \n";

    //Connection parameters?? Is it necessery to configure???

    int timeout = 2000;

    static Enumeration<CommPortIdentifier> portList;
    static CommPortIdentifier portId;

    static SerialPort serialPort;
    static OutputStream outputStream;
    static InputStream inputStream;

    static String answer = "";
    static String IMSI = "";
    PINSecurity pinSecurity = PINSecurity.NONE;

    //PORT NAME
    static String modemPort = "COM13";

    static String PIN = "";
    static String Puck = "";
    static int phoneBookUserNumber = 1;
   // static int phoneBookNumber = 1;
    String phoneNumber = "+48000000000";

    //Messages:

    static String CR = "\r\n";
    String ownMessage = "";
    static String messageModemId = "ATI" +CR;
    static String messageAskPIN = "AT+CPIN?" + CR;

    static String messageWritePIN = "AT+CPIN=" +CR;
    //Identyfikacja karty SIM przy pomocy IMSI
    static String messageIMSI = "AT+CIMI" + CR;

    //"P2" SIM PIN2
    //"SC" SIM card (if this parameter is set, MT will request the password during startup)
    // change PIN - AT+CPWD="SC","9999","1234" || "AT+CPIN=\"0000\"";

    static String messageEnterPIN = "AT+CPIN=";
    static String messageSelectOperator = "AT+COPS" + CR;
    static String messageRegisterNetwork = "AT+CREG" + CR;
    static String messageChangePassword = "AT+CPWD" + CR;

    //returns all numbers Currently written in PhoneBook| the MSISDN
    static String messageCheckNumbers = "AT+CNUM" + CR;
    //DONE
    static String messageSignalQuality = "AT+CSQ" +CR;
    static String messageSelectPhonebookMemoryStorage = "AT+CPBS=\"ON\"" +CR;
    static String messageWritePhonebook = "AT+CPBW=";

    //read all available ports
    public static void getAllPorts() {
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portIdentifier = portList.nextElement();
            System.out.println(portIdentifier.getName());
        }
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
                    serialPort = (SerialPort) portId.open(modemPort, timeout);
                    //boudRate = serialPort.getBaudRate();

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

    public static class SerialReader implements SerialPortEventListener {
        private InputStream in;
        private byte[] buffer = new byte[1024];

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void serialEvent(SerialPortEvent arg0) {
            int data;
            String endAnswer = "";
            try {
                answer = "";
                int len = 0;
                while ((data = in.read()) > -1) {
                    if (data == 'O') {
                        endAnswer = String.valueOf(data);
                    }
                    if (data == 'K' && endAnswer.equals("O")) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                    answer += String.valueOf((byte) data);
                }
                System.out.print(new String(buffer, 0, len));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

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
               // synchronized(this.out);
                while ((i) < (c.length)) {
                        this.out.write(c[i]);
                        //System.out.print(String.valueOf(c[i]));
                        i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    //setting up modem as current port
    public void setModemPort() {
        CommPortIdentifier tmpPortId;
        portList = null;
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            tmpPortId = (CommPortIdentifier) portList.nextElement();
            if (tmpPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (tmpPortId.getName().equals(modemPort)) {
                    portId = tmpPortId;
                    tmpPortId = null;
                    return;
                }
            }
        }
    }

    public void writePhoneNumber(BufferedReader readerTmp) throws IOException {
        BufferedReader reader = readerTmp;
        Thread t1 = new Thread(new SerialWriter(outputStream, messageSelectPhonebookMemoryStorage));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Write phone number!");
        phoneNumber = reader.readLine();
        //145 - whe number has +48/ other country number in iths cypher
        //129 - when number don't have + sign
        int type = 145;
        System.out.println("Set text for user - alias/name");
        String text = String.valueOf(reader.readLine());
        phoneNumber = "+48" +phoneNumber;
        String mesWritePhonebook = messageWritePhonebook += phoneBookUserNumber + ",\"" + phoneNumber + "\"," + type + ",\"" + text + "\"" +CR;

        Thread t2 = (new Thread(new SerialWriter(outputStream, mesWritePhonebook)));
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void clearNumber() {

        Thread t1 = new Thread(new SerialWriter(outputStream, messageSelectPhonebookMemoryStorage));
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread t2 = new Thread(new SerialWriter(outputStream, "AT+CPBW=" + phoneBookUserNumber + CR));
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void checkPhoneNumber() {
        (new Thread(new SerialWriter(outputStream, messageWritePhonebook + "?"))).start();
    }

    public void menuFunction() throws IOException {
        String tmp;
        String readLine = "";
        boolean answer = true;
        int menuChoose = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (answer != false) {
            menuChoose = -1;
            readLine = "";
            System.out.println(menu);
            try {
                while (readLine.equals("")) {
                    readLine = reader.readLine();
                    if(readLine.equals(""))continue;
                    if (Integer.parseInt(readLine) < 0 || Integer.parseInt(readLine) > 10) {
                        System.out.println("Wrong number given! Try again.");
                        continue;
                    } else menuChoose = Integer.parseInt(readLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (menuChoose) {
                case 0: {
                    answer = false;
                    break;
                }
                case 1: {
                    (new Thread(new SerialWriter(outputStream, messageModemId))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 2:{
                    (new Thread(new SerialWriter(outputStream, messageIMSI))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 3: {
                    (new Thread(new SerialWriter(outputStream, messageAskPIN))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 4: {
                    System.out.println("Write Command: \n");
                    ownMessage = reader.readLine() + CR;
                    (new Thread(new SerialWriter(outputStream, ownMessage))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 5:{
                    (new Thread(new SerialWriter(outputStream, messageCheckNumbers))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 6:{
                    writePhoneNumber(reader);
                    tmp = reader.readLine();
                    break;
                }
                case 7:{
                    clearNumber();
                    tmp = reader.readLine();
                    break;
                }
                default: {
                    System.out.println("Error! Wrong number was given!");
                }

            }
        }

    }

    public ModemComm() {

        getAllPorts();
        setModemPort();
        boolean connection = connect();
        if (connection && portId.isCurrentlyOwned()) System.out.println("Connected!");
        else System.out.println("Connection Failed!!!");

        try {
            menuFunction();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
