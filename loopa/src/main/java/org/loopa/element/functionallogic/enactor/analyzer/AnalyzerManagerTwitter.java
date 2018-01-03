package org.loopa.element.functionallogic.enactor.analyzer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.loopa.comm.obtaineddata.ObtainedData;
import org.loopa.element.functionallogic.enactor.analyzer.IAnalyzerManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;

public class AnalyzerManagerTwitter implements IAnalyzerManager {
  private Map<String, String> config = new HashMap<String, String>();
  private ILoopAElementComponent component;
  private Instant lastTime;
  private Long lastTimeElapsed;
  private int counterWrongIterations = 0;

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

  private boolean isWorkingProperly(Instant currentTime, int maxFreq, int maxFreqChangeRate) {
    boolean workingProperly = true;
    if (lastTime != null) {
      Long timeElapsed = Duration.between(lastTime, currentTime).toMillis();
      System.out.println("lastTime: " + lastTime + " | currentTime: " + currentTime);
      System.out.println("lastTimeElapsed: " + lastTimeElapsed + " | timeElapsed: " +  timeElapsed);

      if(timeElapsed > maxFreq) {
        workingProperly = false;
      } else if(lastTimeElapsed != null){
        Long ratio = timeElapsed - lastTimeElapsed;
        lastTimeElapsed = timeElapsed;
        workingProperly = (Math.abs(ratio) <= maxFreqChangeRate);
      }
      lastTimeElapsed = timeElapsed;
    }
    lastTime = currentTime;
    return workingProperly;
  }

  private void doReceivedMonData(String messageTo, Map<String, String> monData) {
    int maxFreq           = Integer.parseInt( this.config.get("maxFreq") ),
        maxFreqChangeRate = Integer.parseInt( this.config.get("maxFreqChangeRate") ),
        iterations        = Integer.parseInt( this.config.get("iterations") );
    ArrayList<Object> arrayObjectTimestamps = ObtainedData.getValuesFromFieldnameInHashMap(monData, "searchTimeStamp");
    System.out.println("Lo que convierto: " + arrayObjectTimestamps);
    for (Object objTimestamp : arrayObjectTimestamps) {
      Instant currentTime = ((Timestamp) objTimestamp).toInstant();
      boolean workingProperly = isWorkingProperly(currentTime, maxFreq, maxFreqChangeRate);
      if (!workingProperly) { counterWrongIterations++; }
      System.out.println("isWorkingProperly: " + workingProperly + " - " + counterWrongIterations);
      System.out.println("maxFreq: " + maxFreq + " , maxFreqChangeRate: " + maxFreqChangeRate + " , iterations: " + iterations);
      if (counterWrongIterations == iterations) {
        System.out.println("Llega al IF");
        counterWrongIterations = 0;
        Map<String, String> body = new HashMap<String, String>();
        body.put("type", "failedMonData");
        ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
        IMessage mResponseMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "response", body);
        r.doOperation(mResponseMonData);
      }
    }
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
