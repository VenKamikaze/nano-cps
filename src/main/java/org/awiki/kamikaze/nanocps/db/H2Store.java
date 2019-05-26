package org.awiki.kamikaze.nanocps.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.awiki.kamikaze.nanocps.message.Topics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class H2Store implements CPSStore
{
  private JsonFactory jsonFactory = new JsonFactory();
  private ObjectMapper jMapper = new ObjectMapper();

  static Connection con = null;

  private String tableName = null;

  @Autowired
  public H2Store(@Value("${driverClassName}") String driver, @Value("${dbUrl}") String connString)
  {
    try
    {
      System.out.println(driver);
      System.out.println(connString);
      Class.forName(driver);
      con = DriverManager.getConnection(connString);
      con.setAutoCommit(true);
      System.out.println("Connection successful: " + connString);
    }
    catch (SQLException | ClassNotFoundException e)
    {
      System.err.println("Failed to connect: " + connString + "; " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public boolean isInitialised()
  {
    try
    {
      return con != null && con.isValid(2);
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Unable to check if connection was initialised.", e);
    }
  }

  @Override
  public void createStore(String storeName)
  {
    try (Statement st = con.createStatement())
    {
      final String ddl = "CREATE TABLE IF NOT EXISTS " + Topics.CONFIRMATION + "( "
              + " id bigint auto_increment,"
              + " unixtime bigint not null,"
              + " hash varchar(256) not null,"
              + " PRIMARY KEY(id)) ";
      st.executeUpdate(ddl);
      System.out.println("Table created: " + Topics.CONFIRMATION);
      tableName = Topics.CONFIRMATION;
      st.close();
    }
    catch (SQLException e)
    {
      System.err.println("Failed to create table: " + Topics.CONFIRMATION + "; " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void addItem(String hash, long unixtime)
  {
    final String sql = " INSERT INTO " + Topics.CONFIRMATION + "(unixtime, hash) "
            + " values (?, ?)";

    try (PreparedStatement st = con.prepareStatement(sql))
    {
      st.setLong(1, unixtime);
      st.setString(2, hash);
      st.executeUpdate();
      System.out.println("Record created.");
      st.close();
    }
    catch (SQLException e)
    {
      System.err.println("Failed to insert record: " + hash + "; " + e.getMessage());
      throw new RuntimeException(e);
    }

  }

  @Override
  public void addItem(String item)
  {
    try (JsonParser jp = jsonFactory.createParser(item))
    {
      JsonNode j = jMapper.readTree(jp);
      if (j.get("topic") != null)
      {
        long blockTime = j.get("time").asLong();
        String hash = j.get("message").get("hash").asText();
        addItem(hash, blockTime);
      }
      else
      {
        System.out.println("Ignoring item: " + item);
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException("Unknown item: " + item, e);
    }
  }

  public String getName()
  {
    return tableName;
  }

  @Override
  public void removeItem(String key)
  {
    throw new NotImplementedException("Not required yet, so not implemented");
  }

  @Override
  public Collection<Object> queryItems(String query, Map<String, Object> bindVars)
  {
    final Collection<Object> results = new ArrayList<Object>();
    try (PreparedStatement st = con.prepareStatement(query))
    {
      int index = 1;
      for (Map.Entry<String, Object> kvp : bindVars.entrySet())
      {
        if (kvp.getValue() instanceof Long)
        {
          st.setLong(index++, (Long) kvp.getValue());
        }
        else if (kvp.getValue() instanceof String)
        {
          st.setString(index++, kvp.getValue().toString());
        }
      }
      final ResultSet rs = st.executeQuery();
      while (rs.next())
      {
        results.add(rs.getString("hash"));
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException("Exception when retrieving results for query: " + query);
    }
    return results;
  }

  public Collection<Object> getConfirmedBlocksForWindow(long secondsBeforeNow)
  {
    String sql = "select hash from " + Topics.CONFIRMATION +
            " where unixtime >= ?";
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("unixtime", Instant.now().minusSeconds(secondsBeforeNow).toEpochMilli());
    return queryItems(sql, m);
  }

}
