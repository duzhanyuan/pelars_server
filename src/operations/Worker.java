package operations;

import hibernateMapping.HibernateSessionManager;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sql.rowset.serial.SerialBlob;
import org.hibernate.Session;
import pelarsServer.Data;
import pelarsServer.OpDetail;
import servlets.Util;

/**
 * 
 * @author Lorenzo Landolfi
 *  class describing the threads executing the asynchronous operations
 *  workers fetch jobs from a shared queue and execute them continuously
 *
 */
public class Worker implements Runnable{

	private final BlockingQueue<Operation> queue;
	int identifier;

	//Each worker is assigned a reference to the shared queue
	public Worker(BlockingQueue<Operation> q){
		queue = q;
	}

	@Override
	public void run() {

		Operation op = null;
		boolean failed = false;
		long start = 0;
		long elapsedTime = 0;

		while(!Thread.interrupted() && !Scheduler.to_stop) {

			try {
				//returns null if timeout expires, gracefully terminates in case the program is stopped 
				op = queue.poll(20000,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {return;}

			if (op != null) {	

				failed = false;
				Session session = HibernateSessionManager.getSession();

				List<OpDetail> to_update = null;
				try {
					to_update = Util.doQuery(session, "SELECT U FROM OpDetail AS U WHERE U.id = :id", "id", op.id);
				} catch (Exception e2) {
					//there is always an OpDetail with the same identifier of the operation instance. This exception is never thrown.
				}

				to_update.get(0).status = OpDetail.Status.EXECUTING;

				op.my_session = session;
				start = System.nanoTime();  

				try{
					List<? extends Data> bs = op.extract();
					op.run(bs);
					op.storeResult();
					elapsedTime = System.nanoTime() - start;
				}
				catch(Exception e){

					Util.rollback(session);

					//session might be closed by the exception
					if (!session.isOpen())
						session = HibernateSessionManager.getSession();

					failed = true;

					to_update.get(0).status = OpDetail.Status.FAILED;
					byte[] bytes = e.getMessage().getBytes();
					Blob err_blob = null;
					try{
						err_blob = new SerialBlob(bytes);
					}catch (SQLException e1){}

					//puts the reason of the failure in the OpDetail entity in db
					to_update.get(0).failure_description = err_blob;
				}

				if (!failed){
					to_update.get(0).status = OpDetail.Status.TERMINATED;
					to_update.get(0).execution_time = (double)elapsedTime/1000000.0;
				}

				if (!session.isOpen())
					session = HibernateSessionManager.getSession();
				Util.update(session, to_update.get(0));

				if (session.isOpen())
					session.close();
			} 
		}
	}
}