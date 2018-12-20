package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class GameClient {

    public String host;
    public int port;

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

}

