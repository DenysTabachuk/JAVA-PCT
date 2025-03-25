package ProducerConsumerTask;

import java.util.Random;

public class Consumer implements Runnable {
    private Drop drop;

    public Consumer(Drop drop) {
        this.drop = drop;
    }

    public void run() {
        Random random = new Random();
        for (int message = drop.take();  message != -1; message = drop.take()) {
            System.out.format("MESSAGE RECEIVED: %s%n", message);
            try {
                Thread.sleep(random.nextInt(5));
            } catch (InterruptedException e) {}
        }
        System.out.println("CONSUMER FINISHED RECEIVING DATA.");
    }
}