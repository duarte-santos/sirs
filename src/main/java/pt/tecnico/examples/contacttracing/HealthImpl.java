
/* ====================================================================== */
/* ====[                   SIRS - Contact Tracing                   ]==== */
/* ====[                   Server - Basic Version                   ]==== */
/* ====================================================================== */

// status - receive a single pair of <number, key>, not encrypted

package pt.tecnico.examples.contacttracing;

import java.time.Instant;
import java.util.*;

import pt.tecnico.examples.contacttracing.*;
import io.grpc.stub.StreamObserver;
import com.google.protobuf.Timestamp;


public class HealthImpl extends ContactTracingGrpc.ContactTracingImplBase {

	/* Domain implementation */
	//private Storage storage = new Storage();

	int publicKey = 123456789;
	int privateKey = 987654321;
	
	/* ====================================================================== */
    /* ====[                      GENERATE_SIGNATURE                    ]==== */
    /* ====================================================================== */
    
	@Override
    public void generateSignature(GenerateSignatureRequest request, StreamObserver<GenerateSignatureResponse> responseObserver) {
		System.out.printf("%nReceived new infected user!%n");

		List<Integer> numbers = new ArrayList<>();
		List<Integer> keys =  new ArrayList<>();

		/* parse client message */
		List<Infected> new_data = request.getInfectedList();

		System.out.printf("Content: \n");
		for (Infected i : new_data){
			System.out.printf("number %d, key %d%n", i.getNumber(), i.getKey());
		}

		/* send response */
		GenerateSignatureResponse response = GenerateSignatureResponse.newBuilder().setSignature(1337).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

		System.out.printf("Sent response to infected user!%n");
	}
}
