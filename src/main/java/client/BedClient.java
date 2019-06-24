package client;

import javax.swing.JPanel;
// import clientui.BedClientGUI;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dominic.example.CentralHeating.CentralHeatingGrpc;
import org.dominic.example.CentralHeating.Status;
import org.dominic.example.doors.doorsGrpc;
import org.dominic.example.doors.LockRequest;
import org.dominic.example.doors.LockResponse;
import org.dominic.example.occupancy.OccupancyGrpc;
import org.dominic.example.occupancy.OccupancyRequest;
import org.dominic.example.occupancy.OccupancyResponse;
import org.dominic.example.lights.LightsGrpc;
import org.dominic.example.lights.Request;
import org.dominic.example.lights.Response;


public class BedClient implements ServiceObserver {

    // protected BedClientGUI ui;
    protected ServiceDescription current;
    private final String serviceType;
    private final String name;
    private static final Logger logger = Logger.getLogger(BedClient.class.getName());

    private ManagedChannel channel;
    private CentralHeatingGrpc.CentralHeatingBlockingStub blockingStub;
    private doorsGrpc.doorsBlockingStub DblockingStub;
    private LightsGrpc.LightsBlockingStub LblockingStub;
    
    
    public BedClient() {
        serviceType = "Home._udp.local.";
        name = "Home";
        jmDNSServiceTracker clientManager = jmDNSServiceTracker.getInstance();
        clientManager.register(this);
        serviceAdded(new ServiceDescription("52.55.92.92", 50021));
        DoorsServiceAdded(new ServiceDescription("52.55.92.92", 8080));
        LightsServiceAdded(new ServiceDescription("52.55.92.92", 3300));
    }

    String getServiceType() {
        return serviceType;
    }

    void disable() {
        // no services exist for this client type
    }

    public List<String> serviceInterests() {
        List<String> interests = new ArrayList<String>();
        interests.add(serviceType);
        return interests;
    }

    public void serviceAdded(ServiceDescription service) {
        System.out.println("service added");
        current = service;
        channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
                .usePlaintext(true)
                .build();
        blockingStub = CentralHeatingGrpc.newBlockingStub(channel);
        warm();
    }
    
    public void DoorsServiceAdded(ServiceDescription service) {
        System.out.println("service added");
        current = service;
        channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
                .usePlaintext(true)
                .build();
        DblockingStub = doorsGrpc.newBlockingStub(channel);
        doors();
    }
    
    public void LightsServiceAdded(ServiceDescription service) {
        System.out.println("service added");
        current = service;
        channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
                .usePlaintext(true)
                .build();
        LblockingStub = LightsGrpc.newBlockingStub(channel);
        lights();
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

    public void warm() {
        try {

            new Thread() {
                public void run() {
                    Empty request = Empty.newBuilder().build();

                    Iterator<Status> response = blockingStub.warm(request);
                    while (response.hasNext()) {
                        System.out.println(response.next().toString());
                    }
                }
            }.start();

            Empty request = Empty.newBuilder().build();
            Status stat = blockingStub.boiler(request);
            System.out.println(stat + "boiling water..." );
            
            Empty another_request = Empty.newBuilder().build();
            Status stats = blockingStub.rads(another_request);
            System.out.println(stats + "heating rads...");

        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "RPC failed", e);
            return;
        }
    }
    
    
    public void doors(){
        try{
            new Thread(){
                 public void run() {
                    Empty req = Empty.newBuilder().build();
                    Iterator<LockResponse> response = DblockingStub.scheduler(req);
                    
                while (response.hasNext()) {
                        System.out.println(response.next().toString());
                    }
                }
            }.start();
            
            Empty request = Empty.newBuilder().build();
            LockResponse resp = DblockingStub.locks(request);
            
            System.out.println(resp);
                
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "RPC failed", e);
            return;
        }
    }
    
    public void lights(){
        try{
            Request request = Request.newBuilder().build();
            Response resp = LblockingStub.turnOn(request);
            
            System.out.println(resp);
            
            Empty req = Empty.newBuilder().build();
            Iterator<Response> response = LblockingStub.changeBrightness(req);
            
            while (response.hasNext()) {
                System.out.println(response.next().toString());
            }
             
            Request another_request = Request.newBuilder().build();
            Response another_response = LblockingStub.changeColour(another_request);
            
            System.out.println(another_response);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "RPC failed", e);
            return;
        }
        
    }


    public void switchService(String name) {
        // TODO
    }

    public static void main(String[] args) {
        new BedClient();
    }

}


