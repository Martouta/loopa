package org.loopa.kafka;

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
import org.loopa.comm.obtaineddata.*;

public class KafkaService {
    private String kafkaUrl;
    private String kafkaTopicRead;
    private String kafkaTopicWrite;
    private Class classDataItemType;

    public KafkaService(String url, String topicRead, String topicWrite, Class classDataItemType) {
        kafkaUrl = url;
        kafkaTopicRead = topicRead;
        kafkaTopicWrite = topicWrite;
        this.classDataItemType = classDataItemType;
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

    public void processRequest(IMessage message) {
        // TODO: Si es request, leer de kafka y enviarlo al monitor (al receiver de algun modo?) y si es response, escribir en otro kafka topic
        System.out.println("KafkaService#processRequest");
        System.out.println("body: " + message.getMessageBody());
        System.out.println("from: " + message.getMessageFrom());
        System.out.println("to: " + message.getMessageTo());
        System.out.println("type: " + message.getMessageType());
        System.out.println("code: " + message.getMessageCode());
        System.out.println( ObtainedData.fromMessage(message) );
        System.out.println("------------------------------------");
    }

    public void writeMessage(String msg) {
        Properties properties = createProducerProperties();
        Producer<Integer, String> producer = new Producer<>(new ProducerConfig(properties));
        KeyedMessage<Integer, String> data = new KeyedMessage<>(kafkaTopicRead, msg);
        producer.send(data);
        producer.close();
    }

    public void readMessages(IReceiver receiver) {
        Properties properties = createConsumerProperties();
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        testConsumer(consumer, receiver);
        if (consumer != null) { consumer.shutdown(); }
    }

    private void testConsumer(ConsumerConnector consumer, IReceiver receiver) {
        Map<String, Integer> topicCount = new HashMap<>();
        topicCount.put(kafkaTopicRead, 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(kafkaTopicRead);
        for (final KafkaStream stream : streams) {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                String kafkaMessage = new String(it.next().message());
                ObtainedData obtainedData = getObtainedDataFromMessage(kafkaMessage);
                receiver.doOperation(obtainedData.toMessage("kafkaServiceTwitter", receiver.getComponentId()));
            }
        }
    }

    private ObtainedData getObtainedDataFromMessage(String message) {
      JSONObject jsonDataObject  = new JSONObject(message); // json with all data
      JSONObject twitterData = jsonDataObject.getJSONObject("SocialNetworksMonitoredData"); // get SocialNetworksMonitoredData object (twitterCase)
      int configId = twitterData.getInt("confId");
      int numDataItems = twitterData.getInt("numDataItems");
      int idOutput = twitterData.getInt("idOutput");
      Timestamp searchTimeStamp = Timestamp.valueOf( twitterData.getString("searchTimeStamp") );
      ObtainedData od = new ObtainedData(configId, numDataItems, idOutput, searchTimeStamp);
      JSONArray dataItemsArray = twitterData.getJSONArray("DataItems");
      try {
        Method fromJSONObjectMethod = classDataItemType.getMethod("fromJSONObject", JSONObject.class);
        for (int i = 0; i < numDataItems; i++) {
          JSONObject jsonDataItem = (JSONObject) dataItemsArray.get(0);
          DataItem dataItem = (DataItem) fromJSONObjectMethod.invoke(null, jsonDataItem);
          od.addDataItem(dataItem);
        }
      } catch (IllegalAccessException e) {
        System.err.println("IllegalAccessException: " + e.getMessage());
      } catch (NoSuchMethodException e) {
        System.err.println("NoSuchMethodException: " + e.getMessage());
      } catch (InvocationTargetException e) {
        System.err.println("InvocationTargetException: " + e.getMessage());
      }

      return od;
    }
}
