package org.awiki.kamikaze.nanocps.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awiki.kamikaze.nanocps.db.H2Store;
import org.awiki.kamikaze.nanocps.message.SubscribeTopicMessage;
import org.awiki.kamikaze.nanocps.message.UnsubscribeTopicMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebsocketSubscriber
{
  static final List<Long> cpsPeriods = new ArrayList<Long>(6);
  static
  {
    cpsPeriods.add(10L);
    cpsPeriods.add(60L);
    cpsPeriods.add(120L);
    cpsPeriods.add(300L);
    cpsPeriods.add(600L);
    cpsPeriods.add(3600L);
  }

  static final Map<Long, BigDecimal> highestCpsSeen = new HashMap<Long, BigDecimal>(6);

  @Autowired
  private H2Store store;

  @Value("${nodeWebsocketUri}")
  private String nodeWebsocketAddress;

  public String subscribeTo(String topic)
  {
    store.createStore(topic);
    NanoWebsocketClient c = new NanoWebsocketClient(nodeWebsocketAddress, store);
    try
    {
      c.subscribe(new SubscribeTopicMessage(topic, Boolean.TRUE));
    }
    catch (NanoClientException e)
    {
      return "failed";
    }
    return "success";
  }

  public String unsubscribeFrom(String topic)
  {
    NanoWebsocketClient c = new NanoWebsocketClient(nodeWebsocketAddress, store);
    try
    {
      c.unsubscribe(new UnsubscribeTopicMessage(topic, Boolean.TRUE));
    }
    catch (NanoClientException e)
    {
      return "failed";
    }
    return "success";
  }

  public String getConfirmationRates()
  {
    StringBuffer buff = new StringBuffer(1000);
    for (Long period : cpsPeriods)
    {
      buff.append(getConfirmationRates(period));
    }
    return buff.toString();
  }

  private void recordHighestCps(long period, BigDecimal cps) {
    BigDecimal existing = highestCpsSeen.get(period);
    if(existing != null) {
      if(existing.compareTo(cps) == -1) {
        highestCpsSeen.put(period, cps);
      }
    }
    else {
      highestCpsSeen.put(period, cps);
    }
  }

  public String getConfirmationRates(long periodInSec)
  {
    StringBuffer buff = new StringBuffer(1000);
    Collection<Object> confirmedBlocksInPeriod = store.getConfirmedBlocksForWindow(periodInSec);
    BigDecimal cps = new BigDecimal(confirmedBlocksInPeriod.size()).divide(new BigDecimal(periodInSec), 2,
            RoundingMode.HALF_UP);
    recordHighestCps(periodInSec, cps);
    buff.append("\nPeriod: ");
    if (periodInSec < 60 || periodInSec % 60 != 0)
    {
      buff.append(periodInSec).append("-sec\n");
    }
    else
    {
      buff.append(periodInSec / 60L).append("-min\n");
    }
    buff.append("Total blocks: ").append(confirmedBlocksInPeriod.size()).append("\n");
    buff.append("CPS: ").append(cps).append("\n");
    buff.append("Highest CPS Seen: ").append(highestCpsSeen.get(periodInSec)).append("\n");
    return buff.toString();
  }

}
