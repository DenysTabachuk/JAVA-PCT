package ProducerConsumerTask;

import java.util.Random;

public class Producer implements Runnable {
    private Drop drop;
    private final int arraySize;
    private int[] numArray;

    public Producer(Drop drop, int size) {
        this.drop = drop;
        this.arraySize = size;
        this.numArray = new int[size];
        fillArray();
    }

    private void fillArray() {
        for (int i = 0; i < arraySize; i++) {
            numArray[i] = i + 1;
        }
    }


    public void run() {
        int array[] = {

        };
        Random random = new Random();

        for (int i = 0; i < arraySize; i++) {
            drop.put(numArray[i]);
            try {
                Thread.sleep(random.nextInt(5));
            } catch (InterruptedException e) {}
        }
        drop.put(-1);
    }
}