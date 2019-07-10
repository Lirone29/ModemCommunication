import gnu.io.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class ModemComm {

    String menu = "Choose action \n" +
            "1. Read information about modem \n" +
            "2. Ask if PIN | PUCK requiered \n" +
            "3. Write own commend \n " +
            "0. EXIT \n";

    //Connection parameters?? Is it necessery to configure???

    int timeout = 2000;
    static Enumeration<CommPortIdentifier> portList;
    static CommPortIdentifier portId;

    static SerialPort serialPort;
    static OutputStream outputStream;
    static InputStream inputStream;

    int boudRate;

    //PORT NAME
    static String modemPort = "COM13";

    static int phoneBookNumber = 1;
    String phoneNumber = "+48000000000";

    //Messages
    String ownMessage = "";
    static String messageModemId = "ATI\r\n";
    static String messageAskPIN = "AT+CPIN?\r\n";
    static String messageEnterPIN = "AT+CPIN=";
    static String messageSelectOperator = "AT+COPS\r\n";
    static String messageRegisterNetwork = "AT+CREG\r\n";
    static String messageChangePassword = "AT+CPWD\r\n";

    //returns all numbers Currently written in PhoneBook| the MSISDN
    static String messageSubscriberNumber = "AT+CNUM\r\n";
    static String messageSignalQuality = "AT+CSQ\r\n";
    static String messageSelectPhonebookMemoryStorage = "AT+CPBS=\"ON\"\r\n";
    static String messageWritePhonebook = "AT+CPBW=";


    static String messageString2 = "AT+CPIN=\"7078\"";
    static String CR = "\r\n";

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
                    boudRate = serialPort.getBaudRate();

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
            String answer = "";
            try {
                int len = 0;
                while ((data = in.read()) > -1) {
                    if (data == 'O') {
                        answer = String.valueOf(data);
                    }
                    if (data == 'K' && answer.equals("O")) {
                        break;
                    }
                    buffer[len++] = (byte) data;
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
                while ((i) < (c.length)) {
                    this.out.write(c[i]);
                    System.out.print(c[i]);
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print("Thread ended");
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
        (new Thread(new SerialWriter(outputStream, messageModemId))).start();
        System.out.println("Write phone number!");
        phoneNumber = reader.readLine();
        //145 - whe number has +48/ other country number in iths cypher
        //129 - when number don't have + sign
        int type = 145;
        System.out.println("Set text for user - alias/name");
        String text = String.valueOf(reader.readLine());
        String mesWritePhonebook = messageWritePhonebook += phoneNumber + ",\"+48" + phoneNumber + "\"," + type + "\",\"" + text + "\"";
        (new Thread(new SerialWriter(outputStream, messageWritePhonebook))).start();

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
                case 2: {
                    (new Thread(new SerialWriter(outputStream, messageAskPIN))).start();
                    tmp = reader.readLine();
                    break;
                }
                case 3: {
                    ownMessage = reader.readLine() + CR;
                    (new Thread(new SerialWriter(outputStream, ownMessage))).start();
                    tmp = reader.readLine();
                    break;
                }
                default: {
                    System.out.println("Error! Wrong number was given!");
                }

            }
        }

    }

    public void writeCommand(String command) {
        byte[] b = messageModemId.getBytes(StandardCharsets.UTF_8);
        try {
            int i = 0;
            byte[] c = b;
            while ((i) < (c.length)) {
                outputStream.write(c[i]);
                System.out.println(c[i]);
                i++;

            }
        } catch (IOException e) {
            e.printStackTrace();
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
