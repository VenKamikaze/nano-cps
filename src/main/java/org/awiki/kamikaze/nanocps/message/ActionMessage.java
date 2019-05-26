package org.awiki.kamikaze.nanocps.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ActionMessage
{
  private String action;
  private Boolean ack;
  private Long id;

  public ActionMessage()
  {
  }

  public ActionMessage(String name)
  {
    this.action = name;
  }

  public String getAction()
  {
    return action;
  }

  public void setAction(String name)
  {
    this.action = name;
  }

  public Boolean getAck()
  {
    return ack;
  }

  public void setAck(Boolean ack)
  {
    this.ack = ack;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  @Override
  public String toString()
  {
    return "ActionMessage [action=" + action + ", ack=" + ack + ", id=" + id + "]";
  }

  
}
