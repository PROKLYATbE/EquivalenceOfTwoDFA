package asd;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;

public class Main {

    private static final String FILE_NAME = "F.txt";
    private static final Semaphore semaphore = new Semaphore(1);

    public static void main(String[] args) {
        Thread process1 = new Thread(new FileProcess("Process 1"));
        Thread process2 = new Thread(new FileProcess("Process 2"));

        process1.start();
        process2.start();
    }

    static class FileProcess implements Runnable {
        private final String processName;

        FileProcess(String name) {
            this.processName = name;
        }

        @Override
        public void run() {
            while (true) {
                writeFile();
                readFile();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void readFile() {
            try {
                semaphore.acquire(); // Захват семафора
                try (RandomAccessFile file = new RandomAccessFile(FILE_NAME, "r")) {
                    String line;
                    System.out.println(processName + " reading file...");
                    while ((line = file.readLine()) != null) {
                        System.out.println(processName + " read: " + line);
                    }
                } catch (IOException e) {
                    System.err.println(processName + " error reading file: " + e.getMessage());
                } finally {
                    semaphore.release(); // Освобождение семафора
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void writeFile() {
            try {
                semaphore.acquire(); // Захват семафора
                try (RandomAccessFile file = new RandomAccessFile(FILE_NAME, "rw")) {
                    System.out.println(processName + " writing in file...");
                    String data = processName + " writes data at " + System.currentTimeMillis() % 100000000;
                    file.seek(file.length());
                    file.writeBytes(data + "\n");
                    System.out.println(processName + " wrote: " + data);
                } catch (IOException e) {
                    System.err.println(processName + " error writing to file: " + e.getMessage());
                } finally {
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}