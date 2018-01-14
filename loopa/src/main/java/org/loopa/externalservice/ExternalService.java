package org.loopa.externalservice;

import org.loopa.comm.message.IMessage;

public abstract class ExternalService {
  String id;
  public ExternalService(String id) { this.id = id; }
  public void processRequest(IMessage message) { System.out.println("ExternalService#processRequest must be overrided in subclasses"); };
  public String getID() { return this.id; }
}
