// Dylan Vera
// 
// Node.java
// ---------
// Node data structure for the network. It has a random string of arbitrary length as its data and uses a lock and token
// to achieve concurrency. It has a function to rearrange the characters in its string at the request of Worker threads

package node;

import java.util.*;
import java.util.concurrent.locks.*;

public class Node {
    
    static final int stringLength = 500;
    public Lock lock = new ReentrantLock();         //Lock for mutual exclusion
    public Condition cond = lock.newCondition();    //Lock condition to coordinate threads waiting and waking up
    public String string;
    public Boolean hasToken = false;                //Only a host that holds a token can send data, and tokens are released when receipt of the data is confirmed

    //Initialize the node with a random string
    public Node() {    
      this.string = makeString();
    }
    
    public Node(String string) {
      this.string = string;
    }
    
    //Locks the node before signaling threads to wake up and operate before releasing the lock again
    //Signals worker thread to awake and continue their update
    public void signalAll() {
        lock.lock();
        cond.signalAll();
        lock.unlock();
    }
    
    //Returns a random string of length stringLength to be used as the nodes data
    public String makeString() {
        String pool = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random r = new Random();
        StringBuilder ret = new StringBuilder(Node.stringLength);
        
        for (int i = 0; i < Node.stringLength; i++)
            ret.append(pool.charAt(r.nextInt(pool.length())));
        
        return ret.toString();
    }
  
    //Wait until node is signalled to complete the shuffle operation (called by worker thread)
    public void shuffle() {
        lock.lock();    //Lock critical section
        
        try {
            while (!hasToken) {
                cond.await();   //Have the thread wait for signal its safe to edit
            }

            System.out.printf("Editing\n");
            this.string = randomize();
            

        } catch (Exception e) {
        } finally {
            lock.unlock();   //exit critical section
        }
    }
    
    //Rearrange the characters of the node's string
    public String randomize() {
        StringBuilder sb = new StringBuilder(this.string.length());
        double rnd;

        //iterate over characters in the data and shuffle them around
        for (char c: this.string.toCharArray()) {
            rnd = Math.random();
            if (rnd < 0.34)
                sb.append(c);
            else if (rnd < 0.67)
                sb.insert(sb.length() / 2, c);
            else
                sb.insert(0, c);
        }       
        return sb.toString();
    }
}