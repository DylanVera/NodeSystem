// Dylan Vera
//
// Runner.java
// -----------
// Main file for the node network. It reads command line arguments to determine whether
// this process will be for a client or server, as well as the ip and port numbers of the server

package node;

public class Runner
{
    public static void main( String args[] )
    {
        setProperties( args );

        //Initialize server or client based on command line args
        Runnable runnable;
        if( type == SocketType.SERVER )
            runnable = new Server( port, addr );
        else
            runnable = new Client( port, addr );

        // Create and start the thread.
        server = new Thread( runnable );
        server.start();

        // Wait for the thread to finish.
        try {
            server.join();
        } catch( InterruptedException e ) {
            e.printStackTrace();
        }
    }

    //Reads in command line args to initialize clients/servers properly
    public static void setProperties( String args[] )
    {
        type = SocketType.SERVER;
        port = 0;
        addr = "localhost";

        for( String i: args )
        {
            // Sets the server type.
            if( i.matches("[a-zA-Z]+") )
            {
                switch(i)
                {
                    case "server":
                    case "s":
                        type = SocketType.SERVER;
                        break;

                    case "client":
                    case "c":
                        type = SocketType.CLIENT;
                        break;
                }
            }

            // Sets the port number. If it's the server, it's the listening port.
            // If it's the client, it's the target port.
            else if( i.matches("[-+]?[0-9]+") )
            {
                int tempPort = Integer.parseInt(i);

                if( tempPort <= 1023 || tempPort > 65535 )
                    tempPort = 0;

                port = tempPort;
            }

            // Sets the address.
            else
            {
                addr = i;
            }
        }
    }

    public static SocketType type = SocketType.SERVER;
    public static int port = 0;              //O as default port# assigns random available port
    public static String addr = "localhost"; //IP address
    public static Thread server;       //Thread for this client/server
}

