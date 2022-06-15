package useapool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class UseCallable {
  public static void delay() {
    try {
      Thread.sleep((int) (Math.random() * 2000) + 1000);
    } catch (InterruptedException ie) {
      System.out.println("huh, interrupted!");
    }
  }

  static class MyCallableTask implements Callable<String> {
    private static int nextJobId = 0;
    private int jobId = nextJobId++;

    @Override
    public String call() throws Exception {
      System.out.println(Thread.currentThread().getName()
          + " Started task " + jobId);
      delay();
      System.out.println("Task " + jobId + " finished");
      return "JobId " + jobId + " returned this message";
    }
  }

  public static void main(String[] args) {
    ExecutorService es = Executors.newFixedThreadPool(2);
    int TASK_COUNT = 6;
    List<Future<String>> handles = new ArrayList<>();
    for (var count = 0; count < TASK_COUNT; count++) {
      int jobId = count;
      handles.add(es.submit(new MyCallableTask()));
    }

    handles.get(2).cancel(true);
    // 1) refuse any more tasks
    // 2) when the current tasks have all completed
    //    then kill the worker threads
    es.shutdown();

    // 1) refuse any new tasks
    // 2) EMPTY THE input queue
    // 3) Interrupt the existing tasks
    // 4) shutdown when tasks have completed
//    es.shutdownNow();
//    System.out.println("All tasks submitted");

    while (handles.size() > 0) {
      Iterator<Future<String>> ifs = handles.iterator();
      while (ifs.hasNext()) {
        Future<String> handle = ifs.next();
        if (handle.isDone()) {
          ifs.remove();
          try {
            String result = handle.get();
            System.out.println("A task returned: " + result);
          } catch (ExecutionException ex) {
            System.out.println("Task threw an exception: " + ex.getCause());
          } catch (InterruptedException e) {
            System.out.println("Main task was interrupted");
          } catch (CancellationException ce) {
            System.out.println("Silly, you just tried to get the results" +
                " from a canceled task!");
          }
        }
      }
    }
    System.out.println("All tasks completed....");
  }
}
