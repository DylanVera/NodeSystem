// Dylan Vera
//
// Server.java
// -----------
// Server process to coordinate releasing node resources to clients and maintaining the set of nodes across all clients in real time. The server owns the "main"
// set of 150 nodes that Worker threads will operate on. When a client messages the server to release a node, it also provides the new node value to be updated on the server
// and sent back to all clients to reflect the change before the node is released.

package node;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.lang3.tuple.*;

public class Server extends BaseServer
{
    Server( int port, String addr )
    {
        super( port, addr );
        for (int i = 0; i < 150; i++) {
            nodes[i].hasToken = true;   //Each node is free to be edited to begin with
            queues.add(new ConcurrentLinkedQueue<Pair<InetAddress, Integer>>());
        }
    }

    public void run()
    {
        System.out.println("Running server code");
        try {

            // Sets up the thread for receiving datagram packets.
            startEQ( port );

            // Sets up the socket for sending  datagram packets.
            setupSender();

            while( true )
            {
                handleEvents();
            }

        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    //
    public void handleEvents() throws Exception
    { 
       while( eq.isEmpty() == false )
        {
            DatagramPacket dp  = eq.poll();         //Get the next event out of the queue
            String message = EventQueue.getString( dp );    //Split the different parts of the message if necessary
            String mes[] = message.split(":");

            int node_num = 0;

            switch( mes[0] )
            {
                //Registration request from the client
                case "reg":
                    register( dp.getAddress(), dp.getPort(), Integer.parseInt(mes[1].trim()) );
                    // send("reg", registered.get( Pair.of(dp.getAddress(), dp.getPort()) ) ); //register server with client?
                    break;
                
                //Client request for a node
                case "req":
                    node_num = Integer.parseInt(mes[1].trim());

                    System.out.printf("Received command req for node %d\n", node_num);

                    //If the node isn't already being updated, acquire the resources and message the client 
                    //that it can signal the Worker to carry out its update op
                    if (nodes[node_num].hasToken) {
                        nodes[node_num].hasToken = false;

                        System.out.printf("Sending %d\n", node_num);

                        send("OK:" + node_num, registered.get( Pair.of(dp.getAddress(), dp.getPort()) ) );
                    }
                    else {      //Otherwise the node is currently being updated and  the event will be queued up to be handled later
                        queues.get(node_num).add( registered.get( Pair.of(dp.getAddress(), dp.getPort()) ));
                        System.out.printf("Queuing %d\n", node_num);
                    }
                    break;
                
                //Client message to update and release a node based on value given.
                //After updating the s ervers local copy of the node, release the node and apply the change 
                //to the client copy  of the node
                case "rel":
                    node_num = Integer.parseInt(mes[1].trim());
                    String updated_value = mes[2].trim();

                    System.out.printf("Node %d: %s -> %s\n", node_num, nodes[node_num].string, updated_value);

                    nodes[node_num].string = updated_value;
                    nodes[node_num].hasToken = true;
                    
                    //If this node was queued anywhere else then release that node to the client that requested it
                    if (queues.get(node_num).size() > 0) {
                        Pair<InetAddress, Integer> destination = queues.get(node_num).poll();
                        nodes[node_num].hasToken = false;
                        send("OK:" + node_num, destination);
                    }

                    //Message all the clients to update their copy of the node
                    //to reflect new value on the server
                    for( Pair<InetAddress, Integer> i: registered.values() )
                    {
                        if( Pair.of(dp.getAddress(), dp.getPort()) != i )
                        {
                            send("upd:" + node_num + ":" + updated_value, i);
                        }
                    }

                    break;
            }

        }
    }

    private final ArrayList<ConcurrentLinkedQueue<Pair<InetAddress, Integer>>> queues = new ArrayList<ConcurrentLinkedQueue<Pair<InetAddress, Integer>>>();
}

