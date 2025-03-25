package CharacterOutputTask;

public class SymbolPrinter implements Runnable {
    private final char symbol;
    private static final Object lock = new Object();
    private static int turn = 0;
    private final int myTurn;

    private static final int SYMBOLS_IN_ROW = 66;
    private static final int ROWS = 90;
    private static int counter = 0;

    public SymbolPrinter(char symbol) {
        this.myTurn = counter++;
        this.symbol = symbol;
        System.out.println("SymbolPrinter " + myTurn + " initialized");
    }

    @Override
    public void run() {
        for (int row = 0; row < ROWS; row++) {
            for (int i = 0; i < SYMBOLS_IN_ROW; i++) {
                synchronized (lock) {
                    while (turn != myTurn) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    System.out.print(symbol);
                    turn = (turn + 1) % 3;

                    // Якщо це останній символ рядка і потік з '/', додаємо новий рядок
                    if (i == SYMBOLS_IN_ROW - 1 && myTurn == 2) {
                        System.out.print("\n");
                    }

                    lock.notifyAll();
                }
            }
        }
    }


//    @Override
//    public void run() {
//        for (int row = 0; row < ROWS; row++) {
//            for (int i = 0; i < SYMBOLS_IN_ROW; i++) {
//                System.out.print(symbol);
//                if (i == SYMBOLS_IN_ROW - 1 && myTurn == 2) {
//                    System.out.print("\n");
//                }
//            }
//        }
//    }

}

