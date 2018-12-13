package Client;

import Server.GamePlayer;
import Client.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import static SharedVariables.Messages.*;
import static SharedVariables.Messages.playerStartMSG;

public class GameClient {

    private String host;
    private int port;

    private GamePlayer connection;

    private ArrayList<ActionListener> listeners = new ArrayList<>();


    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

        try {
            connection = new GamePlayer(new Socket(host, port));

            for(ActionListener listener : listeners) {
                connection.addActionListener(listener);
            }

            connection.start();

        } catch (IOException e) {
        }
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }

    public void stop() {

        if (connection != null) {
            connection.stop();
        }
    }

    public void sendMessage(String message) {
        connection.send(message); //sendMessage
    }

    public static void main(String[] args) {
        //create a new instance of chatclient object
        //We need to define the host and the port, our server is running
        GameClient client = new GameClient("localhost", 1234);
        client.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
            }
        });

        client.start();
        Scanner input = new Scanner(System.in);
        while (client.isConnected()) {
            client.sendMessage(input.nextLine());
            try{
                Thread.sleep((long) 10);
            }catch(Exception e){

            }
        }
    }

}

