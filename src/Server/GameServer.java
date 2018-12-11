package Server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import SharedVariables.GameSelectives;
import SharedVariables.Messages;

import static SharedVariables.Messages.*;
import static SharedVariables.constants.MAX_BUTTONS;
import static SharedVariables.constants.MIN_BUTTONS;

public class GameServer {
    private boolean ready = false;
    private boolean clientAck = false;
    private int port;
    private boolean running = false;
    private ArrayList<GamePlayer> clients = new ArrayList<>();
    private int gameState = 0;

    public GameServer(int port){
        this.port = port;

    }

    private ActionListener startListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            //sende Nachricht an alle Clients die dem Server derzeit bekannt sind
        }
    };

    public void gameLogic(){
        Thread gameLogicThreat = new Thread(){
            @Override
            public void run() {
                if(gameState == 0) {
                    while (!ready) {
                        ready = true;
                        for (GamePlayer c : clients) {
                            if (!c.playerStarted()) {
                                ready = false;
                            }
                        }
                    }
                    System.out.println("All players accepted");            //Debug show all clients accpeted
                    //Get random time
                    int randTime = GameSelectives.randTime();
                    //get random buttons
                    String buttons = "";
                    int buttonCount = GameSelectives.randButtonCount();
                    for (int i = 0; i <= buttonCount; i++) {
                        buttons = buttons + GameSelectives.getButton() + ",";
                    }
                    for (GamePlayer c : clients) {
                        c.send(Messages.serverRandTime + randTime);
                        c.send(Messages.serverRandButton + buttons);
                    }
                    gameState = 1;
                    System.out.println("All players values sent");            //Debug show all clients ready values sent
                }else if(gameState == 1){
                    while(!clientAck){
                        clientAck = true;
                        for (GamePlayer c : clients) {
                            if (!c.playerAcknowledged()) {
                                clientAck = false;
                            }
                        }
                    }
                    gameState = 2;
                    System.out.println("All players acknowledge");             //Debug show all clients have received there values
                }else if(gameState == 2){
                    //If all clients ready -> Start game
                    for (GamePlayer c : clients) {
                        c.send(serverStartGame);
                    }
                    gameState = 3;
                    System.out.println("game started");                     //Debug show game started in console
                }else if(gameState == 3){
                    //client has won -> send won
                    for (GamePlayer c: clients) {
                        if(c.playerFinished()){
                            c.send(serverClientWon);
                        }
                    }
                    gameState = 4;
                    System.out.println("A player was sent \"winner\"");     //Debug show "winner" sent
                }else if(gameState == 4){
                    //all clients who haven't won -> send loose
                    for (GamePlayer c: clients) {
                        if(!c.playerFinished()){
                            c.send(serverClientLost);
                        }
                    }
                    gameState = 5;
                    System.out.println("A player was sent \"looser\"");      //Debug show "looser" sent
                }else if(gameState == 5){
                    //send start new game -> clients should reset
                    for (GamePlayer c: clients) {
                        c.send(serverReset);
                        c.reset();
                    }
                    System.out.println("restart Game");                     //Debug show game restart sent
                }
            }
        };
        gameLogicThreat.start();
    }

    public void start(){
        this.running = true;

        Thread serverThread = new Thread(){
            public void run(){
                try(ServerSocket server = new ServerSocket(port)) {
                    while (running) {
                        //Client verbindet sich auf den Server
                        if (gameState == 0) {
                            Socket client = server.accept();
                            //Weitere Aktionen mit dem Client
                            GamePlayer p = new GamePlayer(client);
                            p.addActionListener(startListener);
                            clients.add(p);
                            p.start();
                            System.out.println("Derzeitige Anzahl der Verbindungen: " + clients.size() + "\n");
                        }
                        gameLogic();
                    }
                }catch (Exception e){

                }
            }
        };
        serverThread.start();
    }

    public void stop(){
        this.running = false;

    }

    public static void main(String[] args) {
        GameServer server = new GameServer(1234);
        server.start();
    }
}
