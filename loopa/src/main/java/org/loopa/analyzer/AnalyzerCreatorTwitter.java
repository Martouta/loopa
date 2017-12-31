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
import org.loopa.kafka.KafkaService;

public class AnalyzerCreatorTwitter {
  public static IAnalyzer create(String analyzerID) {
    return new Analyzer(analyzerID, createReceiver(analyzerID), createLogicSelector(analyzerID), createFunctionalLogic(analyzerID),
                      createAdaptationLogic(analyzerID), createMessageComposer(analyzerID), createSender(analyzerID), createKnowledgeManager(analyzerID));
	}

  private static IReceiver createReceiver(String analyzerID){
    HashMap hmpReceiver = new HashMap<String, String>();
    hmpReceiver.put("1", "logicSelector" + analyzerID);
    IPolicy rP = new Policy("receiverTwitter", hmpReceiver);
    IPolicyManager rPM = new PolicyManager(rP);
    IMessageProcessor rMP = new MessageProcessor();
    rP.addListerner(rMP);
    return new Receiver("receiverTwitter", rPM, rMP);
  }

  private static ILogicSelector createLogicSelector(String analyzerID){
    HashMap hmpLogicSelector = new HashMap<String, String>();
    hmpLogicSelector.put("1", "functionalLogic" + analyzerID);
    IPolicy lsP = new Policy("logicSelectorTwitter", hmpLogicSelector);
    IPolicyManager lsPM = new PolicyManager(lsP);
    ILogicMessageDispatcher lsMD = new LogicMessageDispatcher();
    lsP.addListerner(lsMD);
    return new LogicSelector("logicSelectorTwitter", lsPM, lsMD);
  }

  private static IFunctionalLogic createFunctionalLogic(String analyzerID) {
    HashMap hmpFunctionalLogic = new HashMap<String, String>();
    hmpFunctionalLogic.put("1", "messageComposer" + analyzerID);
    IPolicy flP = new Policy("functionalLogicTwitter", hmpFunctionalLogic);
    IPolicyManager flPM = new PolicyManager(flP);
    IAnalyzerManager mm = new AnalyzerManagerTwitter();
    IFunctionalLogicEnactor flE = new MonitorFunctionalLogicEnactor(mm);
    flP.addListerner(flE);
    return new FunctionalLogic("functionalLogicTwitter", flPM, flE);
  }

  private static IMessageComposer createMessageComposer(String analyzerID) {
    HashMap hmpMessageComposer = new HashMap<String, String>();
    hmpMessageComposer.put("1", "sender" + analyzerID);
    hmpMessageComposer.put("getMonData", "kafkaService" + analyzerID);
    hmpMessageComposer.put("receivedMonData", "kafkaService" + analyzerID);
    IPolicy mcP = new Policy("messageComposerTwitter", hmpMessageComposer);
    IPolicyManager mcPM = new PolicyManager(mcP);
    IDataFormatter mcDF = new DataFormatter();
    IMessageCreator mcMC = new MessageCreator();
    mcP.addListerner(mcDF);
    mcP.addListerner(mcMC);
    return new MessageComposer("messageComposerTwitter", mcPM, mcDF, mcMC);
  }

  private static ISender createSender(String analyzerID) {
    HashMap hmpSender = new HashMap<String, String>();
    hmpSender.put("1", "kafkaService" + analyzerID);
    IPolicy sP = new Policy("senderTwitter", hmpSender);
    IPolicyManager sPM = new PolicyManager(sP);
    IMessageSender sMS = new MessageSender() {
      @Override
      protected void sendMessage(IMessage message) {
        KafkaService ks = (KafkaService) this.getComponent().getComponentRecipients().get(message.getMessageTo());
        ks.processRequest(message);
      }
    };
    sP.addListerner(sMS);
    return new Sender("senderTwitter", sPM, sMS);
  }

  private static IAdaptationLogic createAdaptationLogic(String analyzerID) { // (empty)
    IPolicy alP = new Policy("adaptationLogicTwitter", new HashMap<String, String>());
    IPolicyManager alPM = new PolicyManager(alP);
    IAdaptationLogicEnactor alE = new AdaptationLogicEnactor();
    alP.addListerner(alE);
    return new AdaptationLogic("adaptationLogicTwitter", alPM, alE);
  }

  private static IKnowledgeManager createKnowledgeManager(String analyzerID) { // (empty)
    IPolicy kP = new Policy("knowledgeManagerTwitter", new HashMap<String, String>());
    IPolicyManager kPM = new PolicyManager(kP);
    IAdaptiveKnowledgeManager kAKM = new AdaptiveKnowledgeManager();
    kP.addListerner(kAKM);
    return new KnowledgeManager("knowledgeManagerTwitter", kPM, kAKM);
  }
}
