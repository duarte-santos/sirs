package pt.tecnico.contacttracing.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.Scanner;
import java.io.IOException;

public class ServerApp {
	private static final String SERVER_USAGE = "Usage: java %s port%n";
	private static Server server;	
	
	/* Main */
	public static void main(String[] args) {
        System.out.println("\n[" + ServerApp.class.getSimpleName() + "]");

		/* Check arguments amount */
		if (args.length < 1) {
			System.err.println("Invalid amount of arguments!");
			System.err.printf(SERVER_USAGE, ServerApp.class.getName());
			return;
		}
        
        final int port;

		try {
            /* Initialize arguments */
            port = Integer.parseInt(args[0]);
			
			/* Create server implementation */
			ServerImpl serverImpl = new ServerImpl();
			final BindableService impl = (BindableService) serverImpl;
			
			/* Create and start the new server to listen on port - server threads running in background */
			server = ServerBuilder.forPort(port).addService(impl).build();	
			server.start();
			System.out.printf("Server listening on port %d %n", port);
			
			/* Register an instance of 'Finalize' as shutdown hook */
			Runtime.getRuntime().addShutdownHook( new Finalize() );
			
			/* Create scanner to wait for input - exit when 'enter' is pressed */
			System.out.println("<Press enter to shutdown>");					
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
			scanner.close();
			
			Runtime.getRuntime().exit(0);
			
		} catch (NumberFormatException e) { 
			/* error parsing integer arguments */
			System.out.println("Argument(s) of invalid type!");
			System.out.printf(SERVER_USAGE, ServerApp.class.getName());
		
		} catch (IOException e) {
			System.out.println("Caught exception with description: " + e.getMessage());
		}
		
    }
    	
	/* Class called when the program is exiting - used as shutdown hook */
	static class Finalize extends Thread {

		public void run() {
			server.shutdown();
			System.out.printf("Closing...%n");
		}
		
	}	
		
}
