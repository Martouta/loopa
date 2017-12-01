import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.sql.Timestamp;
import org.json.JSONObject;
import org.json.JSONArray;
import java.lang.reflect.*;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.element.receiver.IReceiver;

public class KafkaService {
    private String kafkaUrl;
    private String kafkaTopic;

    public KafkaService(String url, String topic) {
        kafkaUrl = url;
        kafkaTopic = topic;
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", kafkaUrl+":2181");
        properties.put("group.id", "twitterGroup");
        properties.put("zookeeper.session.timeout.ms", "500");
        properties.put("zookeeper.sync.time.ms", "250");
        properties.put("auto.commit.interval.ms", "1000");
        return properties;
    }

    private Properties createProducerProperties() {
        Properties properties = new Properties();
        properties.put("metadata.broker.list", kafkaUrl+":9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        return properties;
    }

    public void writeMessage(String msg) {
        Properties properties = createProducerProperties();
        Producer<Integer, String> producer = new Producer<>(new ProducerConfig(properties));
        KeyedMessage<Integer, String> data = new KeyedMessage<>(kafkaTopic, msg);
        producer.send(data);
        producer.close();
    }

    public void readMessages(Class dataItemClass, IReceiver receiver) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Properties properties = createConsumerProperties();
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        testConsumer(consumer, dataItemClass, receiver);
        if (consumer != null) { consumer.shutdown(); }
    }

    public void processRequest(IMessage m) {
      // TODO
      // imprimir por pantalla
      // acabara haciewndo request al readmessage
    }

    private void testConsumer(ConsumerConnector consumer, Class dataItemClass, IReceiver receiver) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Map<String, Integer> topicCount = new HashMap<>();
        topicCount.put(kafkaTopic, 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(kafkaTopic);
        for (final KafkaStream stream : streams) {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                String kafkaMessage = new String(it.next().message());
                ObtainedData obtainedData = getObtainedDataFromMessage(kafkaMessage, dataItemClass);
                receiver.doOperation(obtainedData.toMessage("kafkaServiceTwitter", receiver.getComponentId()));
            }
        }
    }

    private ObtainedData getObtainedDataFromMessage(String message, Class dataItemClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      JSONObject jsonDataObject  = new JSONObject(message); // json with all data
      JSONObject twitterData = jsonDataObject.getJSONObject("SocialNetworksMonitoredData"); // get SocialNetworksMonitoredData object (twitterCase)
      int configId = twitterData.getInt("confId");
      int numDataItems = twitterData.getInt("numDataItems");
      int idOutput = twitterData.getInt("idOutput");
      Timestamp searchTimeStamp = Timestamp.valueOf( twitterData.getString("searchTimeStamp") );
      ObtainedData od = new ObtainedData(configId, numDataItems, idOutput, searchTimeStamp);
      JSONArray dataItemsArray = twitterData.getJSONArray("DataItems");
      for (int i = 0; i < numDataItems; i++) {
          JSONObject jsonDataItem = (JSONObject) dataItemsArray.get(0);
          Method fromJSONObjectMethod = dataItemClass.getMethod("fromJSONObject", JSONObject.class);
          DataItem dataItem = (DataItem) fromJSONObjectMethod.invoke(null, jsonDataItem);
          od.addDataItem(dataItem);
      }
      return od;
    }
}
