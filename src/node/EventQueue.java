// Dylan Vera
//
// EventQueue.java
// ---------------
// Event queue for receiving datagram packets and queueing them in FIFO order to be handled.
// Having an event queue for all clients/server allows them to coordinate in order to avoid deadlock and starvation

package node;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class EventQueue extends Thread
{
    EventQueue( int port )
    {
        this.port = port;   //listening port for this queue
        try {
            socket = new DatagramSocket( port );
            System.out.println("Listening on port " + socket.getLocalPort());
        } catch( Exception e ) {
            e.printStackTrace();
        }
    }

    public void run()
    {
        try {
            while( true )
            {
                receive();  //Receive events as long as the queue is running
            }
        } catch( Exception e ) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    //Returns the first packet in the queue or null if empty
    public synchronized DatagramPacket poll()
    {
        return events.poll();
    }

    //Receives a packet on listening  socket and adds it to the vent queue
    public void receive() throws Exception
    {
        byte[] buffer = new byte[ MAX_LENGTH ];
        DatagramPacket dp = new DatagramPacket( buffer, buffer.length );
        socket.receive( dp );   //Receive packet from socket
        events.add( dp );       //Appened to queue
    }

    public static String getString( DatagramPacket dp )
    {
        return new String( dp.getData(), 0, MAX_LENGTH );
    }

    //Is the event queue empty>
    public Boolean isEmpty()
    {
        return events.isEmpty();
    }

    public DatagramSocket getSocket()
    {
        return socket;
    }

    public int getPort()
    {
        return socket.getLocalPort();
    }

    public Queue< DatagramPacket > events = new ConcurrentLinkedQueue< DatagramPacket >();  //FIFO Queue to approopriately schedule events
    public DatagramSocket socket;       //socket for receiving packets for the queue
    private int port;                   //listening port that this queue will receieve packets over

    public static final int MAX_LENGTH = 256;
}

