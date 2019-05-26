package org.awiki.kamikaze.nanocps.client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.awiki.kamikaze.nanocps.db.ArangoStore;
import org.awiki.kamikaze.nanocps.message.SubscribeTopicMessage;
import org.awiki.kamikaze.nanocps.message.Topics;
import org.awiki.kamikaze.nanocps.message.UnsubscribeTopicMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.arangodb.util.MapBuilder;

@Component
public class WebsocketSubscriber
{
  static final List<Long> cpsPeriods = new ArrayList<Long>(5);
  static
  {
    cpsPeriods.add(60L);
    cpsPeriods.add(120L);
    cpsPeriods.add(300L);
    cpsPeriods.add(600L);
    cpsPeriods.add(3600L);
  }

  @Autowired
  private ArangoStore store;

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
      Collection<Object> confirmedBlocksInPeriod = getConfirmedBlocksForWindow(period);
      buff.append("\nPeriod: ").append(Long.divideUnsigned(period, 60)).append("-min\n");
      buff.append("Total blocks: ").append(confirmedBlocksInPeriod.size()).append("\n");
      buff.append("CPS: ").append(
              new BigDecimal(confirmedBlocksInPeriod.size()).divide(new BigDecimal(period.longValue()), 2,
                      RoundingMode.HALF_UP))
              .append("\n");
    }
    return buff.toString();
  }

  public String getConfirmationRates(long periodInSec)
  {
    StringBuffer buff = new StringBuffer(1000);
    Collection<Object> confirmedBlocksInPeriod = getConfirmedBlocksForWindow(periodInSec);
    buff.append("\nPeriod: ").append(periodInSec).append("-sec\n");
    buff.append("Total blocks: ").append(confirmedBlocksInPeriod.size()).append("\n");
    buff.append("CPS: ").append(
            new BigDecimal(confirmedBlocksInPeriod.size()).divide(new BigDecimal(periodInSec), 2, RoundingMode.HALF_UP))
            .append("\n");
    return buff.toString();
  }

  private Collection<Object> getConfirmedBlocksForWindow(long secondsBeforeNow)
  {
    String aql = "FOR t IN " + store.getName()
            + " FILTER t.topic == @topic "
            + " FILTER TO_NUMBER(t.time) >= TO_NUMBER(@startTime) " /*
                                                                     * and t.time >= @startTime and t.time <= DATE_NOW()
                                                                     */ + " RETURN t";
    MapBuilder paramBuilder = new MapBuilder();
    paramBuilder.put("startTime", Instant.now().minusSeconds(secondsBeforeNow).toEpochMilli());
    paramBuilder.put("topic", Topics.CONFIRMATION);
    System.out.println("AQL: " + aql);
    System.out.println("Params: " + paramBuilder.get().toString());
    return store.queryItems(aql, paramBuilder.get());
  }
}
