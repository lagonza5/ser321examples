public class Deadlock {

	//friend does not extend thread or have a run() method... but it has two synchronized methods
	static class Friend {
		private final String name;
		public Friend(String name) {
			this.name = name;
		}
		public String getName() {
			return this.name;
		}
        /* See the README.md for a reference on 'synchronized' methods */
		public synchronized void bow(Friend bower) {
			System.out.format("%s: %s"
					+ "  has bowed to me!%n", 
					this.name, bower.getName());
            System.out.format("%s: waiting to bow back%n", bower.getName());
			bower.bowBack(this);
		}
		public synchronized void bowBack(Friend bower) {
            System.out.format("%s: waiting", this.name);
			System.out.format("%s: %s"
					+ " has bowed back to me!%n",
					this.name, bower.getName());
		}
	}

	/*
	When you use an anonymous inner class in Java, you will be defining the class and instantiating the single object of
	that class in the same statement.

	An anonymous inner class can either extend any existing class or implement any existing interface.

	There are two different ways to create an anonymous inner class:
	1. The first way creates a subclass of an existing class.
	2. The second way is by implementing an interface.

	It is useful when you need to create a subclass of some existing class but you ONLY EVER need 1 object of that subclass.
	It is useful when you need a single object that implements an interface and you won't have to create a new file/class.
	 */
	public static void main(String[] args) {
		final Friend alphonse = new Friend("Alphonse");
		final Friend gaston = new Friend("Gaston");
        /* start two threads - both operating on the same objects */
		//A thread object with no name and an argument that is a class with no name
		new Thread(new Runnable() {
			public void run() { alphonse.bow(gaston); }
		}).start();
		//2 different ways to implement the run method, an anonymous Runnable or a lambda expression
		//the run method has no parameters, hence the empty parentheses
		new Thread(() -> gaston.bow(alphonse)).start();
	}
}
