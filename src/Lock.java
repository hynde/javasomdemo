// Class Lock - a simple lock
// 
// Author:
//   Jussi Hynninen <hynde@iki.fi>
// Date:
//   July 1996

class Lock {
  private boolean locked = false;

  public Lock() {
    locked = false;
  }

  // Lock if not locked, otherwise wait
  public synchronized void lock() {
    while (locked == true) {
      try {
	wait();          
      } catch (InterruptedException e) { }
    }
    locked = true;
    notify();
  }    

  // Unlock lock, anyone can unlock the lock (useful for my purposes, could
  // be better)
  public synchronized void unlock() {
    locked = false;
    notify();
  }    
}
