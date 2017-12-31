package org.loopa.element.functionallogic.enactor.monitor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.Instant;

import org.loopa.element.functionallogic.enactor.monitor.IMonitorManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;

public class MonitorManagerTwitter implements IMonitorManager {
  private Map<String, String> config = new HashMap<String, String>();
  private ILoopAElementComponent component;

  @Override
  public void setConfiguration(Map<String, String> config) {
    this.config = config;
  }

  @Override
  public void setComponent(ILoopAElementComponent c) {
    this.component = c;
  }

  @Override
  public ILoopAElementComponent getComponent() {
    return this.component;
  }

  private void doGetMonData(String messageTo) {
    Map<String, String> body = new HashMap<String, String>();
    body.put("type", "getMonData");
    ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
    IMessage mRequestMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "request", body);
    int monFreq = Integer.parseInt( this.config.get("monFreq") );

    Instant startTime = Instant.now();
    while (true) {
      r.doOperation(mRequestMonData);
      Instant endTime = Instant.now();
      try {
        Long waitTime = Long.valueOf(monFreq) - Duration.between(startTime, endTime).toMillis();
        if (waitTime > 0) {
          TimeUnit.MILLISECONDS.sleep(waitTime);
        }
        startTime = Instant.now();
      } catch (InterruptedException e) {
        System.err.println("InterruptedException: " + e.getMessage());
      }
    }
  }

  private void doSetMonData(String messageTo, Map<String, String> monData) {
    Map<String, String> body = new HashMap<String, String>();
    body.put("idOutput", monData.get("idOutput"));
    body.put("searchTimeStamp", monData.get("searchTimeStamp"));
    body.put("type", "receivedMonData");
    ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
    IMessage mResponseMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "response", body);
    r.doOperation(mResponseMonData);
  }

  @Override
  public void processLogicData(Map<String, String> monData) {
      String type = monData.get("type"), messageTo = config.get("1");

      switch (type) {
      case "setMonData":
        doSetMonData(messageTo, monData);
        break;
      case "getMonData":
        doGetMonData(messageTo);
        // No need to break because it contains an infinite loop: while true
      default:
        System.err.println("Invalid type code " + type + " in processLogicData");
      }
  }
}
