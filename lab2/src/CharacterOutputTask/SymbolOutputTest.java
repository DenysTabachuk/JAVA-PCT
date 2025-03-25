package CharacterOutputTask;

public class SymbolOutputTest {
    public static void main(String args[]) {
        SymbolPrinter symbolPrinter1 = new SymbolPrinter('|');
        SymbolPrinter symbolPrinter2 = new SymbolPrinter('\\');
        SymbolPrinter symbolPrinter3 = new SymbolPrinter('/');

        Thread thread1 = new Thread(symbolPrinter1);
        Thread thread2 = new Thread(symbolPrinter2);
        Thread thread3 = new Thread(symbolPrinter3);

        thread1.start();
        thread2.start();
        thread3.start();
    }
}