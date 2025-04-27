import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Result {
    private AtomicInteger totalRequests = new AtomicInteger(0);
    private AtomicInteger rejectedRequests = new AtomicInteger(0);
    private AtomicInteger processedRequests = new AtomicInteger(0);

    // Для середньої довжини черги
    private AtomicLong totalQueueLength = new AtomicLong(0);
    private AtomicInteger queueMeasurements = new AtomicInteger(0);


    public void incrementRejectedRequests() {
        totalRequests.incrementAndGet();
        rejectedRequests.incrementAndGet();
    }

    public void incrementProcessedRequests() {
        totalRequests.incrementAndGet();
        processedRequests.incrementAndGet();
    }

    public void addQueueLength(int length) {
        totalQueueLength.addAndGet(length);
        queueMeasurements.incrementAndGet();
    }

    public int getTotalRequests() {
        return totalRequests.get();
    }

    public int getRejectedRequests() {
        return rejectedRequests.get();
    }

    public int getProcessedRequests() {
        return processedRequests.get();
    }

    public double getProbabilityOfRejection() {
        if (totalRequests.get() == 0) return 0.0;
        return (double) rejectedRequests.get() / totalRequests.get();
    }

    public double getAverageQueueLength() {
        if (queueMeasurements.get() == 0) return 0.0;
        return (double) totalQueueLength.get() / queueMeasurements.get();
    }

}
