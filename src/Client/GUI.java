package Client;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

import static SharedVariables.Messages.*;
import static SharedVariables.constants.*;

//
public class GUI extends Application {

    public static Button[] btns = new Button[16];
    private Button startButton = new Button("START");
    private int start = 1;

    public boolean running = false;
    public boolean runagain = false;

    public boolean multiplayer = false;
    public boolean runMultiplayer = false;
    public boolean reset = false;

    public String[] randTimeString;
    public String[] randButtonString;

    public int randTime;
    public int[] randButtons;

    public GameClient player = new GameClient("localhost", 1234);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("ButtonGame v0.1 Ahrer/Kamleitner");

        /**
         * This function initialises the ButtonArray
         */
        initBtnsArray();

        Group root = new Group();

        startButton.setPrefSize(1000, 100);
        startButton.setStyle("-fx-background-color: #90aa00");
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(start == 1) {
                    System.out.println("started");
                    startButton.setText("STOP");
                    start = 0;
                    running = true;

                    if (multiplayer) {
                        Thread multiplayerThread = new Thread() {
                            @Override
                            public void run() {
                                while (runMultiplayer) {
                                    reset = false;
                                    try {
                                        Thread.sleep(randTime);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    int anzButtons = randButtons.length;

                                    for (int i = 0; i <= anzButtons; i++) {
                                        btns[randButtons[i]].setDisable(false);
                                        btns[randButtons[i]].setStyle("-fx-background-color: #00ff00");
                                    }
                                    for (int bt = 0; bt <= 15; bt++) {
                                        if (btns[bt].isDisabled() == false) {
                                            runagain = true;
                                        }
                                    }
                                    while (runagain) {
                                        int btr = 0;
                                        for (int bt = 0; bt <= 15; bt++) {
                                            if (btns[bt].isDisabled()) {
                                                btr++;
                                                if (btr == 16) {
                                                    //runagain = false;
                                                    // send finish
                                                    player.sendMessage(clientFinish);

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        };
                    multiplayerThread.start();
                }
                    Thread gameThread = new Thread(){
                        @Override
                        public void run() {
                            while (running){

                                try {
                                    Thread.sleep(getRandomTime());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                int anzButtons = getRandomAmountOfButtons();

                                for (int i = 0; i <= anzButtons ; i++) {
                                    int selectedNumber = getLineRow()*4 + getLineRow() - 1;
                                    btns[selectedNumber].setDisable(false);
                                    btns[selectedNumber].setStyle("-fx-background-color: #00ff00");
                                }

                                for(int bt = 0; bt <= 15; bt++){
                                    if(btns[bt].isDisabled() == false){
                                        runagain = true;
                                    }
                                }
                                while (runagain){
                                    int btr = 0;
                                    for(int bt = 0; bt <= 15; bt++){
                                        if(btns[bt].isDisabled()){
                                            btr++;
                                            if(btr == 16){
                                                runagain = false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    };
                    gameThread.start();

                } else {
                    System.out.println("stopped");
                    startButton.setText("START");
                    start = 1;
                    running = false;
                }
            }
        });

        BorderPane abc = new BorderPane();
        abc.setCenter(getGrid());
        abc.setBottom(startButton);
        abc.setTop(initSettings());

        root.getChildren().add(abc);

        //Scene scene = new Scene(root, 1000, 500);

        Scene scene = new Scene(root);

        primaryStage.setResizable(false);

        primaryStage.setScene(scene);

        primaryStage.sizeToScene();
        //

        primaryStage.show();
    }

    private Pane getGrid() {
        int i = 0;
        int rowIndex = 1;
        GridPane gridPane = new GridPane();

        for(Button b : btns) {
            b.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    //b.setStyle("-fx-background-color: #dedede");
                    b.setStyle(null);
                    b.setDisable(true);
                }
            });

            if(i%4 == 0){
                rowIndex++;
                i=0;
            }
            gridPane.add(b, i*(i+(int)b.getWidth()), rowIndex);
            i++;
        }
        return gridPane;
    }

    private Pane initSettings(){
        BorderPane settingPane = new BorderPane();
        GridPane radioPane = new GridPane();
        GridPane hostportPane = new GridPane();
        final ToggleGroup buttonGroup = new ToggleGroup();
        RadioButton spButton = new RadioButton("SinglePlayer");
        spButton.setToggleGroup(buttonGroup);
        spButton.setSelected(true);
        RadioButton mpButton = new RadioButton("MultiPlayer");
        mpButton.setToggleGroup(buttonGroup);
        radioPane.add(spButton, 0,0);
        radioPane.add(mpButton, 0,1);
        TextField hostField = new TextField();
        TextField portField = new TextField();
        Label hostLabel = new Label(" Host: ");
        Label portLabel = new Label(" Port: ");
        settingPane.setLeft(radioPane);
        hostportPane.add(hostLabel,0,0);
        hostportPane.add(hostField,1,0);
        hostportPane.add(portLabel,2,0);
        hostportPane.add(portField,3,0);
        Button okButton = new Button("OK");
        hostField.setDisable(true);
        portField.setDisable(true);
        okButton.setDisable(true);
        settingPane.setCenter(hostportPane);
        settingPane.setRight(okButton);
        buttonGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(spButton.isSelected() == true){
                    hostField.setDisable(true);
                    portField.setDisable(true);
                    okButton.setDisable(true);
                } else if (mpButton.isSelected() == true){
                    hostField.setDisable(false);
                    portField.setDisable(false);
                    okButton.setDisable(false);
                }
            }
        });
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                multiplayer = true;

                player.host = hostField.getText();
                player.port = Integer.valueOf(portField.getText());
                player.start();

                portField.setDisable(true);
                hostField.setDisable(true);
                okButton.setDisable(true);
            }
        });

        return settingPane;
    }

    private void initBtnsArray() {
        for(int i = 0; i < btns.length; i++) {
            btns[i] = new Button("Button");
            btns[i].setPrefSize(250, 100);
            btns[i].setDisable(true);
        }
    }

    public static int getRandomTime(){
        Random random = new Random();
        int randomNumber = random.nextInt(MAX_TIME_VALUE - MIN_TIME_VALUE) + MIN_TIME_VALUE;
        return(randomNumber);
    }

    protected int getRandomAmountOfButtons(){
        Random random = new Random();
        int randomNumber = random.nextInt(MAX_BUTTONS - MIN_BUTTONS) + MIN_BUTTONS;
        return(randomNumber);
    }

    protected int getLineRow(){
        Random random = new Random();
        int randomNumber = random.nextInt((int)Math.sqrt(BUTTON_COUNT) - 1) + 1;
        return(randomNumber);
    }

    public void randTimeReceive(String massage){
        System.out.println("RandomTime received");
        randTimeString = massage.split(",");
        randTime = Integer.valueOf(randTimeString[1]);
    }

    public void randButtonReceive(String message){
        System.out.println("RandomButton received");
        randButtonString = message.split(",");
        int index = 0;
        for(int i = 1; i < randButtonString.length; i++){
            index++;
            // Array with the buttonnumbers to enable
            randButtons[index] = Integer.valueOf(randButtonString[i]);
        }
        player.sendMessage(clientAck);
    }

    public void startGame(String message){
        runMultiplayer = true;
    }

    public void otherMessage(String message) {
        if (message.equals(serverClientWon)) {
            System.out.println("You won :)");
        } else if (message.equals(serverClientLost)) {
            System.out.println("You lost :(");
        } else if (message.equals(serverReset)) {
            for (int i = 0; i <= 15; i++) {
                btns[i].setDisable(true);
                btns[i].setStyle(null);
            }
            reset = true;
        }
    }
}