package services;

import org.jpdna.grpchello.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import io.grpc.stub.StreamObserver;
import java.util.Timer;
import java.util.TimerTask;
import com.google.protobuf.Empty;
import java.util.logging.Logger;
import io.grpc.stub.StreamObserver;
import org.dominic.example.occupancy.OccupancyGrpc;
import org.dominic.example.occupancy.OccupancyRequest;
import org.dominic.example.occupancy.OccupancyResponse;
import serviceui.Printer;
import serviceui.ServiceUI;
import java.lang.Math; 

public class Occupancy {

    private static final Logger logger = Logger.getLogger(Occupancy.class.getName());

    /* The port on which the server should run */
    private int port = 4200;
    private Server server;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new OccupancyImpl())
                .build()
                .start();
        JmDNSRegistrationHelper helper = new JmDNSRegistrationHelper("Cavin's", "_doors._udp.local.", "", port);
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                Occupancy.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon
     * threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws Exception {
        
        final Occupancy server = new Occupancy();
        server.start();
        server.blockUntilShutdown();
    }

    private class OccupancyImpl extends OccupancyGrpc.OccupancyImplBase {
        
        
        public OccupancyImpl() {
            String name = "Cavin's";
            String serviceType = "_doors._udp.local.";
        }
        
    
   public void isOccupied(org.dominic.example.occupancy.OccupancyRequest request,
        io.grpc.stub.StreamObserver<org.dominic.example.occupancy.OccupancyResponse> responseObserver) {
    
              OccupancyResponse res = OccupancyResponse.newBuilder().setValue(true).build();
              responseObserver.onNext(res);
              responseObserver.onCompleted();
              }
        }
        
}