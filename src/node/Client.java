// Dylan Vera
//
// Client.java
// -----------
// Client process to update nodes and signal server of the changes. Clients maintain their own copy of the 150 nodes 
// hosted on the server as well as the 100 Worker threads responsible for updating the nodes. When server receives 
// an updated node, it will message all clients to update their local copy of the node.

package node;

import java.net.*;

public class Client extends BaseServer
{
    Client( int port, String addr )
    {
        super( port, addr );
    }

    public void run()
    {
        System.out.println("Running client code");
        
        try {
            
            // Sets up the thread for receiving datagram packets.
            startEQ( 0 );
            
            // Sets up the socket for sending  datagram packets.
            setupSender();

            //Register this client with the server
            send( "reg:" + Integer.toString(eq.getPort()), addr, port );

            Worker[] workers = new Worker[num_workers];
            for (int i = 0; i < num_workers; i++) {
                workers[i] = new Worker(nodes, this);
                workers[i].start();
            }

            //handle events from the queue
            while( true )
            {
                handleEvents();
            }

        } catch( Exception e ) {
            e.printStackTrace();
        } finally {

            if( socket != null )
                socket.close();
        }
    }
    
    //Checks for events in the queue to handle messages from server
    //The three types of messages expected from the server are reg, ok, and  upd
    //reg - registers the server with this client 
    //OK - Signals client that may signal waiting worker to start operating on a node
    //upd - server messages client to update its local copy of newly edited server node
    public void handleEvents()
    {
        while( eq.isEmpty() == false )
        {
            DatagramPacket dp  = eq.poll();             //Get the next event from the queue
            String message = EventQueue.getString( dp );    //Break up the message if necessary (message type/data)
            String mes[] = message.split(":");  
            int node_num = 0;

            switch( mes[0] )
            {
                //Register this server with this client
                case "reg":
                    register( dp.getAddress(), dp.getPort(), Integer.parseInt(mes[1].trim()) );
                    break;
                
                //Server messages cilent that a node is now free for a Worker to operate on
                case "OK":
                    System.out.println("Ok received");
                    node_num = Integer.parseInt(mes[1].trim());
                    nodes[node_num].hasToken = true;
                    nodes[node_num].signalAll();
                    break;
                
                //Server alerts the client to update it's copy of the updated node
                case "upd":
                    node_num = Integer.parseInt(mes[1].trim());
                    String updated_value = mes[2].trim();
                    nodes[node_num].string = updated_value;
                    break;
                
                default:
                    System.out.printf("Received Message: %s\n", message);
                    break;
            }
        }
    }
    
    //Send a request to the server for node given by Worker
    public synchronized void handleWorkerRequest(int node_num) throws Exception {
        send("req:" + node_num);
    }

    //blocks node and sends message to server to update it's copy
    public synchronized void handleWorkerUpdate(int node_num) throws Exception {
        nodes[node_num].hasToken = false;
        send("rel:" + node_num + ":" + nodes[node_num].string);
    }

    private DatagramSocket socket;
    static final int num_workers = 100;
}

