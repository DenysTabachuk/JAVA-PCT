import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();

    public void increment() {
        count++;
    }

    public void decrement() {
        count--;
    }

    public synchronized void incrementSynchronizedMethod() {
        count++;
    }

    public synchronized void decrementSynchronizedMethod() {
        count--;
    }

    public void incrementSynchronizedBlock() {
        synchronized (this) {
            count++;
        }
    }

    public void decrementSynchronizedBlock() {
        synchronized (this) {
            count--;
        }
    }

    public void incrementObjectLocking(){
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public void decrementObjectLocking(){
        lock.lock();
        try {
            count--;
        }
        finally {
            lock.unlock();
        }
    }

    public void reset() {
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        final Counter counter = new Counter();

        //Without synchronization
        System.out.println("Without synchronization:");
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });
        Thread thread2 = new Thread(() -> {

            for (int i = 0; i < 100000; i++) {
                counter.decrement();
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("The last value of the counter without thread synchronization: " + counter.getCount());
        counter.reset();


        // synchronized methods
        System.out.println("\nWith synchronized methods:");
        Thread thread3 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.incrementSynchronizedMethod();
            }
        });
        Thread thread4 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.decrementSynchronizedMethod();
            }
        });

        thread3.start();
        thread4.start();
        thread3.join();
        thread4.join();
        System.out.println("The last value of the counter with synchronized methods: " + counter.getCount());



        // synchronized blocks
        System.out.println("\nWith synchronized blocks:");
        Thread thread5 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.incrementSynchronizedBlock();
            }
        });

        Thread thread6 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.decrementSynchronizedBlock();
            }
        });

        thread5.start();
        thread6.start();
        thread5.join();
        thread6.join();
        System.out.println("The last value of the counter with synchronized blocks: " + counter.getCount());


        // object locking
        System.out.println("\nWith object locking:");
        Thread thread7 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.incrementObjectLocking();
            }
        });

        Thread thread8 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
            counter.decrementObjectLocking();
        }
        });

        thread7.start();
        thread8.start();

        System.out.println("The last value of the counter with object locking: " + counter.getCount());
    }
}
