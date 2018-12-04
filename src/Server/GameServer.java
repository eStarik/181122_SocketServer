package Server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import SharedVariables.GameSelectives;
import SharedVariables.Messages;

import static SharedVariables.Messages.*;

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
            if(gameState == 0) {
                while (!ready) {
                    ready = true;
                    for (GamePlayer c : clients) {
                        if (!c.playerStarted()) {
                            ready = false;
                        }
                    }
                }
                for (GamePlayer c : clients) {
                    c.send(Messages.serverRandTime + GameSelectives.randTime());
                    int buttonCount = GameSelectives.randButtonCount();
                    String buttons = "";
                    for (int i = 0; i >= buttonCount; i++) {
                        buttons = buttons + GameSelectives.getButton() + ",";
                    }
                    c.send(Messages.serverRandButton + buttons);
                }
                gameState = 1;
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
                //TODO nächster Spielabschnitt
            }else if(gameState == 2){
                //If all clients ready -> Start game
                for (GamePlayer c : clients) {
                    c.send(serverStartGame);
                }
                gameState = 3;
            }else if(gameState == 3){
                for (GamePlayer c: clients) {
                    if(c.playerFinished()){
                        c.send(serverClientWon);
                    }
                }
                gameState = 4;
            }else if(gameState == 4){
                for (GamePlayer c: clients) {
                    if(!c.playerFinished()){
                        c.send(serverClientLost);
                    }
                }
                gameState = 5;
            }else if(gameState == 5){
                for (GamePlayer c: clients) {
                    c.send(serverReset);
                    c.reset();
                }
            }

        }
    };

    public void start(){
        this.running = true;

        Thread serverThread = new Thread(){
            public void run(){
                try(ServerSocket server = new ServerSocket(port)) {
                    while (running) {
                        if(gameState == 0) {
                            //Client verbindet sich auf den Server
                            Socket client = server.accept();

                            //Weitere Aktionen mit dem Client
                            GamePlayer p = new GamePlayer(client);
                            p.addActionListener(startListener);
                            clients.add(p);
                            p.start();
                            System.out.println("Derzeitige Anzahl der Verbindungen: " + clients.size() + "\n");
                        }
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
