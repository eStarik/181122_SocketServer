package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import Client.*;

import static SharedVariables.Messages.*;

public class GamePlayer {

    //Program variables
    protected String message = ".";

    //logic variables
    private boolean startet = false;
    private boolean running = false;

    //connection variables
    private Socket connection = new Socket();
    //IO Streams
    public DataInputStream receive;
    private DataOutputStream transmit;

    private ArrayList<ActionListener> listener = new ArrayList<>();

    public GamePlayer(Socket player){
        this.connection = player;
    }

    public void start(){
        this.running = true;

        try{
            //Initialize the data Streams
            this.receive = new DataInputStream(this.connection.getInputStream());
            this.transmit = new DataOutputStream(this.connection.getOutputStream());
        } catch (Exception e) {
        }

        this.addActionListener(ackListener);
        this.addActionListener(randTimeListener);
        this.addActionListener(randButtonsListener);
        this.addActionListener(elseListener);


        Thread t = new Thread(){
            @Override
            public void run() {
                while (running){
                    try {
                        message = receive.readUTF();
                        //verarbeite Nachtricht!
                        notifyListener(message);
                    } catch (Exception e) {
                    }
                }
            }
        };
        t.start();
    }

    public void send(String message){
        if(this.transmit != null) {
            try {
                transmit.writeUTF(message);
                transmit.flush();
            } catch (IOException e) {
            }
        }
    }

    public void stop(){
        this.running = false;

    }

    public void notifyListener (String message){
        for(ActionListener l : listener){
            l.actionPerformed(new ActionEvent(this,0,message));
        }
    }

    public void addActionListener(ActionListener l){
        this.listener.add(l);
    }

    //Get Ack from Server
    private ActionListener ackListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message.equals(serverStartAck)){

            }
        }
    };

    //Get Random Time
    private ActionListener randTimeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message.contains(serverRandTime)){
                GUI.randTimeReceive(message);
            }
        }
    };

    //Get Random Time
    private ActionListener randButtonsListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message.contains(serverRandButton)){
                GUI.randButtonReceive(message);
            }
        }
    };

    //
    private ActionListener serverStartGameListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message.equals(serverStartGame)){
                GUI.randButtonReceive();
            }
        }
    };

    //
    private ActionListener elseListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(!message.equals(serverStartGame)&& !message.contains(serverRandTime) && !message.contains(serverRandButton) && !message.equals(serverStartAck)){
                GUI.otherMessage(message);
            }
        }
    };

    public void reset(){
        startet = false;
        running = false;
        message = "";
    }

    public boolean isConnected(){
        return running;
    }
}
