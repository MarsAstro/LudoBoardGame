/**
 * 
 */
package no.ntnu.imt3281.ludo.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author Marius
 *
 */
public class WaitForPacketTask implements Runnable {

    private DatagramSocket socket;

    /**
     * A constructor
     * 
     * @param socket
     *            The socket connected to server
     */
    public WaitForPacketTask(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Run
     */
    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receivePacket = new DatagramPacket(data,
                        data.length);

                socket.receive(receivePacket);

                handleReceivedPacket(receivePacket);

            } catch (IOException ioe) {
                // LOGGER.warning(ioe.getMessage());
            }
        }
    }

    /**
     * Handles received packet
     * 
     * @param receivePacket
     *            The received packet
     */
    private void handleReceivedPacket(DatagramPacket receivePacket) {
        System.out.println(new String(receivePacket.getData(), 0,
                receivePacket.getLength()));

    }
}
