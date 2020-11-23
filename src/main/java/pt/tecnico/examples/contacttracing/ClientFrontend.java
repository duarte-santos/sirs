package pt.tecnico.examples.contacttracing;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.examples.contacttracing.*;

/**
 * Encapsulates gRPC channel and stub for remote service. 
 * All remote calls from the client should use this object.
 */
public class ClientFrontend implements AutoCloseable {
	private ManagedChannel channel;
	private ContactTracingGrpc.ContactTracingBlockingStub stub;
	
	public ClientFrontend (String host, int port) {	 
        // Channel is the abstraction to connect to a service endpoint
        // Let us use plaintext to avoid using TLS (default configuration of ManagedChannel)
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        // Create a blocking stub
        stub = ContactTracingGrpc.newBlockingStub(channel);
	}
	
	/**
	 * Handles client 'registerInfected' remote call.
	**/
	public RegisterInfectedResponse registerInfected(RegisterInfectedRequest request) {
		return stub.registerInfected(request);
	}

	/**
	 * Handles client 'get_infected' remote call.
	**/
	public GetInfectedResponse getInfected(GetInfectedRequest request) {
		return stub.getInfected(request);
	}

	@Override
	public void close() {
		channel.shutdown();	
	}

}
