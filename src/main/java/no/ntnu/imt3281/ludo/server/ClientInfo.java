package no.ntnu.imt3281.ludo.server;

import java.net.InetAddress;

/**
 * Data structure containing the data the server needs to identify a logged in
 * user
 * 
 * @author Mars
 */
public class ClientInfo {
    int clientID;
    InetAddress address;
    int port;

    /**
     * A constructor
     * 
     * @param clientID
     *            The userID of the connected client
     * @param port
     *            The port the user sends packets through
     * @param address
     *            The clients IP address
     */
    ClientInfo(int clientID, InetAddress address, int port) {
        this.clientID = clientID;
        this.address = address;
        this.port = port;
    }
}
