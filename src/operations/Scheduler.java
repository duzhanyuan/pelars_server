package operations;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 singleton class managing the working threads 
 */
public class Scheduler {
	
	private static Scheduler my_scheduler;
	private final BlockingQueue<Operation> task_queue;
	private Thread[] active_threads;
	
	//variable to synchronize threads
	public static boolean to_stop = false;
	
	static{
		my_scheduler = new Scheduler();
	}
	
	private Scheduler(){
		//the queue is blocking when elements are popped or pushed
		task_queue = new LinkedBlockingQueue<Operation>(1000);
		active_threads = new Thread[10];
		for (int i = 0; i < 10; i++){
			active_threads[i] = (new Thread( new Worker(task_queue)));
			//start execute the run() method of the Worker class
			active_threads[i].start();
		}
	}
	
	/**
	should stop gracefully the threads 
	 */
	public static void destroy(){
		to_stop = true;
		for(Thread t : my_scheduler.active_threads){
			try {
				t.join();
			} catch (InterruptedException e) {}
		}
	}
	
	public BlockingQueue<Operation> getTaskQueue(){
		return task_queue;
	}

	
	public static void pushTask(Operation op){
		try {
			my_scheduler.task_queue.put(op);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}

	public int checkActiveThreads(){
		int a = 0;
		for (Thread t : active_threads){
			if(t.isAlive()) 
				a++;
		}
		return a;
	}
}


