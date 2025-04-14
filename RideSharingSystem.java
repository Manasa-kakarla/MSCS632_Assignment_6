import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RideSharingSystem {

    // Task class
    static class Task {
        private final int id;
        public Task(int id) {
            this.id = id;
        }
        public void process() throws InterruptedException {
            Thread.sleep(1000); // Simulate work
        }
        public int getId() {
            return id;
        }
    }

    // Thread-safe Task Queue
    static class TaskQueue {
        private final Queue<Task> queue = new LinkedList<>();

        public synchronized void addTask(Task task) {
            queue.add(task);
            notifyAll();
        }

        public synchronized Task getTask() throws InterruptedException {
            while (queue.isEmpty()) {
                wait();
            }
            return queue.poll();
        }

        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    // Worker Thread
    static class Worker extends Thread {
        private final TaskQueue taskQueue;
        private final List<String> results;

        public Worker(TaskQueue queue, List<String> results) {
            this.taskQueue = queue;
            this.results = results;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Task task;
                    synchronized (taskQueue) {
                        if (taskQueue.isEmpty()) break;
                        task = taskQueue.getTask();
                    }

                    System.out.println(getName() + " started Task ID: " + task.getId());
                    task.process();

                    synchronized (results) {
                        results.add("Processed Task ID: " + task.getId());
                    }

                    System.out.println(getName() + " completed Task ID: " + task.getId());
                }
            } catch (InterruptedException e) {
                System.err.println(getName() + " was interrupted.");
            } catch (Exception e) {
                System.err.println(getName() + " encountered error: " + e.getMessage());
            }
        }
    }

    // Main Method
    public static void main(String[] args) {
        TaskQueue queue = new TaskQueue();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Create and add tasks
        for (int i = 1; i <= 10; i++) {
            queue.addTask(new Task(i));
        }

        // Start worker threads
        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Worker w = new Worker(queue, results);
            w.setName("Worker-" + (i + 1));
            w.start();
            workers.add(w);
        }

        // Wait for all workers
        for (Worker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
                System.err.println("Join interrupted: " + e.getMessage());
            }
        }

        // Write results
        try (FileWriter writer = new FileWriter("results_java.txt")) {
            for (String result : results) {
                writer.write(result + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }

        System.out.println("All tasks processed.");
    }
}

