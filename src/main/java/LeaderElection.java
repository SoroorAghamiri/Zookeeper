package distributed.systems;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";
    private ZooKeeper zooKeeper;
    private String currentZnodeName;
    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected)
                {
                    System.out.println("Successfully connected to Zookeeper.");
                }else{
                    synchronized (zooKeeper){
                        System.out.println("Disconnecting from zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }

    public void run() throws InterruptedException{
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    /**
     * Closes the current session with server.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public void close() throws InterruptedException{
        zooKeeper.close();
    }

    /**
     * Creates a zNode as a child to /election with the name convention of c_ followed by a sequence number.
     * The name of node is saved inside the instance variable <code>this.currentZnodeName</code>
     * @throws KeeperException
     * @throws InterruptedException if the current thread is interrupted.
     */
    public void volunteerForLeadership() throws KeeperException, InterruptedException{
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zooKeeper.create(znodePrefix,new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name " + znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace("/election/" , "");
    }

    /**
     * Gets a list of children of /election.
     * Sorts the children lexicographically and gets the smallest child.
     * By comparing currnet node's name with the smallest child, decides if current node is the leader or not.
     * @throws KeeperException
     * @throws InterruptedException if the current thread is interrupted.
     */
    public void electLeader() throws KeeperException, InterruptedException{
        List<String> electionChildren = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(electionChildren);
        String smallestChild = electionChildren.get(0);
        if(this.currentZnodeName.equals(smallestChild)){
            System.out.println("I am the leader");
            return;
        }
        System.out.println("I am not the leader, " + smallestChild + " is the leader.");

    }
}
