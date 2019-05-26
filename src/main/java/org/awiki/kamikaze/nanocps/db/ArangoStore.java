package org.awiki.kamikaze.nanocps.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;

@Component
public class ArangoStore implements Store
{
  private static final String DB_NAME = "NANO_DB";
  static final ArangoDB arangoDB = new ArangoDB.Builder().build();
  static ArangoDatabase db = null;

  private String collectionName = null;

  public ArangoStore()
  {
    try
    {
      arangoDB.createDatabase(DB_NAME);
      System.out.println("Database created: " + DB_NAME);
    }
    catch (ArangoDBException e)
    {
      System.err.println("Failed to create database: " + DB_NAME + "; " + e.getMessage());
    }
    finally
    {
      try
      {
        db = arangoDB.db(DB_NAME); // db may already exist. should handle this case, but cbf.
        System.out.println("Store initialised for DB: " + DB_NAME);
      }
      catch (ArangoDBException e)
      {
        throw new RuntimeException("Unable to initialise DB: " + DB_NAME, e);
      }
    }
  }

  public boolean isInitialised()
  {
    return StringUtils.isNotBlank(collectionName);
  }

  @Override
  public void createStore(String storeName)
  {
    try
    {
      CollectionEntity myArangoCollection = db.createCollection(storeName);
      System.out.println("Collection created: " + myArangoCollection.getName());
    }
    catch (ArangoDBException e)
    {
      System.err.println("Failed to create collection: " + storeName + "; " + e.getMessage());
    }
    finally
    {
      try
      {
        collectionName = db.collection(storeName).getInfo().getName();
        System.out.println("Collection initialised: " + collectionName);
      }
      catch (ArangoDBException e)
      {
        throw new RuntimeException("Unable to initialise collection: " + storeName, e);
      }
    }
  }

  @Override
  public void addItem(String item)
  {
    try
    {
      db.collection(collectionName).insertDocument(item);
      System.out.println("Document created");
    }
    catch (ArangoDBException e)
    {
      System.err.println("Failed to create document. " + e.getMessage());
    }
  }

  public String getName()
  {
    return collectionName;
  }

  @Override
  public void removeItem(String key)
  {
    try
    {
      db.collection(collectionName).deleteDocument(key);
    }
    catch (ArangoDBException e)
    {
      System.err.println("Failed to delete document. " + e.getMessage());
    }
  }

  // example query: "FOR t IN firstCollection FILTER t.name == @name RETURN t";
  // example bindvars: Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
  @Override
  public Collection<Object> queryItems(String query, Map<String, Object> bindVars)
  {
    final Collection<Object> results = new ArrayList<Object>();
    try
    {
      ArangoCursor<String> cursor = db.query(query, bindVars, null, String.class);
      cursor.forEachRemaining(aDocument -> {
        // System.out.println("item: " + aDocument);
        results.add(aDocument);
      });
    }
    catch (ArangoDBException e)
    {
      System.err.println("Failed to execute query. " + e.getMessage());
    }
    return results;
  }

}
