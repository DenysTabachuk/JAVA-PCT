package BankTask;

class TransferThread extends Thread {
    private final Bank bank;
    private final int fromAccount;
    private final int maxAmount;
    private final int method;
    private static final int REPS = 10;

    public TransferThread(Bank b, int from, int max, int method) {
        bank = b;
        fromAccount = from;
        maxAmount = max;
        this.method = method;
    }

    @Override
    public void run() {
        while (true) {
            for (int i = 0; i < REPS; i++) {
                int toAccount = (int) (bank.size() * Math.random());
                int amount = (int) (maxAmount * Math.random() / REPS);

                switch (method) {
                    case 1 -> bank.asynchronousTransfer(fromAccount, toAccount, amount);
                    case 2 -> bank.synchronizedMethodTransfer(fromAccount, toAccount, amount);
                    case 3 -> bank.objectLockingTransfer(fromAccount, toAccount, amount);
                    case 4 -> bank.synchronizedBlockTransfer(fromAccount, toAccount, amount);
                    case 5 -> bank.atomicTransfer(fromAccount, toAccount, amount);
                    default -> throw new IllegalArgumentException("Невірний вибір методу");
                }
            }
        }
    }
}

