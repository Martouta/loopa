import org.loopa.comm.obtaineddata.DataItemTwitter;
import org.loopa.kafka.KafkaService;
import org.loopa.monitor.MonitorTwitterCreator;
import org.loopa.monitor.IMonitor;

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

        int monFreq = 30;

        IMonitor monitor = MonitorTwitterCreator.create("MonitorPersistenceOperation", monFreq);
        KafkaService kafkaService = new KafkaService("kafkaServiceTwitter", monitor, kafkaUrl, kafkaTopicRead, kafkaTopicWrite, DataItemTwitter.class);
        monitor.addRecipient("kafkaServiceTwitter", kafkaService);

        TwitterMonitorSimulator.simulate(kafkaUrl, kafkaTopicRead);
        
        MonitorTwitterCreator.startMonitoring(monitor);
    }
}
