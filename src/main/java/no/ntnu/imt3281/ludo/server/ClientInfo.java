package no.ntnu.imt3281.ludo.server;

import java.net.Socket;

/**
 * Data structure containing the data the server needs to identify a logged in
 * user
 * 
 * @author Mars
 */
public class ClientInfo {
    Socket connection;
    int clientID;
    String username;

    /**
     * Constructor for temporary objects used for searching
     * 
     * @param clientID
     *            ID is the only parameter relevant for equals operator
     */
    ClientInfo(int clientID) {
        this.clientID = clientID;
    }

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
    ClientInfo(Socket connection, int clientID, String username) {
        this.clientID = clientID;
        this.connection = connection;
        this.username = username;
    }

    /**
     * @see java.lang.Object.equals
     */
    @Override
    public boolean equals(Object other) {
        boolean isEqual = false;

        ClientInfo otherInfo = (ClientInfo) other;
        if (otherInfo != null) {
            isEqual = clientID == otherInfo.clientID;
        }

        return isEqual;
    }

    /**
     * Equals check on username
     * 
     * @param username
     *            The name of the user to check against
     * @return True if username is equal
     */
    public boolean userEquals(String username) {
        return this.username.equals(username);
    }
}
