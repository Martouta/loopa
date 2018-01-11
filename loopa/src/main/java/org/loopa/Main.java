import org.loopa.comm.obtaineddata.DataItemTwitter;
import org.loopa.externalservice.KafkaService;
import org.loopa.externalservice.MonitoredService;
import org.loopa.monitor.MonitorCreatorTwitter;
import org.loopa.monitor.IMonitor;
import org.loopa.analyzer.AnalyzerCreatorTwitter;
import org.loopa.analyzer.IAnalyzer;

public class Main {
    public static void main(String[] args) {
        String kafkaUrl = "147.83.192.53", kafkaTopic = "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3", keywordExpression = "Coutinho",
                monitorID = "MonitorTwitter", analyzerID = "AnalizerMonitor";
        int timeSlot = 40, newTimeSlot = 40, monFreq = 1, maxFreq = 45, maxFreqChangeRate = 100, iterations = 50,
            idConf = 21;

        // TODO: error management
        // TODO: mas flexible?

        // TODO: ojo con los "Social..."
        // TODO OJO con el nombre "Twitter" por todas partes!!!!
        // TODO remove DataItem && DataItemTwitter

        MonitoredService monitoredService = new MonitoredService("MonitoredServiceID", idConf, "twitterAPI", timeSlot, kafkaUrl, kafkaTopic, keywordExpression);
        KafkaService kafkaService = new KafkaService("kafkaServiceID", monitoredService.getKafkaEndpoint(), monitoredService.getKafkaTopic(), "kafkaTopicWrite", DataItemTwitter.class);
        IMonitor monitor = MonitorCreatorTwitter.create(monitorID, kafkaService, monFreq);
        kafkaService.setMonitor(monitor);
        IAnalyzer analyzer = AnalyzerCreatorTwitter.create(analyzerID, monitoredService, maxFreq, maxFreqChangeRate, iterations, newTimeSlot);
        kafkaService.setAnalyzer(analyzer);

        MonitorCreatorTwitter.startMonitoring(monitor);
    }
}
