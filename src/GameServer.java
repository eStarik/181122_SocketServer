import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import SharedVariables.GameSelectives;
import SharedVariables.Messages;

import javax.swing.*;

import static SharedVariables.Messages.playerStartMSG;
import static SharedVariables.Messages.serverStartAck;
import static SharedVariables.Messages.serverStartGame;

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
                /* From ChatServer
                if(!c.equals(actionEvent.getSource())) {
                    c.send(actionEvent.getActionCommand());
                }
                */
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
                //If all clients ready -> Start game
                for (GamePlayer c : clients) {
                    c.send(serverStartGame);
                }
                //TODO nächster Spielabschnitt
            }
        }
    };

    public void start(){
        this.running = true;

        Thread serverThread = new Thread(){
            public void run(){
                try(ServerSocket server = new ServerSocket(port)) {
                    while (running) {
                        //Client verbindet sich auf den Server
                        Socket client = server.accept();

                        //Weitere Aktionen mit dem Client
                        GamePlayer p = new GamePlayer(client);
                        p.addActionListener(startListener);
                        clients.add(p);
                        p.start();
                        System.out.println("Derzeitige Anzahl der Verbindungen: "+clients.size()+"\n");
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
