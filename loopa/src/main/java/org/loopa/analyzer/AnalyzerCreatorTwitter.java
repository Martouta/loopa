package org.loopa.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.loopa.element.adaptationlogic.AdaptationLogic;
import org.loopa.element.adaptationlogic.IAdaptationLogic;
import org.loopa.element.adaptationlogic.enactor.AdaptationLogicEnactor;
import org.loopa.element.adaptationlogic.enactor.IAdaptationLogicEnactor;
import org.loopa.element.functionallogic.FunctionalLogic;
import org.loopa.element.functionallogic.IFunctionalLogic;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.analyzer.IAnalyzerManager;
import org.loopa.element.functionallogic.enactor.analyzer.AnalyzerManagerTwitter;
import org.loopa.element.functionallogic.enactor.analyzer.AnalyzerFunctionalLogicEnactor;
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
import org.loopa.element.sender.messagesender.MessageSender;
import org.loopa.generic.documents.IPolicy;
import org.loopa.generic.documents.Policy;
import org.loopa.generic.documents.managers.IPolicyManager;
import org.loopa.generic.documents.managers.PolicyManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.analyzer.IAnalyzer;
import org.loopa.analyzer.Analyzer;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.externalservice.ExternalService; // TODO remove in the future when refactor is done
import org.loopa.externalservice.MonitoredService;

public class AnalyzerCreatorTwitter {
  public static IAnalyzer create(String analyzerID, MonitoredService monitoredService, int maxFreq, int maxFreqChangeRate, int iterations) {
    String msID = monitoredService.getID();
    IAnalyzer analyzer = new Analyzer(analyzerID, createReceiver(analyzerID), createLogicSelector(analyzerID), createFunctionalLogic(analyzerID, maxFreq, maxFreqChangeRate, iterations),
                      createAdaptationLogic(analyzerID), createMessageComposer(analyzerID), createSender(analyzerID, msID), createKnowledgeManager(analyzerID));
    analyzer.addRecipient(msID, monitoredService);
    return analyzer;
	}

  private static IReceiver createReceiver(String analyzerID){
    HashMap hmpReceiver = new HashMap<String, String>();
    hmpReceiver.put("1", "logicSelector" + analyzerID);
    IPolicy rP = new Policy("receiverPolicy" + analyzerID, hmpReceiver);
    IPolicyManager rPM = new PolicyManager(rP);
    IMessageProcessor rMP = new MessageProcessor();
    rP.addListerner(rMP);
    return new Receiver("receiver" + analyzerID, rPM, rMP);
  }

  private static ILogicSelector createLogicSelector(String analyzerID){
    HashMap hmpLogicSelector = new HashMap<String, String>();
    hmpLogicSelector.put("1", "functionalLogic" + analyzerID);
    IPolicy lsP = new Policy("logicSelectorPolicy" + analyzerID, hmpLogicSelector);
    IPolicyManager lsPM = new PolicyManager(lsP);
    ILogicMessageDispatcher lsMD = new LogicMessageDispatcher();
    lsP.addListerner(lsMD);
    return new LogicSelector("logicSelector" + analyzerID, lsPM, lsMD);
  }

  private static IFunctionalLogic createFunctionalLogic(String analyzerID, int maxFreq, int maxFreqChangeRate, int iterations) {
    HashMap hmpFunctionalLogic = new HashMap<String, String>();
    hmpFunctionalLogic.put("1", "messageComposer" + analyzerID);
    hmpFunctionalLogic.put("maxFreq", Integer.toString(maxFreq));
    hmpFunctionalLogic.put("maxFreqChangeRate", Integer.toString(maxFreqChangeRate));
    hmpFunctionalLogic.put("iterations", Integer.toString(iterations));
    IPolicy flP = new Policy("functionalLogicPolicy" + analyzerID, hmpFunctionalLogic);
    IPolicyManager flPM = new PolicyManager(flP);
    IAnalyzerManager am = new AnalyzerManagerTwitter();
    IFunctionalLogicEnactor flE = new AnalyzerFunctionalLogicEnactor(am);
    flP.addListerner(flE);
    return new FunctionalLogic("functionalLogic" + analyzerID, flPM, flE);
  }

  private static IMessageComposer createMessageComposer(String analyzerID) {
    HashMap hmpMessageComposer = new HashMap<String, String>();
    hmpMessageComposer.put("1", "sender" + analyzerID);
    hmpMessageComposer.put("failedMonData", "sender" + analyzerID);
    IPolicy mcP = new Policy("messageComposerPolicy" + analyzerID, hmpMessageComposer);
    IPolicyManager mcPM = new PolicyManager(mcP);
    IDataFormatter mcDF = new DataFormatter();
    IMessageCreator mcMC = new MessageCreator();
    mcP.addListerner(mcDF);
    mcP.addListerner(mcMC);
    return new MessageComposer("messageComposer" + analyzerID, mcPM, mcDF, mcMC);
  }

  private static ISender createSender(String analyzerID, String monitoredServiceID) {
    HashMap hmpSender = new HashMap<String, String>();
    hmpSender.put("1", monitoredServiceID);
    IPolicy sP = new Policy("senderPolicy" + analyzerID, hmpSender);
    IPolicyManager sPM = new PolicyManager(sP);
    IMessageSender sMS = new MessageSender() {
      @Override
      protected void sendMessage(IMessage message) {
        // TODO refactor
        ExternalService externalService = (ExternalService) this.getComponent().getComponentRecipients().get(message.getMessageTo());
        externalService.processRequest(message);
      }
    };
    sP.addListerner(sMS);
    return new Sender("sender" + analyzerID, sPM, sMS);
  }

  private static IAdaptationLogic createAdaptationLogic(String analyzerID) { // (empty)
    IPolicy alP = new Policy("adaptationLogicPolicy" + analyzerID, new HashMap<String, String>());
    IPolicyManager alPM = new PolicyManager(alP);
    IAdaptationLogicEnactor alE = new AdaptationLogicEnactor();
    alP.addListerner(alE);
    return new AdaptationLogic("adaptationLogic" + analyzerID, alPM, alE);
  }

  private static IKnowledgeManager createKnowledgeManager(String analyzerID) { // (empty)
    IPolicy kP = new Policy("knowledgeManagerPolicy" + analyzerID, new HashMap<String, String>());
    IPolicyManager kPM = new PolicyManager(kP);
    IAdaptiveKnowledgeManager kAKM = new AdaptiveKnowledgeManager();
    kP.addListerner(kAKM);
    return new KnowledgeManager("knowledgeManager" + analyzerID, kPM, kAKM);
  }
}
