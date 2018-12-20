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
    public static boolean runMultiplayer = false;
    public boolean reset = false;

    public static String[] randTimeString;
    public static String[] randButtonString;

    public static int randTime;
    public static int[] randButtons;

    public static GameClient player = new GameClient("localhost", 1234);

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
                    //running = true;

                    if (multiplayer) {
                        player.start();
                        player.sendMessage(playerStartMSG);

                        Thread multiplayerThread = new Thread() {
                            @Override
                            public void run() {
                                while (runMultiplayer) {

                                    reset = false;
                                    try {
                                        Thread.sleep(randTime);
                                        System.out.println("randTime");
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

        Scene scene = new Scene(root);

        primaryStage.setResizable(false);

        primaryStage.setScene(scene);

        primaryStage.sizeToScene();

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
        GridPane hostportPane = new GridPane();
        settingPane.setLeft(radioPane);
        hostportPane.add(hostLabel,0,0);
        hostportPane.add(hostField,1,0);
        hostportPane.add(portLabel,2,0);
        hostportPane.add(portField,3,0);
        Button okButton = new Button("OK");
        hostField.setDisable(true);
        portField.setDisable(true);
        settingPane.setCenter(hostportPane);
        settingPane.setRight(okButton);
        buttonGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(spButton.isSelected() == true){
                    hostField.setDisable(true);
                    portField.setDisable(true);
                } else if (mpButton.isSelected() == true){
                    hostField.setDisable(false);
                    portField.setDisable(false);

                }
            }
        });
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if(spButton.isSelected()){
                    running = true;
                } else if(mpButton.isSelected()){
                    multiplayer = true;

                    player.host = hostField.getText();
                    player.port = Integer.valueOf(portField.getText());
                    //player.start();
                    //player.sendMessage(playerStartMSG);


                }

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

}