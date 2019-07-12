package GUI;

import App.ModemComm;
import App.MySQLConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class GUIController {

    boolean connectionStatus = false;
    ModemComm modemComm;
    MySQLConnection sqlConnection;

    @FXML
    private ScrollPane modemScrollPane;

    @FXML
    private Pane appPane;

    @FXML
    private Button executeButton;

    @FXML
    private Button writeNumberButton;

    @FXML
    private TextArea modemTextArea;

    @FXML
    private Button clearNumberButton;

    @FXML
    private Button getSerialNumberButton;

    @FXML
    private Button modemInfoButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button connectButton;

    @FXML
    private Button compareIPButton;

    @FXML
    private Label modemResponseLabel;

    @FXML
    private Label modemLabel;

    @FXML
    private Button displayNumbersButton;

    @FXML
    private ChoiceBox<String> portChoiceBox;

    @FXML
    private ChoiceBox<?> portCB;

    @FXML
    private ComboBox<?> portComboBox;

    @FXML
    private Button clearButton;

    @FXML
    private Button checkSecurityButton;

    @FXML
    private TextField commandTextField;

    //145 - number dont have
    Object[] possibilitiesType = {"145", "129"};
    Object[] PDP_Types = {"IP", "PPP", "IPV6", "IPV4V6"};


    //String numberType = JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have", 145);
    //String numberToWrite = JOptionPane.showInputDialog("Write number: \n");
    //int userNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));

    ArrayList<String> choiceBox;
    ObservableList choiceList;

    public GUIController(){

        choiceBox = new ArrayList<String>();
        modemComm = new ModemComm();
        choiceBox =modemComm.getAllSerialPorts();


        choiceList= FXCollections.observableArrayList(choiceBox);
        /*
        for(int i = 0 ; i < choiceBox.size(); i++){
            choiceList.add(choiceBox.get(i));


        }*/

        System.out.println(choiceList);

        portCB = new ChoiceBox<>();
       // portCB.getItems().add();
        portCB = new ChoiceBox<>(FXCollections.observableArrayList(choiceBox));
        //portCB.getTooltip().setText("Choose Port");
        portComboBox = new ComboBox<String>(choiceList);
        for(int i = 0; i < choiceList.size(); i ++) {
            System.out.println(choiceList.get(i));

           // portComboBox.getItems().setAll(choiceList);
        }

        //portComboBox = new ComboBox<String>(choiceList);
        //portChoiceBox = new ChoiceBox<String>(choiceList);
        //portChoiceBox.setItems(choiceList);

       /* portChoiceBox = ChoiceBoxBuilder.create()
                .items(choiceList)
                .build();
*/
       // portChoiceBox.getSelectionModel()
       //         .selectedItemProperty();

        //portChoiceBox.setValue("B");
    //(choiceList).forEach(n -> portChoiceBox.getItems().add(String.valueOf(choiceList.indexOf(n))));

    }


    public void setModemInfoButton(){
        this.modemTextArea.setText(modemComm.getModemInfo());
    }

    public void setStatusLabel(){
        if(connectionStatus==true) statusLabel.setText("CONNECTED");
        else statusLabel.setText("DISCONECTED");
    }

    public void setWriteNumberButton(){

    }

    public void setExecuteButton(){

    }
    public void setClearNumberButton(){

    }

    public void setConnectButton(){

        setStatusLabel();
    }

    public void setCompareIPButton(){
        System.out.println("IP BUtton");
    }

    public void setGetSerialNumberButton(){

    }

    public void setDisplayNumbersButton(){

    }

    public void setCheckSecurityButton(){

    }

    public void setCommandTextField(){

    }

    public void setClearButton(){
        this.modemTextArea.clear();
    }
}

