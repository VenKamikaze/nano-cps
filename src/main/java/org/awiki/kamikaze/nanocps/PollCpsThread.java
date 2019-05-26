package org.awiki.kamikaze.nanocps;

import java.util.concurrent.atomic.AtomicBoolean;

import org.awiki.kamikaze.nanocps.client.WebsocketSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PollCpsThread implements Runnable
{
  public final AtomicBoolean running = new AtomicBoolean(false);
  
  @Autowired
  private WebsocketSubscriber subscriber;
  
  @Override
  public void run() {
    running.set(true);
    
    while(running.get()) {
      try
      {
        Thread.sleep(10000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      subscriber.getConfirmationRates();
    }
  }
}
