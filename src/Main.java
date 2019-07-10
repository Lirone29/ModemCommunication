import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

public class Main {

    CommPortIdentifier serialPortId;
    Enumeration enumComm;

    public void test(){


        enumComm = CommPortIdentifier.getPortIdentifiers();

        while(enumComm.hasMoreElements())
        {
            serialPortId = (CommPortIdentifier)enumComm.nextElement();
            if(serialPortId.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                System.out.println(serialPortId.getName());
            }
        }

        System.out.println("Program Finished Sucessfully");
    }


    public static void main(String[] args) {
        ModemComm modemComm = new ModemComm();
    }
}
