package ProducerConsumerTask;

public class ProducerConsumerExample {
    public static void main(String[] args) {
        Drop drop = new Drop();
        (new Thread(new Producer(drop, 1000))).start();
        (new Thread(new Consumer(drop))).start();
    }
}
