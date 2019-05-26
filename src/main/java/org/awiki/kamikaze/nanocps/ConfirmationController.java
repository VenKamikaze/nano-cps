package org.awiki.kamikaze.nanocps;

import org.awiki.kamikaze.nanocps.client.WebsocketSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
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
  private WebsocketSubscriber subscriber;
  
  @RequestMapping(value = "/subscribe/{topic}", method = RequestMethod.GET)
  @ResponseBody
  public String subscribe(@PathVariable String topic) {
    return subscriber.subscribeTo(topic);
  }
  
  @RequestMapping(value = "/unsubscribe/{topic}", method = RequestMethod.GET)
  @ResponseBody
  public String unsubscribe(@PathVariable String topic) {
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
