package org.loopa.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;

import java.util.stream.Collectors;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.sql.Timestamp;
import org.json.JSONObject;
import org.json.JSONArray;
import java.lang.reflect.*;

import org.loopa.monitor.IMonitor;
import org.loopa.analyzer.IAnalyzer;
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
    private IAnalyzer analyzer;
    private KafkaConsumer<String, String> consumer;

    public KafkaService(String id, IMonitor monitor, IAnalyzer analyzer, String url, String topicRead, String topicWrite, Class classDataItemType) {
        ksID = id;
        this.monitor = monitor;
        this.analyzer = analyzer;
        kafkaUrl = url;
        kafkaTopicRead = topicRead;
        kafkaTopicWrite = topicWrite;
        this.classDataItemType = classDataItemType;
        createConsumer();
    }

    private void createConsumer() {
        this.consumer = new KafkaConsumer<>(createConsumerProperties());
        consumer.subscribe(Arrays.asList(this.kafkaTopicRead));
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
        String type = message.getMessageType();
        switch (type) {
    		case "request":
    			readLastMessages(monitor.getReceiver());
    			break;
    		case "response":
          sendToAnalyzer(message, analyzer.getReceiver());
    			break;
    		default:
    			System.err.println("Invalid type code in processRequest");
    		}
    }

    private void sendToAnalyzer(IMessage monitorMessage, IReceiver receiver) {
      IMessage analyzerMessage = new Message(this.ksID, receiver.getComponentId(), 1, "request", monitorMessage.getMessageBody());
      receiver.doOperation(analyzerMessage);
    }

    private String buildResponseKafkaMessage(Map<String, String> messageBody){
      messageBody.remove("type");
      return messageBody.toString();
    }

    private void writeMessage(String msg) {
        Properties properties = createProducerProperties();
        Producer<Integer, String> producer = new Producer<>(new ProducerConfig(properties));
        KeyedMessage<Integer, String> data = new KeyedMessage<>(kafkaTopicWrite, msg);
        producer.send(data);
        producer.close();
    }

    private void readLastMessages(IReceiver receiver) {
        ConsumerRecords<String, String> records = consumer.poll(0);
        if (!records.isEmpty()) {
          List<ConsumerRecord<String, String>> listRecords = records.records(new TopicPartition(this.kafkaTopicRead, 0));
          ArrayList<ObtainedData> arrayObtainedDatas = new ArrayList();
          int totalRecords = listRecords.size();
          for (int i = 0; i < totalRecords; i++) {
            ConsumerRecord<String, String> currentRecord = listRecords.get(i);
            arrayObtainedDatas.add( getObtainedDataFromKafkaRecord(currentRecord) );
          }
          receiver.doOperation(ObtainedData.toMessage(arrayObtainedDatas, this.ksID, receiver.getComponentId(), 1, "response"));
        }
        consumer.commitSync();
    }

    private ObtainedData getObtainedDataFromKafkaRecord(ConsumerRecord<String, String> record) {
      String message = new String(record.value());
      JSONObject jsonDataObject  = new JSONObject(message); // json with all data
      JSONObject twitterData = jsonDataObject.getJSONObject("SocialNetworksMonitoredData");
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
