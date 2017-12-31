package org.loopa.element.functionallogic.enactor.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.loopa.element.functionallogic.enactor.analyzer.IAnalyzerManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;

public class AnalyzerManagerTwitter implements IAnalyzerManager {
  private Map<String, String> config = new HashMap<String, String>();
  private ILoopAElementComponent component;
  private Instant lastTime;

  @Override
  public void setConfiguration(Map<String, String> config) {
    System.out.println("AnalyzerManagerTwitter#setConfiguration " + config);
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

  private void doReceivedMonData(String messageTo, Map<String, String> monData) {
    // Map<String, String> body = new HashMap<String, String>();
    // body.put("idOutput", monData.get("idOutput"));
    // body.put("searchTimeStamp", monData.get("searchTimeStamp"));
    // body.put("type", "receivedMonData");
    // ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
    // IMessage mResponseMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "response", body);
    // r.doOperation(mResponseMonData);

    // TODO: determinar si el twitterMonitor esta yendo bien o no
    //    y si no mandar una reconfiguraciona traves de kafka utilizando la topic y formato que exite para ello
    int monFreq = Integer.parseInt( this.config.get("monFreq") );
    Instant currentTime = Timestamp.valueOf( monData.get("searchTimeStamp") ).toInstant();
    if (lastTime != null) {
      Long timeElapsed = Duration.between(lastTime, currentTime).toMillis();
      System.out.println("Los instants --> " + lastTime + ' y ' + currentTime); // TODO for testing purposes
      if (timeElapsed == monFreq) { System.out.println("TODO AnalyzerManagerTwitter#doReceivedMonData todo correcto"); }
      else { System.out.println("TODO AnalyzerManagerTwitter#doReceivedMonData no tiene el tiempo correcto. real: " + timeElapsed + " monFreq: " + monFreq); }
    }
    lastTime = currentTime;
  }

  @Override
  public void processLogicData(Map<String, String> monData) {
      String type = monData.get("type"), messageTo = config.get("1");

      switch (type) {
      case "receivedMonData":
        doReceivedMonData(messageTo, monData);
        break;
      default:
        System.err.println("Invalid type code " + type + " in processLogicData");
      }
  }
}
