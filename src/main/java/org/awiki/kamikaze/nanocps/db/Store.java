package org.awiki.kamikaze.nanocps.db;

import java.util.Collection;
import java.util.Map;

public interface Store
{
  void createStore(String storeName);
  boolean isInitialised();
  void addItem(String item);
  void removeItem(String item);
  String getName();
  Collection<Object> queryItems(String query, Map<String, Object> bindVars);
}
