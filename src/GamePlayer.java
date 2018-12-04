import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import static SharedVariables.Messages.*;

public class GamePlayer {

    //Program variables
    protected String message = "";

    //logic variables
    boolean startet = false;
    boolean acknowledged = false;
    private boolean running = false;

    //connection variables
    private Socket connection = new Socket();
    //IO Streams
    private DataInputStream receive;
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

        this.addActionListener(readyListener);

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

    public boolean playerStarted(){
        return startet;
    }
    public boolean playerAcknowledged(){ return acknowledged;}

    public void playerStarted(boolean start){
        startet = start;
    }
    public void playerAcknowledged(boolean acknowledge){
        acknowledged = acknowledge;
    }

    //Actionlistener for ready
    private ActionListener readyListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message == playerStartMSG){
                playerStarted(true);
                send(serverStartAck);
            }
        }
    };

    //Get Ack from clients
    private ActionListener ackListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(message == clientAck){
                playerAcknowledged(true);
            }
        }
    };



    //TODO other ActionListeners?
}
