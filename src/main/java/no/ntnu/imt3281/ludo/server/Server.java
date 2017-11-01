package no.ntnu.imt3281.ludo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JFrame;

/**
 * 
 * This is the main class for the server. **Note, change this to extend other
 * classes if desired.**
 * 
 * @author
 *
 */
public class Server {
    private DatagramSocket socket;

    /**
     * Sets port number and opens the server GUI
     */
    public Server() {

	// TODO don't do this
	JFrame frame = new JFrame("Servers gonna serve");
	frame.setSize(200, 971);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setVisible(true);

	try {
	    socket = new DatagramSocket(9003);
	} catch (SocketException se) {
	    se.printStackTrace();
	    System.exit(1);
	}
    }

    /**
     * Waits until a packet is received, then handles it
     */
    public void waitForPackets() {
	while (true) {
	    try {
		byte[] data = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		socket.receive(receivePacket);

		String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

		if (message.equals("conplz")) {
		    System.out.println("bitch connected, yo");
		}

		sendPacketToClient(receivePacket);
	    } catch (IOException ioe) {
		ioe.printStackTrace();
	    }
	}
    }

    private void sendPacketToClient(DatagramPacket receivePacket) throws IOException {
	DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
		receivePacket.getAddress(), receivePacket.getPort());

	socket.send(sendPacket);
    }

    /**
     * Sets server to wait for packets
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
	Server application = new Server();
	application.waitForPackets();
    }
}
