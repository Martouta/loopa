import org.loopa.comm.obtaineddata.DataItemTwitter;
import org.loopa.kafka.KafkaService;
import org.loopa.monitor.MonitorCreatorTwitter;
import org.loopa.monitor.IMonitor;
import org.loopa.analyzer.AnalyzerCreatorTwitter;
import org.loopa.analyzer.IAnalyzer;

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

        // TODO: don't forget about the 'lambda' ?

        int monFreq = 30;
        String monitorID = "MonitorTwitter", analyzerID = "AnalizerMonitor", kafkaServiceID = "kafkaService"+monitorID;

        IMonitor monitor = MonitorCreatorTwitter.create(monitorID, monFreq);
        IAnalyzer analyzer = AnalyzerCreatorTwitter.create(analyzerID, monFreq);
        KafkaService kafkaService = new KafkaService(kafkaServiceID, monitor, analyzer, kafkaUrl, kafkaTopicRead, kafkaTopicWrite, DataItemTwitter.class);
        monitor.addRecipient(kafkaServiceID, kafkaService);

        TwitterMonitorSimulator.simulate(kafkaUrl, kafkaTopicRead);

        MonitorCreatorTwitter.startMonitoring(monitor);
    }
}
