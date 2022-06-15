package makeapool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MyThreadPool {
  private Thread[] workers;
  private BlockingQueue<Runnable> input;

  private Runnable workerBody = new Runnable() {
    public void run() {
        try {
          while (true) {
            input.take().run();
          }
        } catch (InterruptedException ie) {
          System.out.println("shutdown requested");
        }
    }
  };
  public MyThreadPool(int workerCount, BlockingQueue<Runnable> input) {
    this.workers = new Thread[workerCount];
    this.input = input;
    for (var idx = 0; idx < workerCount; idx++) {
      workers[idx] = new Thread(workerBody);
      workers[idx].start();
    }
  }

  public static void delay() {
    try {
      Thread.sleep((int)(Math.random() * 2000) + 1000);
    } catch (InterruptedException ie) {
      System.out.println("huh, interrupted!");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    BlockingQueue<Runnable> jobQueue = new ArrayBlockingQueue(4);
    MyThreadPool pool = new MyThreadPool(2, jobQueue);

    for (var count = 0; count < 4; count++) {
      int jobId = count;
      jobQueue.put(() -> {
        System.out.println(Thread.currentThread().getName()
            + " Started task " + jobId);
        delay();
        System.out.println("Task " + jobId + " finished");
      });
    }
  }
}
