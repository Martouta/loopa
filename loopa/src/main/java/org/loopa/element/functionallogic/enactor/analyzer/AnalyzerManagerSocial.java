package org.loopa.element.functionallogic.enactor.analyzer;

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

public class AnalyzerManagerSocial implements IAnalyzerManager {
  private Map<String, String> config = new HashMap<String, String>();
  private ILoopAElementComponent component;
  private Instant lastTime;
  private Long lastTimeElapsed;
  private int counterWrongIterations, maxFreq, maxFreqChangeRate, iterations, timeSlot;

  @Override
  public void setConfiguration(Map<String, String> config) {
    this.config            = config;
    counterWrongIterations = 0;
    maxFreq                = Integer.parseInt( config.get("maxFreq") );
    maxFreqChangeRate      = Integer.parseInt( config.get("maxFreqChangeRate") );
    iterations             = Integer.parseInt( config.get("iterations") );

    String strTimeSlot = config.get("timeSlot");
    timeSlot = (strTimeSlot != null) ? Integer.parseInt(strTimeSlot) : -1;
  }

  @Override
  public void setComponent(ILoopAElementComponent c) {
    this.component = c;
  }

  @Override
  public ILoopAElementComponent getComponent() {
    return this.component;
  }

  private boolean isWorkingProperly(Instant currentTime) {
    boolean workingProperly = true;
    if (lastTime != null) {
      System.out.println("lastTime: " + lastTime + " & currentTime: " + currentTime); // TODO remove line
      System.out.println("maxFreq: " + maxFreq + " & maxFreqChangeRate: " + maxFreqChangeRate + " & iterations: " + iterations); // TODO remove line
      Long timeElapsed = Duration.between(lastTime, currentTime).getSeconds();

      if(timeElapsed > maxFreq) {
        workingProperly = false;
      } else if(lastTimeElapsed != null){
        Long ratio = timeElapsed - lastTimeElapsed;
        lastTimeElapsed = timeElapsed;
        workingProperly = (Math.abs(ratio) <= maxFreqChangeRate);
      }
      System.out.println("lastTimeElapsed: " + lastTimeElapsed + " & timeElapsed: " + timeElapsed); // TODO remove line
      lastTimeElapsed = timeElapsed;
    }
    lastTime = currentTime;
    return workingProperly;
  }

  private void doReceivedMonData(String messageTo, Map<String, String> monData) {
    ArrayList<Object> arrayObjectTimestamps = ObtainedData.getValuesFromFieldnameInHashMap(monData, "searchTimeStamp");

    for (Object objTimestamp : arrayObjectTimestamps) {
      Instant currentTime = ((Timestamp) objTimestamp).toInstant();
      boolean workingProperly = isWorkingProperly(currentTime);
      System.out.println("isWorkingProperly: " + workingProperly + " & counterWrongIterations: " + (counterWrongIterations + ((workingProperly) ? 0 : 1))); // TODO remove line
      if (!workingProperly) {
        counterWrongIterations++;
        if (counterWrongIterations == iterations) {
          System.out.println("llega al 'reconfigurame'"); // TODO remove line
          counterWrongIterations = 0;
          Map<String, String> body = new HashMap<String, String>();
          body.put("type", "failedMonData");
          body.put("timeSlot", Integer.toString(timeSlot));
          ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
          IMessage mResponseMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "response", body);
          r.doOperation(mResponseMonData);
        }
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
