package locking;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ImproveThisQueue<E> {
  private Object rendezvous = new Object();
  private ReentrantLock lock = new ReentrantLock();
  private Condition notFull = lock.newCondition();
  private Condition notEmpty = lock.newCondition();
  private E[] data = (E[]) new Object[10];
  private int count = 0;

  public void put(E e) throws InterruptedException {
    lock.lock();
    try {
//    synchronized (rendezvous) {
      while (count == 10) { // MUST BE A LOOP (can waken for wrong reasons)
//        rendezvous.wait(); // final method in Object
        // unlocks the lock!!!
        // must be transactionally stable!!
        // re-locks before continuing
        notFull.await();
      }

      data[count++] = e;
//      rendezvous.notify();
      // functionall correct with multiple producer/consumer
      // but horribly inefficient.
      // use ReentrantLock instead
//      rendezvous.notifyAll();
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public E take() throws InterruptedException {
    lock.lock();
    try {
//    synchronized (rendezvous) {
      while (count == 0) {
//        rendezvous.wait();
        notEmpty.await();
      }
      E rv = data[0];
      System.arraycopy(data, 1, data, 0, --count);
      // FAILS if we have multiple producers/consumers
      // no guarantee or wake up order!!!
//      rendezvous.notify();
//      rendezvous.notifyAll();
      notFull.signal();
      return rv;
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws Throwable {
    ImproveThisQueue<int[]> bq = new ImproveThisQueue<>();
    Thread prod = new Thread(() -> {
      try {
        for (int i = 0; i < 1_000; i++) {
          int[] data = {-1, i}; // transactionally "unsound"
          if (i < 100) Thread.sleep(1); // test "empty" queue
          data[0] = i;

          if (i == 500) data[0] = -1;

          bq.put(data);
          data = null;
        }
      } catch (InterruptedException ie) {
        System.out.println("Unexpected!");
      }
    });
    Thread cons = new Thread(() -> {
      try {
        for (int i = 0; i < 1_000; i++) {
          if (i > 900) Thread.sleep(1);
          int[] data = bq.take();

          if (data[0] != data[1] || data[0] != i) {
            System.out.println("***** ERROR AT " + i);
          }
        }
      } catch (InterruptedException ie) {
        System.out.println("Unexpected!");
      }
    });
    prod.start();
    cons.start();
    prod.join();
    cons.join();
    System.out.println("all done");
  }
}
