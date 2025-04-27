import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int numChannels = 5;             // кількість каналів обслуговування
        int queueCapacity = 8;           // максимальний розмір черги
        int simulationTimeMs = 6000;     // тривалість симуляції

        int meanIncomeTimeMs = 5;        // середній час між надходженнями
        int meanServiceTimeMs = 30 ;     // середній час обслуговування одного клієнта

        int numSimulations = 10;         // кількість симуляцій


        // Виконуємо симуляції
        SimulationResult simulationResult = runParallelSimulations(numChannels, queueCapacity, simulationTimeMs, meanIncomeTimeMs, meanServiceTimeMs, numSimulations);

        // Розрахунок теоретичних оцінок
        calculateTheoreticalEstimates(numChannels, queueCapacity, meanIncomeTimeMs, meanServiceTimeMs);


        System.out.println("\n====== Simulation Results ===========");
        System.out.println("Rejection probability: " + String.format("%.2f", simulationResult.rejectionProbability * 100) + " %");
        System.out.println("Average queue length: " + String.format("%.2f", simulationResult.averageQueueLength));
        System.out.println("=======================================");

    }


    public static SimulationResult runParallelSimulations(int numChannels, int queueCapacity, int simulationTimeMs,
                                                          int meanIncomeTimeMs, int meanServiceTimeMs, int numSimulations) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<Result>> futures = new ArrayList<>();

        for (int i = 0; i < numSimulations; i++) {
            futures.add(executorService.submit(() -> {
                MultiChannelMSS mss = new MultiChannelMSS(numChannels, queueCapacity, simulationTimeMs, meanIncomeTimeMs, meanServiceTimeMs);
                return mss.simulate();
            }));
        }

        double totalAverageQueueLength = 0;
        double totalRejectionProbability = 0;

        for (Future<Result> future : futures) {
            Result result = future.get();
            totalAverageQueueLength += result.getAverageQueueLength();
            totalRejectionProbability += result.getProbabilityOfRejection();
        }

        executorService.shutdown();

        return new SimulationResult(totalAverageQueueLength / numSimulations, totalRejectionProbability / numSimulations);
    }


    // Теоретичний метод для обчислення показників для черги M/M/c/K
    private static void calculateTheoreticalEstimates(int c, int K, int meanIncomeTimeMs, int meanServiceTimeMs) {
        double lambda = 1000.0 / meanIncomeTimeMs;
        double mu = 1000.0 / meanServiceTimeMs;
        double rho = lambda / (c * mu);

        // Обчислення p0 - ймовірність нульових клієнтів у системі
        double p0 = 0;
        double sum = 0;

        // Перша частина суми (n=0 до c-1)
        for (int n = 0; n < c; n++) {
            sum += Math.pow(lambda / mu, n) / factorial(n);
        }

        // Друга частина суми (n=c до K)
        if (rho == 1) {
            sum += Math.pow(lambda / mu, c) / factorial(c) * (K - c + 1);
        } else {
            sum += Math.pow(lambda / mu, c) / factorial(c) *
                    (1 - Math.pow(rho, K - c + 1)) / (1 - rho);
        }

        p0 = 1 / sum;

        // Ймовірність відмови (pK) - ймовірність, що система заповнена
        double pK = p0 * Math.pow(lambda / mu, K) / (factorial(c) * Math.pow(c, K - c));

        // Середня довжина черги Lq
        double Lq;
        if (rho == 1) {
            Lq = p0 * Math.pow(lambda / mu, c) * (K - c) * (K - c + 1) / (2 * factorial(c));
        } else {
            Lq = p0 * Math.pow(lambda / mu, c) * rho /
                    (factorial(c) * Math.pow(1 - rho, 2)) *
                    (1 - Math.pow(rho, K - c + 1) - (1 - rho) * (K - c + 1) * Math.pow(rho, K - c)) * 2;
        }


        System.out.println("\n==== Theoretical Measures (M/M/" + c + "/" +  K + ") ====");
        System.out.println("Arrival rate (λ): " + String.format("%.2f", lambda) + " customers/second");
        System.out.println("Service rate (μ): " + String.format("%.2f", mu) + " customers/second");
        System.out.println("Traffic intensity (ρ): " + String.format("%.2f", rho));
        System.out.println("Probability of empty system (p₀): " + String.format("%.4f", p0));
        System.out.println("Rejection probability: " + String.format("%.2f", pK ) + " %");
        System.out.println("Average queue length: " + String.format("%.2f", Lq));
        System.out.println("=======================================\n");

    }


    private static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }


    static class SimulationResult {
        double averageQueueLength;
        double rejectionProbability;

        SimulationResult(double averageQueueLength, double rejectionProbability) {
            this.averageQueueLength = averageQueueLength;
            this.rejectionProbability = rejectionProbability;
        }
    }
}
