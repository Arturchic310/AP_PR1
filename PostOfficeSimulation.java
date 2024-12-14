import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class Mail {
    private final String sender;
    private final String receiver;
    private final String content;

    public Mail(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Лист від " + sender + " для " + receiver + " => " + content;
    }
}

class PostOfficeWorker implements Runnable {
    private final BlockingQueue<Mail> mailQueue;
    private volatile boolean isOpen;

    public PostOfficeWorker(BlockingQueue<Mail> mailQueue) {
        this.mailQueue = mailQueue;
        this.isOpen = true;
    }

    public void closeOffice() {
        isOpen = false;
    }

    @Override
    public void run() {
        System.out.println("Працівник пошти готовий обробляти листи.");
        while (isOpen || !mailQueue.isEmpty()) {
            try {
                Mail mail = mailQueue.take(); // Blocks if no mail is available
                System.out.println("Обробляється: " + mail);
                Thread.sleep(1500); // Simulate time taken to process each mail
            } catch (InterruptedException e) {
                System.out.println("Роботу працівника перервано: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Пошта закрита. Листи більше не обробляються.");
    }
}

class Sender implements Runnable {
    private final String name;
    private final BlockingQueue<Mail> mailQueue;

    public Sender(String name, BlockingQueue<Mail> mailQueue) {
        this.name = name;
        this.mailQueue = mailQueue;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 3; i++) {
            Mail mail = new Mail(name, "Приймач" + i, "Вміст пошти " + i);
            try {
                mailQueue.put(mail); // Blocks if the queue is full
                System.out.println(name + " відправив: " + mail);
                Thread.sleep(500); // Simulate delay between sending mails
            } catch (InterruptedException e) {
                System.out.println(name + " перервано: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class PostOfficeSimulation {
    public static void main(String[] args) {
        BlockingQueue<Mail> mailQueue = new LinkedBlockingQueue<>(10);
        PostOfficeWorker worker = new PostOfficeWorker(mailQueue);

        Thread workerThread = new Thread(worker);
        workerThread.start();

        Thread[] senders = new Thread[3];
        for (int i = 0; i < senders.length; i++) {
            senders[i] = new Thread(new Sender("Відправник" + (i + 1), mailQueue));
            senders[i].start();
        }

        for (Thread sender : senders) {
            try {
                sender.join();
            } catch (InterruptedException e) {
                System.out.println("Головний потік перервано під час очікування відправників: " + e.getMessage());
            }
        }

        worker.closeOffice();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Головний потік перервано під час очікування працівника: " + e.getMessage());
        }

        System.out.println("Симуляція завершена.");
    }
}
