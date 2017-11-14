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
    String username;
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
    ClientInfo(int clientID, InetAddress address, int port, String username) {
        this.clientID = clientID;
        this.address = address;
        this.port = port;
        this.username = username;
    }

    /**
     * @see java.lang.Object.equals
     */
    @Override
    public boolean equals(Object other) {
        ClientInfo otherInfo = (ClientInfo) other;

        if (otherInfo != null) {
            return address.equals(otherInfo.address) && port == otherInfo.port;
        }

        return false;
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
