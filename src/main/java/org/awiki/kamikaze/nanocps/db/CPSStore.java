package org.awiki.kamikaze.nanocps.db;

import java.util.Collection;

public interface CPSStore extends Store
{
  public static final String DB_NAME = "NANO_DB";
  
  Collection<Object> getConfirmedBlocksForWindow(long secondsBeforeNow);
}
