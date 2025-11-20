package worker;

import worker.ConversionWorker;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	private Thread workerThread;
	private ConversionWorker worker;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("Application started - Starting ConversionWorker...");

		worker = new ConversionWorker();
		workerThread = new Thread(worker);
		workerThread.start();

		sce.getServletContext().setAttribute("workerThread", workerThread);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("Application stopping - Stopping ConversionWorker...");

		if (worker != null) {
			worker.stop();
		}

		if (workerThread != null) {
			workerThread.interrupt();
			try {
				workerThread.join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}