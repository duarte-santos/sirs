package pt.tecnico.contacttracing.server.client;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.*;

public class Client {

    /** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = 65_507;

	public static void main(String[] args) throws IOException {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", Client.class.getName());
			return;
		}
		final String serverHost = args[0];
		final InetAddress serverAddress = InetAddress.getByName(serverHost);
		final int serverPort = Integer.parseInt(args[1]);

		// Create socket
		DatagramSocket socket = new DatagramSocket();

		/* Json:
			{
				"number": "...",
				"key": "..."
			}
		*/

        // Create request message
		JsonObject requestJson = JsonParser.parseString​("{}").getAsJsonObject();
		{
			String numberText = "1337";
			requestJson.addProperty("number", numberText);

			String keyText = "123456789";
			requestJson.addProperty("key", keyText);
		}
		System.out.println("Request message: " + requestJson);

		// Send request
		byte[] clientData = requestJson.toString().getBytes();
		System.out.printf("%d bytes %n", clientData.length);
		DatagramPacket clientPacket = new DatagramPacket(clientData, clientData.length, serverAddress, serverPort);
		socket.send(clientPacket);
		System.out.printf("Request packet sent to %s:%d!%n", serverAddress, serverPort);

		// Receive response
		byte[] serverData = new byte[BUFFER_SIZE];
		DatagramPacket serverPacket = new DatagramPacket(serverData, serverData.length);
		System.out.println("Wait for response packet...");
		socket.receive(serverPacket);
		System.out.printf("Received packet from %s:%d!%n", serverPacket.getAddress(), serverPacket.getPort());
		System.out.printf("%d bytes %n", serverPacket.getLength());

		// Convert response to string
		String serverText = new String(serverPacket.getData(), 0, serverPacket.getLength());
		System.out.println("Received response: " + serverText);

		/* Json:
			{
				"status": "OK"
			}
		*/

		// Parse JSON and extract arguments
		JsonObject responseJson = JsonParser.parseString​(serverText).getAsJsonObject();
		String status = null;
		{
			status = responseJson.get("status").getAsString();
		}
		System.out.printf("Message from '%s':%n%s%n", status);

		// Close socket
		socket.close();
		System.out.println("Socket closed");
	}

}
