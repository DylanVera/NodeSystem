// Dylan Vera
//
// BaseServer.java
// ---------------
// Abstract class that clients/server will extend. It defines the common methods used by both.
// This class handles setting up a socket for sending packets and the event queue for receiving packets
// as well as the message that physically sends the packets. Clients and the server maintain a list of registerd
// ip/port #'s to ensure we're only handling messages from registered connections

package node;

import java.util.*;
import java.net.*;
import org.apache.commons.lang3.tuple.*;

public abstract class BaseServer implements Runnable
{
    BaseServer( int port, String addr )
    {
        this.port = port;   //Sending port for client/server
        
        //Tries to fetch the IP adress of the machine operating the server
        try {
            this.addr = InetAddress.getByName( addr );
        } catch( Exception e ) {
            e.printStackTrace();
        }
        
        //Each 
        for( int i = 0; i < nodes.length; ++i )
        {
            nodes[i] = new Node();
        }
    }

    //Abstract functions for client/server specific code
    public abstract void run();
    public abstract void handleEvents() throws Exception;

    //Sets up the event queue for receiving messages
    protected void startEQ( int eqPort )
    {
        try {

            eq = new EventQueue( eqPort );
            eq.start();

        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
    
    //Sets up a socket for sending messages
    protected void setupSender() throws Exception
    {
        sender = new DatagramSocket();
    }

    //Sends a message to the given port an ip
    protected synchronized void send( String message, InetAddress serverAddress, int port ) throws Exception
    {
        send( message, Pair.of( serverAddress, port ) );
    }

    protected synchronized void send( String message ) throws Exception
    {
        send( message, Pair.of( addr, port ) );
    }

    protected synchronized void send( String message, Pair<InetAddress, Integer> pair ) throws Exception
    {
        byte[] buffer = message.getBytes();
        
        //Sends a packet with a message (event) to the given port#
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, pair.getLeft(), pair.getRight() );
        System.out.println( "Sending data to remote port#" + pair.getRight() + ": " + message);
        sender.send( dp );
    }

    //Register the given pair of port and ip with this client/server
    //Only messages from registered connections will be accepted
    protected synchronized void register( InetAddress addr, int port1, int port2 )
    {
        register( Pair.of( addr, port1 ), Pair.of( addr, port2 ) );
    }

    protected synchronized void register( Pair<InetAddress, Integer> pair1, Pair<InetAddress,Integer> pair2 )
    {
        registered.put( pair1, pair2 );
    }

    protected int port;     //Port that this client/server uses to send datagram packets
    protected InetAddress addr; //IP addr of this client/server
    protected EventQueue eq;    //Queue for this client/server to receive and schedule handling of datagram packets
    protected DatagramSocket sender;   //Socket for sending packets

    protected Node[] nodes = new Node[150]; //The server and all clients need to maintain an updated list of nodes

    //Clients and the server only want to accept packets from registered connections
    protected Map<Pair<InetAddress, Integer>, Pair<InetAddress, Integer>> registered = new HashMap<Pair<InetAddress, Integer>, Pair<InetAddress, Integer>>();
}

