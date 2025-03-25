package BankTask;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

class Bank {
    public static final int NTEST = 1000;

    private final AtomicIntegerArray accountsAtomic;
    private final AtomicLong ntransactsAtomic = new AtomicLong(0);

    private final int[] accounts;
    private long ntransacts = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition sufficientFunds = lock.newCondition();

    public Bank(int n, int initialBalance){
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++)
            accounts[i] = initialBalance;
        ntransacts = 0;

        accountsAtomic = new AtomicIntegerArray(n);
        for (int i = 0; i < accountsAtomic.length(); i++)
            accountsAtomic.set(i, initialBalance);
    }

    public void asynchronousTransfer(int from, int to, int amount) {
//        while (accounts[from] < amount) {
//            Thread.yield();
//        }
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;

        if (ntransacts % NTEST == 0)
            test(false);
    }


    public synchronized void synchronizedMethodTransfer(int from, int to, int amount) {
        while (accounts[from] < amount) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;

        notifyAll();
        if (ntransacts % NTEST == 0)
            test(false);
    }

    public void objectLockingTransfer(int from, int to, int amount) {
        lock.lock();
        try {
            while (accounts[from] < amount) {
                sufficientFunds.await();
            }

            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;

            sufficientFunds.signalAll();

            if (ntransacts % NTEST == 0)
                test(false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void synchronizedBlockTransfer(int from, int to, int amount) {
        synchronized (lock) {
            while (accounts[from] < amount) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;

            lock.notifyAll();

            if (ntransacts % NTEST == 0)
                test(false);
        }
    }

    public void atomicTransfer(int from, int to, int amount) {

        if (accountsAtomic.get(from) < amount) {
            Thread.yield();
        }
        else{
            accountsAtomic.addAndGet(to, amount);
            accountsAtomic.addAndGet(from, -amount);
            ntransactsAtomic.incrementAndGet();

            test(true);
        }
    }


    public void test(boolean useAtomic) {
        int sum = 0;
        long transactions;

        if (useAtomic) {
            for (int i = 0; i < accountsAtomic.length(); i++) {
                sum += accountsAtomic.get(i);
            }
            transactions = ntransactsAtomic.get();
        } else {
            for (int i = 0; i < accounts.length; i++) {
                sum += accounts[i];
            }
            transactions = ntransacts;
        }

        System.out.println("Transactions: " + transactions + " Sum: " + sum);
    }


    public int size(){
        return accounts.length;
    }
}