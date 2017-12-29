import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.loopa.element.adaptationlogic.AdaptationLogic;
import org.loopa.element.adaptationlogic.IAdaptationLogic;
import org.loopa.element.adaptationlogic.enactor.AdaptationLogicEnactor;
import org.loopa.element.adaptationlogic.enactor.IAdaptationLogicEnactor;
import org.loopa.element.functionallogic.FunctionalLogic;
import org.loopa.element.functionallogic.IFunctionalLogic;
import org.loopa.element.functionallogic.enactor.IFunctionalLogicEnactor;
import org.loopa.element.functionallogic.enactor.monitor.IMonitorManager;
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
import org.loopa.element.sender.messagesender.MessageSender;
import org.loopa.generic.documents.IPolicy;
import org.loopa.generic.documents.Policy;
import org.loopa.generic.documents.managers.IPolicyManager;
import org.loopa.generic.documents.managers.PolicyManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.monitor.IMonitor;
import org.loopa.monitor.Monitor;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.comm.obtaineddata.ObtainedData;
import org.loopa.comm.obtaineddata.DataItemTwitter;
import org.loopa.kafka.KafkaService;

public class Main {
    private final static String DEFAULT_KAFKA_URL = "147.83.192.53";
    private final static String DEFAULT_KAFKA_TOPIC_READ = "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3";
    private final static String DEFAULT_KAFKA_TOPIC_WRITE = "defaultWrite";

    public static void main(String[] args) {
        String kafkaUrl = DEFAULT_KAFKA_URL;
        String kafkaTopicRead = DEFAULT_KAFKA_TOPIC_READ;
        String kafkaTopicWrite = DEFAULT_KAFKA_TOPIC_WRITE;
        if (args.length > 0) {
          kafkaUrl = args[0];
          if (args.length > 1) {
            kafkaTopicRead = args[1];
            if (args.length > 2) { kafkaTopicWrite = args[2]; }
          }
        }


        // Receiver
        HashMap hmpReceiver = new HashMap<String, String>();
        hmpReceiver.put("1", "logicSelectorTwitter");
        IPolicy rP = new Policy("receiverTwitter", hmpReceiver);
        IPolicyManager rPM = new PolicyManager(rP);
        IMessageProcessor rMP = new MessageProcessor();
        rP.addListerner(rMP);
        IReceiver r = new Receiver("receiverTwitter", rPM, rMP);


        // LogicSelector
        HashMap hmpLogicSelector = new HashMap<String, String>();
        hmpLogicSelector.put("1", "functionalLogicTwitter");
        IPolicy lsP = new Policy("logicSelectorTwitter", hmpLogicSelector);
        IPolicyManager lsPM = new PolicyManager(lsP);
        ILogicMessageDispatcher lsMD = new LogicMessageDispatcher();
        lsP.addListerner(lsMD);
        ILogicSelector ls = new LogicSelector("logicSelectorTwitter", lsPM, lsMD);


        // FunctionalLogic
        HashMap hmpFunctionalLogic = new HashMap<String, String>();
        hmpFunctionalLogic.put("1", "messageComposerTwitter");
        hmpFunctionalLogic.put("monFreq", "30");
        IPolicy flP = new Policy("functionalLogicTwitter", hmpFunctionalLogic);
        IPolicyManager flPM = new PolicyManager(flP);
        IMonitorManager mm = new IMonitorManager() {
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

        	@Override
        	public void processLogicData(Map<String, String> monData) {
              int monFreq = Integer.parseInt( this.config.get("monFreq") );
              String type = monData.get("type"), messageTo = config.get("1");
              ILoopAElementComponent r = (ILoopAElementComponent) this.getComponent().getComponentRecipients().get(messageTo);
              Map<String, String> body = new HashMap<String, String>();

              System.out.println("processLogicData: " + monData);

              if (type == "getMonData") {
                body.put("type", type);
                IMessage mRequestMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "request", body);


                while (true) {
                  // TODO: supongo que acabare haciendolo con threads
                  r.doOperation(mRequestMonData);
                  try {
                    TimeUnit.MILLISECONDS.sleep(monFreq);
                  } catch (InterruptedException e) {
                    System.err.println("InterruptedException: " + e.getMessage());
                  }
                }
              } else if (type == "setMonData") {
                body.putAll(monData);
                IMessage mResponseMonData = new Message(this.getComponent().getComponentId(), messageTo, 1, "response", body);
                r.doOperation(mResponseMonData);
              }
        	}
        };
        IFunctionalLogicEnactor flE = new MonitorFunctionalLogicEnactor(mm);
        flP.addListerner(flE);
        IFunctionalLogic fl = new FunctionalLogic("functionalLogicTwitter", flPM, flE);

        // MessageComposer
        HashMap hmpMessageComposer = new HashMap<String, String>();
        hmpMessageComposer.put("1", "senderTwitter");
        hmpMessageComposer.put("getMonData", "kafkaServiceTwitter");
        hmpMessageComposer.put("setMonData", "kafkaServiceTwitter");
        IPolicy mcP = new Policy("messageComposerTwitter", hmpMessageComposer);
    		IPolicyManager mcPM = new PolicyManager(mcP);
    		IDataFormatter mcDF = new DataFormatter();
    		IMessageCreator mcMC = new MessageCreator();
    		mcP.addListerner(mcDF);
    		mcP.addListerner(mcMC);
        IMessageComposer mc = new MessageComposer("messageComposerTwitter", mcPM, mcDF, mcMC);

        // Sender
        HashMap hmpSender = new HashMap<String, String>();
        hmpSender.put("1", "kafkaServiceTwitter");
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
        ISender s = new Sender("senderTwitter", sPM, sMS);

        // AdaptationLogic (empty)
        IPolicy alP = new Policy("adaptationLogicTwitter", new HashMap<String, String>());
        IPolicyManager alPM = new PolicyManager(alP);
        IAdaptationLogicEnactor alE = new AdaptationLogicEnactor();
        alP.addListerner(alE);
        IAdaptationLogic al = new AdaptationLogic("adaptationLogicTwitter", alPM, alE);

        // KnowledgeManager (empty)
        IPolicy kP = new Policy("knowledgeManagerTwitter", new HashMap<String, String>());
        IPolicyManager kPM = new PolicyManager(kP);
        IAdaptiveKnowledgeManager kAKM = new AdaptiveKnowledgeManager();
        kP.addListerner(kAKM);
        IKnowledgeManager k = new KnowledgeManager("knowledgeManagerTwitter", kPM, kAKM);

        IMonitor monitor = new Monitor("MonitorPersistenceOperation", r, ls, fl, al, mc, s, k);
        KafkaService kafkaService = new KafkaService("kafkaServiceTwitter", monitor, kafkaUrl, kafkaTopicRead, kafkaTopicWrite, DataItemTwitter.class);
        monitor.addRecipient("kafkaServiceTwitter", kafkaService);

        TwitterMonitorSimulator.simulate(kafkaUrl, kafkaTopicRead);

        HashMap<String, String> hmBodyMessage = new HashMap();
        hmBodyMessage.put("type", "getMonData");
        IMessage m = new Message("Main", monitor.getReceiver().getComponentId(), 1, "request", hmBodyMessage);
        monitor.getReceiver().doOperation(m);
    }
}
