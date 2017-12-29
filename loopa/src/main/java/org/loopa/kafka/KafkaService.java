package org.loopa.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

// import kafka.consumer.*;
// import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.stream.Collectors;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.sql.Timestamp;
import org.json.JSONObject;
import org.json.JSONArray;
import java.lang.reflect.*;

import org.loopa.monitor.IMonitor;
import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.element.receiver.IReceiver;
import org.loopa.comm.obtaineddata.*;

public class KafkaService {
    private String ksID;
    private String kafkaUrl;
    private String kafkaTopicRead;
    private String kafkaTopicWrite;
    private Class classDataItemType;
    private IMonitor monitor;
    private KafkaConsumer<String, String> consumer;

    public KafkaService(String id, IMonitor monitor, String url, String topicRead, String topicWrite, Class classDataItemType) {
        ksID = id;
        this.monitor = monitor;
        kafkaUrl = url;
        kafkaTopicRead = topicRead;
        kafkaTopicWrite = topicWrite;
        this.classDataItemType = classDataItemType;
        createConsumer();
    }

    private void createConsumer() {
        this.consumer = new KafkaConsumer<>(createConsumerProperties());
        consumer.subscribe(Arrays.asList(this.kafkaTopicRead));
        consumer.poll(0); // INFO: subscribe() and assign() are lazy -- thus, you also need to do a "dummy call" to poll() before you can use seek()
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();
        String groupID = this.classDataItemType.getSimpleName().replaceAll("DataItem","");
        properties.put("group.id", groupID);

        properties.put("bootstrap.servers", kafkaUrl+":9092");
        properties.put("enable.auto.commit", "false");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

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
        // TODO switch case
        String type = message.getMessageType();
        if (type == "request") { readLastMessage(monitor.getReceiver()); }
        else if (type == "response") { System.out.println("TODO response"); }
    }

    public void writeMessage(String msg) {
        Properties properties = createProducerProperties();
        Producer<Integer, String> producer = new Producer<>(new ProducerConfig(properties));
        KeyedMessage<Integer, String> data = new KeyedMessage<>(kafkaTopicRead, msg);
        producer.send(data);
        producer.close();
    }

    public void readLastMessage(IReceiver receiver) {
        System.out.println("Llega a KafkaService#readLastMessage");

        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
          System.out.println(record.toString()); // TODO
        }
        consumer.commitSync();

        System.out.println("------------------------------------");
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
