import java.util.Properties;
import java.util.concurrent.TimeUnit;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class TwitterMonitorSimulator {
    private final static String DEFAULT_KAFKA_URL = "localhost";
    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();

    public TwitterMonitorSimulator(String kafkaUrl) {
        properties.put("metadata.broker.list", kafkaUrl+":9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        producer = new Producer<>(new ProducerConfig(properties));
    }

    public static void simulate() {
        String kafkaUrl = DEFAULT_KAFKA_URL; //(args.length > 0) ? args[0] : DEFAULT_KAFKA_URL;
        TwitterMonitorSimulator kafkaProducer = new TwitterMonitorSimulator(kafkaUrl);

        int timeSlot = 5;
        int secondsRunning = timeSlot;

        try {
          while (!kafkaProducer.isFinishTime(secondsRunning)) {
              String topic = "twitter";
              String msg = TweetsGenerator.getRandomTweets();
              KeyedMessage<Integer, String> data = new KeyedMessage<>(topic, msg);
              producer.send(data);
              TimeUnit.SECONDS.sleep(timeSlot);
              secondsRunning += timeSlot;
          }
        } catch (InterruptedException e) {
          System.err.println("InterruptedException: " + e.getMessage());
        }

        producer.close();
    }

    private boolean isFinishTime(int secondsRunning){
        double maxMinutes = 0.5;
        return (secondsRunning > maxMinutes*60);
    }
}