import org.loopa.externalservice.KafkaService;
import org.loopa.externalservice.MonitoredService;
import org.loopa.monitor.MonitorCreatorSocial;
import org.loopa.monitor.IMonitor;
import org.loopa.analyzer.AnalyzerCreatorSocial;
import org.loopa.analyzer.IAnalyzer;

public class Main {
    public static void main(String[] args) {
        String kafkaUrl = "147.83.192.53", kafkaTopic = "68d24960-5eff-4c14-8a8c-6d0c7f8ea5c3", keywordExpression = args[1],
                monitorID = "MonitorSocial", analyzerID = "AnalizerMonitor";
        int timeSlot = Integer.parseInt(args[2]), newTimeSlot = Integer.parseInt(args[2]), monFreq = Integer.parseInt(args[3]),
            maxFreq = Integer.parseInt(args[4]), maxFreqChangeRate = Integer.parseInt(args[5]), iterations = Integer.parseInt(args[6]),
            idConf = Integer.parseInt(args[0]);
        if (args.length == 8) { newTimeSlot = Integer.parseInt(args[7]); }

        MonitoredService monitoredService = new MonitoredService("MonitoredServiceID", idConf, "TwitterAPI", timeSlot, kafkaUrl, kafkaTopic, keywordExpression);
        KafkaService kafkaService = new KafkaService("kafkaServiceID", monitoredService.getKafkaEndpoint(), monitoredService.getKafkaTopic(), "kafkaTopicWrite");
        IMonitor monitor = MonitorCreatorSocial.create(monitorID, kafkaService, monFreq);
        kafkaService.setMonitor(monitor);
        IAnalyzer analyzer = AnalyzerCreatorSocial.create(analyzerID, monitoredService, maxFreq, maxFreqChangeRate, iterations, newTimeSlot);
        kafkaService.setAnalyzer(analyzer);

        MonitorCreatorSocial.startMonitoring(monitor);
    }
}
