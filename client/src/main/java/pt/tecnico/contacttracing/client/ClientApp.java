package pt.tecnico.contacttracing.client;

import io.grpc.StatusRuntimeException;
import pt.tecnico.contacttracing.grpc.*;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientApp {
	private static final String CLIENT_USAGE = "Usage: java %s host port%n";

    /** Buffer size for receiving a UDP packet. */
	private static final int BUFFER_SIZE = 65_507;

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

		System.out.println("\nType a command\n");

		do{
			try {
				System.out.print("> ");
				
				/* read next command */
				command = scanner.next();

				/********* infected ********/
				if(command.equals("infected")){
					/* send request */
					InfectedRequest request = InfectedRequest.newBuilder().setNumber(1337).setKey(123456789).build();
					InfectedResponse response = frontend.infected(request);
					
					/* print feedback */
					System.out.println("> Infected info stored");
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

}
