package org.awiki.kamikaze.nanocps.message;

public class SubscribeTopicMessage extends ActionMessage
{
  private String topic;

  public SubscribeTopicMessage(String topic)
  {
    this(topic, null, null);
  }

  public SubscribeTopicMessage(String topic, Boolean ack)
  {
    this(topic, ack, null);
  }

  public SubscribeTopicMessage(String topic, Boolean ack, Long id)
  {
    super(Actions.SUBSCRIBE);
    setAck(ack);
    setId(id);
    setTopic(topic);
  }

  public String getTopic()
  {
    return topic;
  }

  public void setTopic(String name)
  {
    this.topic = name;
  }

  @Override
  public String toString()
  {
    return "SubscribeTopicMessage [topic=" + topic + ", super.toString()=" + super.toString() + "]";
  }
  
}
