package services;

import org.jpdna.grpchello.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import io.grpc.stub.StreamObserver;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import com.google.protobuf.Empty;
import org.dominic.example.doors.doorsGrpc;
import org.dominic.example.doors.LockRequest;
import org.dominic.example.doors.LockResponse;
import org.dominic.example.occupancy.OccupancyGrpc;
import org.dominic.example.occupancy.OccupancyRequest;
import org.dominic.example.occupancy.OccupancyResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import client.ServiceObserver;
import client.ServiceDescription;
import client.jmDNSServiceTracker;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import serviceui.Printer;
import serviceui.ServiceUI;

public class DoorServer implements ServiceObserver {

    private final String name;
    protected ServiceDescription current;
    private final String serviceType;
    private static final Logger logger = Logger.getLogger(DoorServer.class.getName());
    
    private ManagedChannel channel;
    private OccupancyGrpc.OccupancyBlockingStub blockingStub;

    /* The port on which the server should run */
    private int port = 8080;
    private Server server;

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(new DoorsImpl())
                .build()
                .start();
        JmDNSRegistrationHelper helper = new JmDNSRegistrationHelper("Cavin's", "_doors._udp.local.", "", port);
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                DoorServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }
    
    public DoorServer() {
        serviceType = "Door._udp.local.";
        name = "Doors";
        jmDNSServiceTracker clientManager = jmDNSServiceTracker.getInstance();
        clientManager.register(this);
        serviceAdded(new ServiceDescription("127.0.0.1", 4200));
    }
    
    String getServiceType() {
        return serviceType;
    }
    
    public void serviceAdded(ServiceDescription service) {
        System.out.println("service added");
        current = service;
        channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
                .usePlaintext(true)
                .build();
        blockingStub = OccupancyGrpc.newBlockingStub(channel);
        Occupancy();
    }
    
    public List<String> serviceInterests() {
        List<String> interests = new ArrayList<String>();
        interests.add(serviceType);
        return interests;
    }
    
    public boolean interested(String type) {
        return serviceType.equals(type);
    }

    public String getName() {
        return name;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
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
        
        final DoorServer server = new DoorServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class DoorsImpl extends doorsGrpc.doorsImplBase {
        
        public DoorsImpl() {
            String name = "Cavin's";
            String serviceType = "_doors._udp.local.";
        }
        
        
        public void locks(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.dominic.example.doors.LockResponse> responseObserver) {
                int max = 2; 
                int min = 1; 
                int range = max - min + 1;
                
                    int locks = (int)(Math.random() * range) + min; 
                    
                     String lockString;
                
                        switch (locks) {
                        case 1:  lockString = "Locked: ";
                                 break;
                        case 2:  lockString = "Unlocked: ";
                                 break;
                        default: lockString = "Invalid";
                                 break; 
                        }
                    
                        LockResponse reply = LockResponse.newBuilder().setReply(lockString).build();
                        responseObserver.onNext(reply);
                        responseObserver.onCompleted();
            }
        
        public void scheduler(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<org.dominic.example.doors.LockResponse> responseObserver) {
            Timer t = new Timer();
            //Timer runs once an hour
            long delay = 1000L;
            long period = 1000L * 60L * 60L * 1L;
            t.schedule(new RemindTask(responseObserver), delay, period);
        }    
    }
    
    class RemindTask extends TimerTask {

            StreamObserver<LockResponse> o;

            public RemindTask(StreamObserver<LockResponse> j) {
                o = j;
            }

            @Override
            public void run() {
                    try {
                        LockResponse status = LockResponse.newBuilder().build();
                        o.onNext(status);
                    } catch(RuntimeException e) {
                         o.onCompleted();
                         this.cancel();   
                    }
                    
                } 
                
            }
        
        
        public void Occupancy(){
            
            OccupancyRequest reply = OccupancyRequest.newBuilder().build();
            OccupancyResponse resp = blockingStub.isOccupied(reply);
            System.out.println(resp);
            
        }
        
    public void switchService(String name) {
        // TODO
    }
    
}