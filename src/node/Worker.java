// Dylan Vera
//
// Worker.java
//-------------
// Worker threads used by clients to update nodes. A worker randomly select a node to update before signaling
// its client to request the node from the server. Once the server messages the client with an OK
// the worker shuffles the node data and alerts the client to message the updated node back to the server.
// Each worker completes 200 updates.

package node;

import java.util.*;

public class Worker extends Thread {
    
    public Node[] nodes;    //Each worker has a list of nodes matching its client
    public Client client;   //Reference to the client that created this worker
    public Random rand = new Random();
    
    public Worker(Node[] nodes, Client client) {
        this.nodes = nodes;
        this.client = client;
    }
    
    public void run() {
        for(int i = 0; i < 200; i++)
        {
            try {
            
                //Choose a random node and have the client request it from the server
                int node_num = rand.nextInt(150);
                client.handleWorkerRequest(node_num);
                                
                //Shuffle the node data when signaled/node is unlocked
                nodes[node_num].shuffle();
                                                                                                                                                                            
                //Release the node with brief sleep cycle
                System.out.println("Releasing node " + node_num);
            
                //Signal the client to message the server the updated node
                client.handleWorkerUpdate(node_num);
                Thread.sleep(10);
            } catch (Exception e) {}
        }
    }
}

