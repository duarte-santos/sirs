/* 
SIRS - Project
Contact Tracing

Basic Version (Server) - receive a single pair of <number, key>, not encrypted
*/

package pt.tecnico.contacttracing.server;

import java.net.*;

import com.google.gson.*;


public class Server {
    //private List<InfectedInfo> infectedInfos;

	private static final int MAX_UDP_DATA_SIZE = (64 * 1024 - 1) - 8 - 20;
	private static final int BUFFER_SIZE = MAX_UDP_DATA_SIZE;

    public static void main(String[] args) throws Exception {
		// Arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", Server.class.getName());
			return;
		}
		final int port = Integer.parseInt(args[0]);

		// Create server socket
		DatagramSocket socket = new DatagramSocket(port);
		System.out.printf("Server listening on port %d %n", port);

		// Wait for client packets 
		byte[] buf = new byte[BUFFER_SIZE];
		while (true) {

			// Receive packet
			DatagramPacket clientPacket = new DatagramPacket(buf, buf.length);
			socket.receive(clientPacket);
			
            InetAddress clientAddress = clientPacket.getAddress();
			int clientPort = clientPacket.getPort();
			int clientLength = clientPacket.getLength();
			byte[] clientData = clientPacket.getData();
			System.out.printf("> Received packet from %s:%d! (%d bytes)%n", clientAddress, clientPort, clientLength);

			// Convert message to JSON
			String clientText = new String(clientData, 0, clientLength);
			JsonObject messageJson = JsonParser.parseString​(clientText).getAsJsonObject();

			// Parse JSON
			String number = null, key = null;
			{
				number = messageJson.get("number").getAsString();
				key = messageJson.get("key").getAsString();
			}
			System.out.printf("> Number: %s, Key: %s", number, key);

			// Create response message
			JsonObject responseJson = JsonParser.parseString​("{}").getAsJsonObject();
			{
				responseJson.addProperty("status", "OK");
			}

			// Send response
			byte[] serverData = responseJson.toString().getBytes();
			DatagramPacket serverPacket = new DatagramPacket(serverData, serverData.length, clientPacket.getAddress(), clientPacket.getPort());
			socket.send(serverPacket);
			System.out.printf("> Response packet sent to %s:%d! (%d bytes)%n", clientPacket.getAddress(), clientPacket.getPort(), serverData.length);
		}
	}
}
