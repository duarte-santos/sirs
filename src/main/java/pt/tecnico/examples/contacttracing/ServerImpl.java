
/* ====================================================================== */
/* ====[                   SIRS - Contact Tracing                   ]==== */
/* ====[                   Server - Basic Version                   ]==== */
/* ====================================================================== */

// status - receive a single pair of <number, key>, not encrypted

package pt.tecnico.examples.contacttracing;

import java.time.Instant;
import java.util.List;

import pt.tecnico.examples.contacttracing.*;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.Timestamp;


public class ServerImpl extends ContactTracingGrpc.ContactTracingImplBase {

	/* Domain implementation */
	private Storage storage = new Storage();
	
	/* ====================================================================== */
    /* ====[                       NEW_INFECTED                         ]==== 	*/
    /* ====================================================================== */
    
	@Override
    public void registerInfected(RegisterInfectedRequest request, StreamObserver<RegisterInfectedResponse> responseObserver) {
		/* save time of reception */
		Instant timestamp = Instant.now(); 

		System.out.printf("%nReceived new infected user!%n");

		/* parse client message */
		int number = request.getNumber();
		int key = request.getKey();

		System.out.printf("Content: number %d, key %d%n", number, key);

		/* update storage */
		storage.storeInfectedData(number, key, timestamp);

		/* send response */
		RegisterInfectedResponse response = RegisterInfectedResponse.newBuilder().build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

		System.out.printf("Sent response to infected user!%n");
	}


	/* ====================================================================== */
    /* ====[                       GET_INFECTED                         ]==== */
    /* ====================================================================== */
    
	@Override
    public void getInfected(GetInfectedRequest request, StreamObserver<GetInfectedResponse> responseObserver) {
		System.out.printf("%nReceived new request for infected update!%n");

		/* parse client message */
		Timestamp timestamp = request.getLastUpdate();
		Instant lastUpdate = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());

		System.out.println("Content: last update " + lastUpdate);

		/* get update from storage */
		List<Storage.InfectedData> new_data = storage.getUpdates(lastUpdate);

		/* prepare response */
		GetInfectedResponse.Builder response = GetInfectedResponse.newBuilder();
		for (Storage.InfectedData data : new_data) {
			int number = data.getNumbers();
			int key = data.getKeys();
			Infected responseData = Infected.newBuilder().setNumber(number).setKey(key).build();

			response.addInfected(responseData);
		}
		
		/* send response */
		responseObserver.onNext(response.build());
		responseObserver.onCompleted();

		System.out.printf("Sent infected update to user!%n");
	}
}
