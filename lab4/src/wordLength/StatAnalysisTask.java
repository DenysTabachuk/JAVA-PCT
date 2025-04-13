package wordLength;

import java.util.List;
import java.util.concurrent.*;


class StatsResult {
    double average;
    double standardDeviation;

    public StatsResult(double average, double standardDeviation) {
        this.average = average;
        this.standardDeviation = standardDeviation;
    }

    public double getAverage() {
        return average;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void printResult() {
        System.out.printf("Average: %.2f\n", average);
        System.out.printf("Standard Deviation: %.2f\n", standardDeviation);
    }
}


class StatAnalysisTask {

    public static StatsResult computeStatisticsSequential(List<Integer> data) {
        double sum = 0;
        for (int value : data) {
            sum += value;
        }

        double average = sum / data.size();

        double deviationSum = 0;
        for (int value : data) {
            double diff = value - average;
            deviationSum += diff * diff;
        }

        double standardDeviation = Math.sqrt(deviationSum / data.size());

        return new StatsResult(average, standardDeviation);
    }

    public static StatsResult computeStatisticsParallel(List<Integer> data) {
        ForkJoinPool pool = new ForkJoinPool();

        double sum = pool.invoke(new SumTask(data, 0, data.size()));
        double average = sum / data.size();

        double deviationSum = pool.invoke(new DeviationTask(data, average, 0, data.size()));
        double standardDeviation = Math.sqrt(deviationSum / data.size());

        return new StatsResult(average, standardDeviation);
    }
}


class SumTask extends RecursiveTask<Double> {
    private final List<Integer> data;
    private final int start, end;
    private static final int THRESHOLD = 10000;

    public SumTask(List<Integer> data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Double compute() {
        if (end - start <= THRESHOLD) {
            double sum = 0;
            for (int i = start; i < end; i++) {
                sum += data.get(i);
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            SumTask left = new SumTask(data, start, mid);
            SumTask right = new SumTask(data, mid, end);

            left.fork();
            double rightResult = right.compute();
            double leftResult = left.join();

            return leftResult + rightResult;
        }
    }
}


class DeviationTask extends RecursiveTask<Double> {
    private final List<Integer> data;
    private final double average;
    private final int start, end;
    private static final int THRESHOLD = 10000;

    public DeviationTask(List<Integer> data, double average, int start, int end) {
        this.data = data;
        this.average = average;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Double compute() {
        if (end - start <= THRESHOLD) {
            double sum = 0;
            for (int i = start; i < end; i++) {
                double diff = data.get(i) - average;
                sum += diff * diff;
            }
            return sum;
        } else {
            int mid = (start + end) / 2;
            DeviationTask left = new DeviationTask(data, average, start, mid);
            DeviationTask right = new DeviationTask(data, average, mid, end);

            left.fork();
            double rightResult = right.compute();
            double leftResult = left.join();

            return leftResult + rightResult;
        }
    }
}
