import java.util.concurrent.*;


public class MultiChannelMSS {
    private final int numChannels;

    private final int simulationTimeMs;
    private final int meanIncomeTimeMs;
    private final int meanServiceTimeMs;

    private final BlockingQueue<Customer> queue;
    private final Result result;

    public MultiChannelMSS(int numChannels,int queueCapacity
            ,int simulationTimeMs, int meanIncomeTimeMs
            ,int meanServiceTimeMs){
        this.numChannels = numChannels;
        this.simulationTimeMs = simulationTimeMs;
        this.meanIncomeTimeMs = meanIncomeTimeMs;
        this.meanServiceTimeMs = meanServiceTimeMs;

        this.result = new Result();
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
    }

    public Result simulate(){
        ExecutorService executorService = Executors.newFixedThreadPool(numChannels + 2);
        // + 1 для Producer
        // + 1 для Monitor


        executorService.submit(new Producer(queue, result, meanIncomeTimeMs));

        for (int i = 0; i < numChannels; i++){
            executorService.submit(new ServiceChannelThread(queue, result, meanServiceTimeMs));
        }

        executorService.submit(new MonitorThread(queue, result));


        // чекаємо кінця часу симуляції
        try {
            Thread.sleep(simulationTimeMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executorService.shutdownNow(); // Попросити всі потоки зупинитися (через interrupt)


        return result;
    }
}
