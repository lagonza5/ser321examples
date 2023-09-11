//A class that impelments the Runnable interface can be used in a multithreaded environment
class Worker425 implements Runnable {
    protected int id;
    protected int sleepDelay;
    
    public Worker425 (int assignedID, int sd) {
        id = assignedID;
        sleepDelay = sd;
    }

    //A class that implements the Runnable interface must provide an implementation of the run method
    @Override
    public void run() {
        for (int loop=0; loop < 5; loop++) {
            System.out.println("Hello from " + id  + " loop=" + loop);
            //each thread will perform 5 iterations of this for-loop
            try {
        		Thread.sleep(sleepDelay);
        	} catch (Throwable t) {
        		t.printStackTrace();
        	}
        }
    }
}

class FirstThread {
    public static void main(String args[]) {
      if (args.length != 2) {
              System.out.println("Expected Arguments: <repeat(int)> <sleep(int)>");
                System.exit(0);
       }
      int times = 5; // default repeat count
      int sleepDelay = 5; // default sleep delay
      try {
          times = Integer.parseInt(args[0]);
          sleepDelay = Integer.parseInt(args[1]);
      } catch (NumberFormatException nfe) {
          System.out.println("[repeat|sleep] must be integer");
          System.exit(2);
      }

      //5 threads will be created (how many "times" we are performing the work)
      for (int loop = 0; loop < times; loop++) {
          //Not creating a "Runnable" object, "Runnable" is an interface.
          Runnable worker = new Worker425(loop, sleepDelay*loop);
          Thread task = new Thread(worker, "Task#"+loop); //the work that the thread will perform
          task.start(); //you must call this start method (a life cycle method) to start a thread's life cycle
      }
    }
}
