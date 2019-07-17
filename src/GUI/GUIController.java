package GUI;

import App.ModemComm;
import App.MySQLConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GUIController implements Initializable, ActionListener {

    boolean connectionStatus = false;
    ModemComm modemComm;
    MySQLConnection sqlConnection;

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
    public ChoiceBox<String> portCB;

    @FXML
    public ComboBox<String> portComboBox;

    @FXML
    private Button clearButton;

    @FXML
    private Button checkSecurityButton;

    @FXML
    private TextField commandTextField;

    @FXML
    private Button allPortsButton;

    public void show(){
        portCB.show();
    }
    String portName = "";
    ObservableList<String>tmp;


    public void setAllPortsButton(){

        this.portCB.getItems().clear();
        choiceBox =modemComm.getAllSerialPorts();
        tmp = FXCollections.observableArrayList(choiceBox);
        choiceList= FXCollections.observableArrayList(choiceBox);
        System.out.println(choiceList);

        //portCB.getOnShowing();

        /*
        portCB.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            portCB.getSelectionModel().select(tmp.indexOf(newValue));
                            portName = newValue.toString();

                    }
                }
        );
*/
        portCB.setItems(tmp);
       // portCB.getItems().setAll(tmp);
        portCB.getSelectionModel().select(1);
        //portCB.setTooltip(new Tooltip("Select the language"));
        //portCB.setDisable(false);
        portComboBox = new ComboBox<String>(FXCollections.observableArrayList("AA", "BB"));
        portComboBox.getItems().setAll(choiceList);

    }
    //145 - number dont have
    Object[] possibilitiesType = {"145", "129"};
    Object[] PDP_Types = {"IP", "PPP", "IPV6", "IPV4V6"};

    String modemPort = "";

    public void setModemPort(){
        this.modemPort = portCB.getValue();
    }

    //String numberType = JOptionPane.showInputDialog("Write type of number:\n 145 - number dont have + \n 129 - number have", 145);
    //String numberToWrite = JOptionPane.showInputDialog("Write number: \n");
    //int userNumber = Integer.valueOf(JOptionPane.showInputDialog("Write user number ID:", 1));

    ArrayList<String> choiceBox;
    ObservableList choiceList;

    public GUIController(){
        modemComm = new ModemComm();
    }

    public void setPort(){

    }

    public void setModemInfoButton(){
        //this.modemTextArea.setText(modemComm.getModemInfo());
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
        modemTextArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.portCB = new ChoiceBox<String>();
        this.choiceBox = new ArrayList<String>();
        this.modemTextArea= new TextArea();
        this.modemTextArea.setText("AAA");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}

