import org.loopa.externalservice.KafkaService;
import org.loopa.externalservice.MonitoredService;
import org.loopa.monitor.MonitorCreatorSocial;
import org.loopa.monitor.IMonitor;
import org.loopa.analyzer.AnalyzerCreatorSocial;
import org.loopa.analyzer.IAnalyzer;

public class Main {
    public static void main(String[] args) {
        String kafkaUrl = "147.83.192.53", kafkaTopic = "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3", keywordExpression = "Coutinho",
                monitorID = "MonitorSocial", analyzerID = "AnalizerMonitor";
        int timeSlot = 30, newTimeSlot = 25, monFreq = 1, maxFreq = 29, maxFreqChangeRate = 2, iterations = 2,
            idConf = 11;

        MonitoredService monitoredService = new MonitoredService("MonitoredServiceID", idConf, "SocialAPI", timeSlot, kafkaUrl, kafkaTopic, keywordExpression);
        KafkaService kafkaService = new KafkaService("kafkaServiceID", monitoredService.getKafkaEndpoint(), monitoredService.getKafkaTopic(), "kafkaTopicWrite");
        IMonitor monitor = MonitorCreatorSocial.create(monitorID, kafkaService, monFreq);
        kafkaService.setMonitor(monitor);
        IAnalyzer analyzer = AnalyzerCreatorSocial.create(analyzerID, monitoredService, maxFreq, maxFreqChangeRate, iterations, newTimeSlot);
        kafkaService.setAnalyzer(analyzer);

        MonitorCreatorSocial.startMonitoring(monitor);
    }
}
