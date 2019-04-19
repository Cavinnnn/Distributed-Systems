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
import org.dominic.example.bed.BedGrpc;
import org.dominic.example.bed.BedStatus;
import org.dominic.example.doors.doorsGrpc;
import org.dominic.example.doors.LockRequest;
import org.dominic.example.doors.LockResponse;
import org.dominic.example.doors.Time;
import org.dominic.example.occupancy.OccupancyGrpc;
import org.dominic.example.occupancy.OccupancyRequest;
import org.dominic.example.occupancy.OccupancyResponse;


public class BedClient implements ServiceObserver {

    // protected BedClientGUI ui;
    protected ServiceDescription current;
    private final String serviceType;
    private final String name;
    private static final Logger logger = Logger.getLogger(BedClient.class.getName());

    private ManagedChannel channel;
    private BedGrpc.BedBlockingStub blockingStub;
    private doorsGrpc.doorsBlockingStub DblockingStub;
    private OccupancyGrpc.OccupancyBlockingStub OblockingStub;
    
    
    public BedClient() {
        serviceType = "Home._udp.local.";
        name = "Home";
        jmDNSServiceTracker clientManager = jmDNSServiceTracker.getInstance();
        clientManager.register(this);
        serviceAdded(new ServiceDescription("127.0.0.1", 50021));
        LightsServiceAdded(new ServiceDescription("127.0.0.1", 8080));
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
        blockingStub = BedGrpc.newBlockingStub(channel);
        warm();
    }
    
    public void LightsServiceAdded(ServiceDescription service) {
        System.out.println("service added");
        current = service;
        channel = ManagedChannelBuilder.forAddress(service.getAddress(), service.getPort())
                .usePlaintext(true)
                .build();
        DblockingStub = doorsGrpc.newBlockingStub(channel);
        doors();
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

                    Iterator<BedStatus> response = blockingStub.warm(request);
                    while (response.hasNext()) {
                        System.out.println(response.next().toString());
                    }
                }
            }.start();

            Empty request = Empty.newBuilder().build();
            BedStatus status = blockingStub.getStatus(request);
            System.out.println(status);

        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "RPC failed", e);
            return;
        }
    }
    
    
    public void doors(){
        LockRequest req = LockRequest.newBuilder().build();
        LockResponse response = DblockingStub.locks(req);
                        
        System.out.println(response.getReply());
    }

    public void switchService(String name) {
        // TODO
    }

    public static void main(String[] args) {
        new BedClient();
    }

}

