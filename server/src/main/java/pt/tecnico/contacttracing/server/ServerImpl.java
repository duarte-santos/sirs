
/* ====================================================================== */
/* ====[                   SIRS - Contact Tracing                   ]==== */
/* ====[                   Server - Basic Version                   ]==== */
/* ====================================================================== */

// status - receive a single pair of <number, key>, not encrypted

package pt.tecnico.contacttracing.server;

import java.time.Instant;
import pt.tecnico.contacttracing.grpc.*;
import io.grpc.stub.StreamObserver;


public class ServerImpl extends ContactTracingGrpc.ContactTracingImplBase {

	/* Domain implementation */
	private Storage storage = new Storage();
	
	/* ====================================================================== */
    /* ====[                       NEW_INFECTED                         ]==== */
    /* ====================================================================== */
    
	@Override
    public void infected(InfectedRequest request, StreamObserver<InfectedResponse> responseObserver) {
		/* save time of reception */
		Instant timestamp = Instant.now(); 

		System.out.printf("%nReceived new infected user!%n");

		/* parse client message */
		int number = request.getNumber();
		int key = request.getKey();

		System.out.printf("Content: number %d, key %d%n", number, key);

		storage.storeInfectedData(number, key, timestamp);

		/* send response */
		InfectedResponse response = InfectedResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

		System.out.printf("Sent response to infected user!%n");
	}
}
