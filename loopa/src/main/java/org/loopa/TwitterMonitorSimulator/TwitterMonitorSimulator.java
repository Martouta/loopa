import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.Instant;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class TwitterMonitorSimulator {
    private static Producer<Integer, String> producer;
    private Properties properties;

    public TwitterMonitorSimulator(String kafkaUrl) {
        properties = new Properties();
        properties.put("metadata.broker.list", kafkaUrl+":9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        producer = new Producer<>(new ProducerConfig(properties));
    }

    public static void simulate(String kafkaUrl, String topic) {
        TwitterMonitorSimulator kafkaProducer = new TwitterMonitorSimulator(kafkaUrl);
        final int maxMessages = 5*3, timeSlot = 10;

        Thread threadAll = new Thread("ThreadSimulateTwitterAllMessages") {
          public void run(){
            int numMessages = 0;
            Instant startTime = Instant.now();
            while (numMessages < maxMessages) {
                String msg = TweetsGenerator.getRandomTweets();
                KeyedMessage<Integer, String> data = new KeyedMessage<>(topic, msg);
                producer.send(data);
                numMessages++;
                try {
                  Instant endTime = Instant.now();
                  Long waitTime = Long.valueOf(timeSlot) - Duration.between(startTime, endTime).toMillis();
                  if (waitTime > 0) {
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                  }
                  startTime = Instant.now();
                } catch (InterruptedException e) {
                  System.err.println("InterruptedException: " + e.getMessage());
                }
            }
            producer.close();
          }
        };
        threadAll.start();
    }
}
