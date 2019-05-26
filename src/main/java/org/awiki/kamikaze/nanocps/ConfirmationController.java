package org.awiki.kamikaze.nanocps;

import java.util.ArrayList;
import java.util.List;

import org.awiki.kamikaze.nanocps.client.WebsocketSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConfirmationController
{
  @Autowired
  private TaskExecutor taskExecutor;
  
  @Autowired
  private ApplicationContext applicationContext;
  
  @Autowired
  private WebsocketSubscriber subscriber;
  
  private List<String> topicsSubscribed = new ArrayList<>();
  
  private PollCpsThread poller = null;
  
  @RequestMapping(value = "/subscribe/{topic}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String subscribe(@PathVariable String topic) {
    String output = "Already subscribed to: " + topic;
    if(! topicsSubscribed.contains(topic))
    {
      output = subscriber.subscribeTo(topic);
      topicsSubscribed.add(topic);
      if(poller == null) {
        poller = applicationContext.getBean(PollCpsThread.class);
        taskExecutor.execute(poller);
      }
      else if (! poller.running.get() ) {
        taskExecutor.execute(poller);
      }
    }
    
    return output;
  }
  
  @RequestMapping(value = "/unsubscribe/{topic}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String unsubscribe(@PathVariable String topic) {
    topicsSubscribed.remove(topic);
    if(poller != null) {
      poller.running.set(false);
    }
    return subscriber.unsubscribeFrom(topic);
  }
  
  @RequestMapping(value = "/cps", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String cps()
  {
    return subscriber.getConfirmationRates();
  }

  @RequestMapping(value = "/cps/{periodInSeconds}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  public String cps(@PathVariable Long periodInSeconds)
  {
    return subscriber.getConfirmationRates(periodInSeconds);
  }
}
