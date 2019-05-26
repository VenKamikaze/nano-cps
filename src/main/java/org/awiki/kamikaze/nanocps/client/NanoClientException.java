package org.awiki.kamikaze.nanocps.client;

public class NanoClientException extends RuntimeException
{
  private static final long serialVersionUID = -2644165158675703778L;

  public NanoClientException(String message, Throwable ex) {
    super(message, ex);
  }
}
