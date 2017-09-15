package com;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * Created by andrius on 14/09/2017.
 */
public class Utilities {

    private boolean availablePort(String host, int port) {
        boolean available = true;

        Socket socket = null;
        try {
            socket = new Socket(host, port);
            socket.close();
            available = false;
        } catch (IOException e) {
        }
        return available;
    }

    public int getRandomPortToUse() {

        boolean foundAvail = false;
        int portToUse = -0;

        while (!foundAvail) {
            int portFrom = 18080;
            int portTo = 28080;
            portToUse = new Random().nextInt(portTo - portFrom + 1) + portTo;
            foundAvail = availablePort("localhost", portToUse);
        }

        return portToUse;
    }
}
