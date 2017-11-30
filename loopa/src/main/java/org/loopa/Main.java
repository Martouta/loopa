import java.lang.reflect.InvocationTargetException;

public class Main {
    private final static String DEFAULT_KAFKA_URL = "147.83.192.53";
    private final static String DEFAULT_KAFKA_TOPIC = "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3";
    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String kafkaUrl = DEFAULT_KAFKA_URL;
        String kafkaTopic = DEFAULT_KAFKA_TOPIC;
        if (args.length > 0) {
          kafkaUrl = args[0];
          if (args.length > 1) { kafkaTopic = args[1]; }
        }


        // Receiver
        HashMap hmpReceiver = new HashMap<String, String>();
        hmpReceiver.put("001", "logicSelectorTwitter");
        IPolicy rP = new Policy("receiverTwitter", hmpReceiver);
        IPolicyManager rPM = new PolicyManager(rP);
        IMessageProcessor rMP = new MessageProcessor();
        rP.addListerner(rMP);
        IReceiver r = new Receiver("receiverTwitter", rPM, rMP);


        // LogicSelector
        HashMap hmpLogicSelector = new HashMap<String, String>();
        hmpLogicSelector.put("001", "functionalLogicTwitter");
        IPolicy lsP = new Policy("logicSelectorTwitter", hmpLogicSelector);
        IPolicyManager lsPM = new PolicyManager(lsP);
        ILogicMessageDispatcher lsMD = new LogicMessageDispatcher();
        lsP.addListerner(lsMD);
        ILogicSelector ls = new LogicSelector("logicSelectorTwitter", lsPM, lsMD);


        // FunctionalLogic
        HashMap hmpFunctionalLogic = new HashMap<String, String>();
        hmpFunctionalLogic.put("001", "messageComposerTwitter");
        hmpFunctionalLogic.put("monFreq", "30");
        IPolicy flP = new Policy("functionalLogicPolicy", hmpFunctionalLogic);
        IPolicyManager flPM = new PolicyManager(flP);
        IMonitorManager mm = new IMonitorManager() {
        	@Override
        	public void setConfiguration(Map<String, String> config) {
        		System.out.println(config);
        	}

        	@Override
        	public void setComponent(ILoopAElementComponent c) {
        		// TODO Auto-generated method stub
        	}

        	@Override
        	public void processLogicData(Map<String, String> monData) {
        		System.out.println(monData);
        	}

        	@Override
        	public ILoopAElementComponent getComponent() {
        		// TODO Auto-generated method stub
        		return null;
        	}
        };
        IFunctionalLogicEnactor flE = new MonitorFunctionalLogicEnactor(mm);
        flP.addListerner(flE);
        IFunctionalLogic fl = new FunctionalLogic("functionalLogicTwitter", flPM, flE);

        // MessageComposer
        HashMap hmpMessageComposer = new HashMap<String, String>();
        hmpMessageComposer.put("001", "senderTwitter");
        IPolicy mcP = new Policy("messageComposerTwitter", hmpMessageComposer);
    		IPolicyManager mcPM = new PolicyManager(mcP);
    		IDataFormatter mcDF = new DataFormatter();
    		IMessageCreator mcMC = new MessageCreator();
    		mcP.addListerner(mcDF);
    		mcP.addListerner(mcMC);
        IMessageComposer mc = new MessageComposer("messageComposerTwitter", mcPM, mcDF, mcMC);

        // Sender
        HashMap hmpSender = new HashMap<String, String>();
        hmpSender.put("001", "kafkaServiceTwitter");
        IPolicy sP = new Policy("senderTwitter", hmpSender);
    		IPolicyManager sPM = new PolicyManager(sP);
    		IMessageSender sMS = new MessageSender() {
          @Override
          protected void sendMessage(IMessage m) {
        		KafkaService ks = this.getComponent.getComponentRecipients().get(m.getMessageTo());
            ks.processRequest(m);
        	}
        }
    		sP.addListerner(sMS);
        ISender s = new Sender("senderTwitter", sPM, sMS);

        IMonitor m = new Monitor("MonitorPersistenceOperation", r, ls, fl, null, mc, s, null);

        // // Run kafkaService reader and send messages to the receiver
        // KafkaService kafkaService = new KafkaService(kafkaUrl, kafkaTopic);
        // kafkaService.readMessages(DataItemTwitter.class, r);
    }
}
