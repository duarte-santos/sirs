package pt.tecnico.examples.contacttracing;

import io.grpc.StatusRuntimeException;
import pt.tecnico.examples.contacttracing.*;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.time.Instant;
import com.google.protobuf.Timestamp;

import java.util.List;

public class ClientApp {
	private static final String CLIENT_USAGE = "Usage: java %s host port%n"; 

	public static void main(String[] args) {
		/* print client name */
		System.out.println("\n[" + ClientApp.class.getSimpleName() + "]");

		/* check arguments amount */
		if (args.length < 2) {
			System.out.println("Invalid amount of arguments!");
			System.err.printf(CLIENT_USAGE, ClientApp.class.getName());
			return;
		}

		final String serverHost;
		final int serverPort;
		
		/* check arguments type and initialize arguments */
		try {
			serverHost = args[0];
			serverPort = Integer.parseInt(args[1]);
		
		} catch (NumberFormatException e) {  /* the given instance number is not integer */
			System.out.println("Argument(s) of invalid type!");
			System.out.printf(CLIENT_USAGE, ClientApp.class.getName());
			return;
		}

		/* create frontend and run client */
		try(ClientFrontend frontend = new ClientFrontend(serverHost, serverPort)) {
			run(frontend);
		
		} finally {
			System.out.printf("Closing...%n");
		}
	}

	/* run client */
	private static void run(ClientFrontend frontend){
		String command;
		Scanner scanner = new Scanner(System.in);
		
		Instant lastUpdate = Instant.now();

		System.out.println("\nType a command\n");

		do{
			try {
				System.out.print("> ");
				
				/* read next command */
				command = scanner.next();

				/********* infected ********/
				if (command.equals("infected")){
					/* send request */
					RegisterInfectedRequest request = RegisterInfectedRequest.newBuilder().setNumber(133744).setKey(123456789).build();
					RegisterInfectedResponse response = frontend.registerInfected(request);
					
					/* print feedback */
					System.out.println("Infected info stored");
				}

				/******* get_infected ******/
				else if(command.equals("get_infected")){
					/* send request */
					GetInfectedRequest request = GetInfectedRequest.newBuilder()
												.setLastUpdate( instantToTimestamp(lastUpdate) )
												.build();
					GetInfectedResponse response = frontend.getInfected(request);
					
					/* set lastUpdate to current time */
					lastUpdate = Instant.now();

					/* print response */
					List<Infected> new_data = response.getInfectedList();
					
					/* no updates */
					if (new_data.size() == 0) System.out.println("No new infected data available");
					
					/* new updates */
					else  {
						System.out.println("New infected data received:");
						for (Infected data : new_data)
							System.out.printf("- Number: %s, Key: %s%n", data.getNumber(), data.getKey());
					}
	

				}

				/*********** exit ***********/
				else if (command.equals("exit")){
					break;
				}
				
				/********** other ***********/
				else {
					System.out.println("> Unknown command");
				}
				
				System.out.println();
	
		    } catch (StatusRuntimeException e) {
		    	/* exception received from server */
		    	System.out.println("> Caught exception with description: " + 
		            e.getStatus().getDescription() + "\n");
		    }
		
		} while (true);
		
		scanner.close();
		
	}

	private static Timestamp instantToTimestamp(Instant instant) {
		/* setup grpc timestamp */
		Timestamp timestamp = Timestamp.newBuilder()
								.setSeconds(instant.getEpochSecond())
								.setNanos(instant.getNano())
								.build();
		return timestamp;
	}

}
