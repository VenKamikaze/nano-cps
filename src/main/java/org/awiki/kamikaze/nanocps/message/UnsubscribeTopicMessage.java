package org.awiki.kamikaze.nanocps.message;

public class UnsubscribeTopicMessage extends ActionMessage
{

  private String topic;

  public UnsubscribeTopicMessage(String topic)
  {
    this(topic, null, null);
  }

  public UnsubscribeTopicMessage(String topic, Boolean ack)
  {
    this(topic, ack, null);
  }

  public UnsubscribeTopicMessage(String topic, Boolean ack, Long id)
  {
    super(Actions.UNSUBSCRIBE);
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
    return "UnsubscribeTopicMessage [topic=" + topic + ", super.toString()=" + super.toString() + "]";
  }
}
