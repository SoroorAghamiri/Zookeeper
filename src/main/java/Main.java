package distributed.systems;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Main is responsible for creating an instance of LeaderElection object.
 * The instance invokes connectToZookeeper to start a session with Zookeeper server.
 * Next, zNodes are created and added as children to /election.
 * Then Leader election algorithm is run on each individual node to find the leader.
 * The connection is kept open through run method and will receive a message when server is disconnected.
 * In case of connection failure, the session is closed.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnecting from zookeeper, exiting application.");
    }
}
