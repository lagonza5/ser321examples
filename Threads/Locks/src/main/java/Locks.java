import java.util.concurrent.locks.*;

//You can also inherit the Thread class to make code execute in a multithreaded environment
class LockThread extends Thread {
    protected int id;
    protected int sleepDelay;
    protected int loopCount;
    protected Lock mutex;

    public LockThread (int id, Lock mutex, int sd, int lc) {
        this.id = id;
        this.mutex = mutex;
        this.loopCount = lc;
        this.sleepDelay = sd;
    }

    /**
     * Each thread will acquire and release the lock 'loopCount' amount of times (5 times for default values)
     */
    public void run() {
        for (int loop=0; loop < loopCount; loop++) {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Protected region of code
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            mutex.lock();
            try {
                System.out.println("Thread" + id + " has lock");
                Thread.sleep(sleepDelay);
             } catch (InterruptedException e) {
                //
             } finally {
                System.out.println("Thread" + id + " releasing lock");
                mutex.unlock();
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Protected region of code
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
	        }

	    }
    }
}

public class Locks {
    public static void main(String args[]) throws Exception {

        //What is a reentrant lock?
        Lock mutex = new ReentrantLock();

        if (args.length != 3) {
          System.out.println("Expected Arguments: <workers(int)> <sleep(int)> <loop count(int)>");
          System.exit(0);
        }

        int sleepDelay = 10; // default value
        int numWorkers = 25; // default value
        int loopCount = 50; // default value

        try {
            numWorkers = Integer.parseInt(args[0]);
            sleepDelay = Integer.parseInt(args[1]);
            loopCount = Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            System.out.println("[workers|sleep|loop count] must be integer");
            System.exit(0);
        }


        //numWorkers is the upper limit on how many threads will be created.
        //each thread will do a 'loopCount' units of work
        for (int i=0; i < numWorkers; i++) {
            (new LockThread(i, mutex, sleepDelay, loopCount)).start();
            //args: unique id, a lock, a sleep delay, a loop count (units of work performed per thread)
            //always call the start method to start a threads life cycle
        }
    }
}

