import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServiceChannelThread implements Runnable {
    private final double STANDARD_DEVIATION = 0.15;  // 15% стандартного відхилення

    private boolean running;

    private final BlockingQueue<Customer> queue;
    private final Result result;
    private final Random random;
    private final int meanServiceTimeMs;

    public ServiceChannelThread(BlockingQueue<Customer> queue, Result result, int meanServiceTimeMs) {
        this.running = true;
        this.queue = queue;
        this.result = result;
        this.random = new Random();
        this.meanServiceTimeMs = meanServiceTimeMs ;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Customer customer = queue.poll(100, TimeUnit.MILLISECONDS);
                if (customer != null) {
                    int serviceTime = generateNormalServiceTime(meanServiceTimeMs);
                    Thread.sleep(serviceTime);
                    result.incrementProcessedRequests();
                }
            } catch (InterruptedException e) {
                running = false;
            }
        }
        System.out.println("ServiceChannelThread stopped");
    }


    private int generateNormalServiceTime(int meanServiceTimeMs) {
        // Генеруємо випадкове число за нормальним розподілом
        double normalValue = random.nextGaussian();
        // Масштабування до нашого середнього часу обслуговування
        int serviceTime = (int) Math.max(0, meanServiceTimeMs + normalValue * meanServiceTimeMs * STANDARD_DEVIATION);
        return serviceTime;
    }
}
