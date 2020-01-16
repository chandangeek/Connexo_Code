package com.elster.protocols.hdlc;



/**
 * Implementation for an trigger.
 * <P>
 * Maybe java.util.concurrent.locks.Condition is an alternative.
 *
 * @author osse
 */
public class Trigger
{
  private boolean triggered = false;


  /**
   * Notifies waiting threads.
   */
  public synchronized void trigger()
  {
    triggered = true;
    notifyAll();
  }

  /**
   * Waits for the trigger for an specified time.
   *
   * @param timeoutMillis Maximum time to wait.
   * @param reset If <b>true</b> the trigger will be reseted
   * @throws InterruptedException
   */
  public synchronized void waitForTrigger(int timeoutMillis, boolean reset) throws InterruptedException
  {
    if (!triggered)
    {
      long waitTo = System.currentTimeMillis() + timeoutMillis;
      long waitTime = waitTo - System.currentTimeMillis();
      while (!triggered && waitTime > 0)
      {
        wait(waitTime);
        waitTime = waitTo - System.currentTimeMillis();
      }
    }
    if (reset)
    {
      triggered = false;
    }
  }

  /**
   * Waits for the trigger.
   *
   * @param reset If <b>true</b> the trigger will be reseted
   * @throws InterruptedException
   */
  public synchronized void waitForTrigger(boolean reset) throws InterruptedException
  {
    while (!triggered)
    {
      wait();
    }
    if (reset)
    {
      triggered = false;
    }
  }

  /**
   * Resets the trigger.
   *
   */
  public synchronized void reset()
  {
    triggered = false;
  }

  /**
   * Retruns <b>true</b> if the trigger is set.
   *
   * @return <b>true</b> if the trigger is set.
   */
  public synchronized boolean isTriggered()
  {
    return triggered;
  }

}
