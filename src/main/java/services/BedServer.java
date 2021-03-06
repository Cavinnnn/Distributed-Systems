package services;

import org.jpdna.grpchello.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import io.grpc.stub.StreamObserver;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.dominic.example.CentralHeating.CentralHeatingGrpc;
import org.dominic.example.CentralHeating.Status;
import serviceui.Printer;
import serviceui.ServiceUI;

public class BedServer {

    private static final Logger logger = Logger.getLogger(BedServer.class.getName());

    /* The port on which the server should run */
    private int port = 50021;
    private Server server;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new HeatingImpl())
                .build()
                .start();
        JmDNSRegistrationHelper helper = new JmDNSRegistrationHelper("Cavin's", "_rads._udp.local.", "", port);
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                BedServer.this.stop();
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
        final BedServer server = new BedServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class HeatingImpl extends CentralHeatingGrpc.CentralHeatingImplBase {

        private int boiler = 0;
        private int rads = 0;

        public HeatingImpl() {
            String name = "Cavin's";
            String serviceType = "_rads._udp.local.";
        }

        @Override
        public void warm(com.google.protobuf.Empty request,
            io.grpc.stub.StreamObserver<org.dominic.example.CentralHeating.Status> responseObserver) {
            Timer t = new Timer();
            t.schedule(new RemindTask(responseObserver), 0, 2000);
        }

        
        //heat water
        @Override
        public void boiler(com.google.protobuf.Empty request,
            io.grpc.stub.StreamObserver<org.dominic.example.CentralHeating.Status> responseObserver) {
            responseObserver.onNext(Status.newBuilder().setPercentageHeated(boiler).build());
            responseObserver.onCompleted();
        }
        
        
        //stream rad heat
        public void rads(com.google.protobuf.Empty request,
            io.grpc.stub.StreamObserver<org.dominic.example.CentralHeating.Status> responseObserver) {
            responseObserver.onNext(Status.newBuilder().setPercentageHeated(rads).build());
            responseObserver.onCompleted();
        }

        class RemindTask extends TimerTask {

            StreamObserver<Status> o;

            public RemindTask(StreamObserver<Status> j) {
                o = j;
            }
            
            public void run() {
                
                if (boiler < 40) {
                    boiler += 10;
                    Status stat = Status.newBuilder().setPercentageHeated(boiler).build();
                    o.onNext(stat);
                } else if(rads < 35) {
                    rads += 5;
                    Status stat = Status.newBuilder().setPercentageHeated(rads).build();
                    o.onNext(stat);
                } else {
                    o.onCompleted();
                    this.cancel();
                }
                
                
            }
            
            
        }
    }
}
