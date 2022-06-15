package useapool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UseExecutorService {
  public static void delay() {
    try {
      Thread.sleep((int)(Math.random() * 2000) + 1000);
    } catch (InterruptedException ie) {
      System.out.println("huh, interrupted!");
    }
  }
  public static void main(String[] args) {
    ExecutorService es = Executors.newFixedThreadPool(2);
    for (var count = 0; count < 4; count++) {
      int jobId = count;
      es.submit(() -> {
        System.out.println(Thread.currentThread().getName()
            + " Started task " + jobId);
        delay();
        System.out.println("Task " + jobId + " finished");
      });
    }
  }
}
