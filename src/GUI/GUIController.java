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
    private Button clearButton;

    @FXML
    private ChoiceBox<String> portChoiceBox;

    @FXML
    private Button checkSecurityButton;

    @FXML
    private TextField commandTextField;

    //145 - number dont have
    Object[] possibilitiesType = {"145", "129"};

    //String numberType = JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have", 145);
    //String numberToWrite = JOptionPane.showInputDialog("Write number: \n");
    //int userNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));

    ArrayList choiceBox;
    public GUIController(){
        choiceBox = modemComm.getSerialPortList();
        ObservableList choiceList = FXCollections.observableArrayList();
        (choiceBox).forEach(n -> choiceList.add(choiceBox.indexOf(n)));

        //for(int i = 0 ;i < choiceList.size(); i++){
        //    portChoiceBox.getItems().add(choiceList.indexOf(i));
        //}

        (choiceList).forEach(n -> portChoiceBox.getItems().add(String.valueOf(choiceList.indexOf(n))));
    }

    public void setModemInfoButton(){

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

