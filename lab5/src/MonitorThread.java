import java.util.concurrent.BlockingQueue;

public class MonitorThread implements Runnable {
    private boolean running = true;

    private final BlockingQueue<Customer> queue;
    private final Result result;

    public MonitorThread(BlockingQueue<Customer> queue, Result result){
        this.queue = queue;
        this.result = result;
    }

    void stop(){
        this.running = false;
    }

    @Override
    public void run(){

        while (running){
            try {
                int currentQueueSize = queue.size();
                result.addQueueLength(currentQueueSize);

                synchronized (System.out) {
                    System.out.println("============SYSTEM STATE============");
                    System.out.println("Queue length: " + queue.size());
                    System.out.println("Processed: " + result.getProcessedRequests());
                    System.out.println("Rejected: " + result.getRejectedRequests());
                    System.out.println("Total: " + result.getTotalRequests());
                    System.out.println("Average Queue Length: " + result.getAverageQueueLength());
                    System.out.println("Probability of Rejection: " + result.getProbabilityOfRejection());
                    System.out.println("====================================");
                }


                Thread.sleep(1000); // раз в секунду
            } catch (InterruptedException e) {
                stop();
            }
        }
    }
}
