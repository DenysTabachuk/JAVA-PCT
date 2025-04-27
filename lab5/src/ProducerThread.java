import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class ProducerThread implements Runnable {
    private boolean running = true;

    private final BlockingQueue<Customer> queue;
    private final Result result;
    private final Random random;
    private final int meanIncomeTimeMs;

    public ProducerThread(BlockingQueue<Customer> queue, Result result, int meanIncomeTimeMs){
        this.queue = queue;
        this.result = result;
        this.meanIncomeTimeMs = meanIncomeTimeMs  ;
        this.random = new Random();
    }

    public void stop(){
        this.running = false;
    }

    @Override
    public void run(){

        while (running){
            try {
                Customer newCustomer = new Customer();

                if (queue.offer(newCustomer)){
                System.out.println("Customer " + newCustomer.getId() + " added to the queue");
                }
                else{
                    // якщо черга повна
                    result.incrementRejectedRequests();
                System.out.println("Customer " + newCustomer.getId() + " failed to add to queue. Queue is full");
                }

                // рівномірний розподіл
                int minInterval = meanIncomeTimeMs - meanIncomeTimeMs / 4;
                int maxInterval = meanIncomeTimeMs + meanIncomeTimeMs / 2;
                int interval = random.nextInt(maxInterval - minInterval) + minInterval;


                Thread.sleep(interval);
            } catch (InterruptedException e) {
                stop();
            }

        }

        System.out.println("The producer stopped.\n");
    }
}
