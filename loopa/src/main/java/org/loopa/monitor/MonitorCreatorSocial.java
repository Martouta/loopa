package org.loopa.monitor;

import java.util.HashMap;
import java.util.Map;

import org.loopa.element.adaptationlogic.AdaptationLogic;
import org.loopa.element.adaptationlogic.IAdaptationLogic;
import org.loopa.element.adaptationlogic.enactor.AdaptationLogicEnactor;
import org.loopa.element.adaptationlogic.enactor.IAdaptationLogicEnactor;
import org.loopa.element.functionallogic.FunctionalLogic;
import org.loopa.element.functionallogic.IFunctionalLogic;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.monitor.IMonitorManager;
import org.loopa.element.functionallogic.enactor.monitor.MonitorManagerSocial;
import org.loopa.element.functionallogic.enactor.monitor.MonitorFunctionalLogicEnactor;
import org.loopa.element.knowledgemanager.IKnowledgeManager;
import org.loopa.element.knowledgemanager.KnowledgeManager;
import org.loopa.element.knowledgemanager.adaptiveknowledgemanager.AdaptiveKnowledgeManager;
import org.loopa.element.knowledgemanager.adaptiveknowledgemanager.IAdaptiveKnowledgeManager;
import org.loopa.element.logicselector.ILogicSelector;
import org.loopa.element.logicselector.LogicSelector;
import org.loopa.element.logicselector.messagedispatcher.ILogicMessageDispatcher;
import org.loopa.element.logicselector.messagedispatcher.LogicMessageDispatcher;
import org.loopa.element.messagecomposer.IMessageComposer;
import org.loopa.element.messagecomposer.MessageComposer;
import org.loopa.element.messagecomposer.dataformatter.DataFormatter;
import org.loopa.element.messagecomposer.dataformatter.IDataFormatter;
import org.loopa.element.messagecomposer.messagecreator.IMessageCreator;
import org.loopa.element.messagecomposer.messagecreator.MessageCreator;
import org.loopa.element.receiver.IReceiver;
import org.loopa.element.receiver.Receiver;
import org.loopa.element.receiver.messageprocessor.IMessageProcessor;
import org.loopa.element.receiver.messageprocessor.MessageProcessor;
import org.loopa.element.sender.ISender;
import org.loopa.element.sender.Sender;
import org.loopa.element.sender.messagesender.IMessageSender;
import org.loopa.element.sender.messagesender.MessageSenderSocial;
import org.loopa.generic.documents.IPolicy;
import org.loopa.generic.documents.Policy;
import org.loopa.generic.documents.managers.IPolicyManager;
import org.loopa.generic.documents.managers.PolicyManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.monitor.IMonitor;
import org.loopa.monitor.Monitor;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.externalservice.KafkaService;

public class MonitorCreatorSocial {
  public static IMonitor create(String monitorID, KafkaService kafkaService, int monFreq) {
    String ksID = kafkaService.getID();
    IMonitor monitor = new Monitor(monitorID, createReceiver(monitorID), createLogicSelector(monitorID), createFunctionalLogic(monitorID, monFreq),
                      createAdaptationLogic(monitorID), createMessageComposer(monitorID, ksID), createSender(monitorID, ksID), createKnowledgeManager(monitorID));
    monitor.addRecipient(ksID, kafkaService);
    return monitor;
	}

  public static void startMonitoring(IMonitor monitor){
    HashMap<String, String> hmBodyMessage = new HashMap();
    hmBodyMessage.put("type", "getMonData");
    IMessage m = new Message("Main", monitor.getReceiver().getComponentId(), 1, "request", hmBodyMessage);
    monitor.getReceiver().doOperation(m);
  }

  private static IReceiver createReceiver(String monitorID){
    HashMap hmpReceiver = new HashMap<String, String>();
    hmpReceiver.put("1", "logicSelector" + monitorID);
    IPolicy rP = new Policy("receiverPolicy" + monitorID, hmpReceiver);
    IPolicyManager rPM = new PolicyManager(rP);
    IMessageProcessor rMP = new MessageProcessor();
    rP.addListerner(rMP);
    return new Receiver("receiver" + monitorID, rPM, rMP);
  }

  private static ILogicSelector createLogicSelector(String monitorID){
    HashMap hmpLogicSelector = new HashMap<String, String>();
    hmpLogicSelector.put("1", "functionalLogic" + monitorID);
    IPolicy lsP = new Policy("logicSelectorPolicy" + monitorID, hmpLogicSelector);
    IPolicyManager lsPM = new PolicyManager(lsP);
    ILogicMessageDispatcher lsMD = new LogicMessageDispatcher();
    lsP.addListerner(lsMD);
    return new LogicSelector("logicSelector" + monitorID, lsPM, lsMD);
  }

  private static IFunctionalLogic createFunctionalLogic(String monitorID, int monFreq) {
    HashMap hmpFunctionalLogic = new HashMap<String, String>();
    hmpFunctionalLogic.put("1", "messageComposer" + monitorID);
    hmpFunctionalLogic.put("monFreq", Integer.toString(monFreq));
    IPolicy flP = new Policy("functionalLogicPolicy" + monitorID, hmpFunctionalLogic);
    IPolicyManager flPM = new PolicyManager(flP);
    IMonitorManager mm = new MonitorManagerSocial();
    IFunctionalLogicEnactor flE = new MonitorFunctionalLogicEnactor(mm);
    flP.addListerner(flE);
    return new FunctionalLogic("functionalLogic" + monitorID, flPM, flE);
  }

  private static IMessageComposer createMessageComposer(String monitorID, String kafkaServiceID) {
    HashMap hmpMessageComposer = new HashMap<String, String>();
    hmpMessageComposer.put("1", "sender" + monitorID);
    hmpMessageComposer.put("getMonData", kafkaServiceID);
    hmpMessageComposer.put("receivedMonData", kafkaServiceID);
    IPolicy mcP = new Policy("messageComposerPolicy" + monitorID, hmpMessageComposer);
    IPolicyManager mcPM = new PolicyManager(mcP);
    IDataFormatter mcDF = new DataFormatter();
    IMessageCreator mcMC = new MessageCreator();
    mcP.addListerner(mcDF);
    mcP.addListerner(mcMC);
    return new MessageComposer("messageComposer" + monitorID, mcPM, mcDF, mcMC);
  }

  private static ISender createSender(String monitorID, String kafkaServiceID) {
    HashMap hmpSender = new HashMap<String, String>();
    hmpSender.put("1", kafkaServiceID);
    IPolicy sP = new Policy("senderPolicy" + monitorID, hmpSender);
    IPolicyManager sPM = new PolicyManager(sP);
    IMessageSender sMS = new MessageSenderSocial();
    sP.addListerner(sMS);
    return new Sender("sender" + monitorID, sPM, sMS);
  }

  private static IAdaptationLogic createAdaptationLogic(String monitorID) { // (empty)
    IPolicy alP = new Policy("adaptationLogicPolicy" + monitorID, new HashMap<String, String>());
    IPolicyManager alPM = new PolicyManager(alP);
    IAdaptationLogicEnactor alE = new AdaptationLogicEnactor();
    alP.addListerner(alE);
    return new AdaptationLogic("adaptationLogic" + monitorID, alPM, alE);
  }

  private static IKnowledgeManager createKnowledgeManager(String monitorID) { // (empty)
    IPolicy kP = new Policy("knowledgeManagerPolicy" + monitorID, new HashMap<String, String>());
    IPolicyManager kPM = new PolicyManager(kP);
    IAdaptiveKnowledgeManager kAKM = new AdaptiveKnowledgeManager();
    kP.addListerner(kAKM);
    return new KnowledgeManager("knowledgeManager" + monitorID, kPM, kAKM);
  }
}
