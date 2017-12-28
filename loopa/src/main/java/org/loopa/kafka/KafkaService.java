package org.loopa.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

// import kafka.consumer.*;
// import kafka.javaapi.consumer.ConsumerConnector;
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

    public KafkaService(String id, IMonitor monitor, String url, String topicRead, String topicWrite, Class classDataItemType) {
        ksID = id;
        this.monitor = monitor;
        kafkaUrl = url;
        kafkaTopicRead = topicRead;
        kafkaTopicWrite = topicWrite;
        this.classDataItemType = classDataItemType;
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();
        properties.put("group.id", "twitterGroup"); // TODO dynamic?

        // properties.put("zookeeper.connect", kafkaUrl+":2181");
        // properties.put("zookeeper.session.timeout.ms", "500");
        // properties.put("zookeeper.sync.time.ms", "250");
        // properties.put("auto.commit.interval.ms", "1000");

        properties.put("bootstrap.servers", kafkaUrl+":9092");
        properties.put("auto.offset.reset", "latest"); // TODO testing so far
        properties.put("enable.auto.commit", "false");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // properties.put("bootstrap.servers", kafkaUrl+":9092");
        // properties.put("enable.auto.commit", "true");
        // properties.put("auto.commit.interval.ms", "1000");
        // properties.put("session.timeout.ms", "30000");
        // properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

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
        Properties properties = createConsumerProperties();

        // ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        // Map<String, Integer> topicCount = new HashMap<>();
        // topicCount.put(kafkaTopicRead, 1);
        // Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        // List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(kafkaTopicRead);
        // for (final KafkaStream stream : streams) {
        //     ConsumerIterator<byte[], byte[]> it = stream.iterator();
        //     while (it.hasNext()) {
        //         String kafkaMessage = new String(it.next().message());
        //         ObtainedData obtainedData = getObtainedDataFromMessage(kafkaMessage);
        //         System.out.println(obtainedData.toString()); // TODO
        //         // receiver.doOperation(obtainedData.toMessage(ksID, receiver.getComponentId()));
        //     }
        // }
        // if (consumer != null) { consumer.shutdown(); }


        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(kafkaTopicRead));
        List<TopicPartition> partitions = consumer.partitionsFor(kafkaTopicRead).stream().map(part->{TopicPartition tp = new TopicPartition(part.topic(),part.partition()); return tp;}).collect(Collectors.toList());
        //    consumer.poll(0); // INFO: subscribe() and assign() are lazy -- thus, you also need to do a "dummy call" to poll() before you can use seek()
        // TopicPartition topicPartition = partitions.get(0); //new TopicPartition(kafkaTopicRead, 0);
        //consumer.seek(partitions.get(0), -1);
        //    consumer.seekToEnd(partitions);
        //consumer.seek(partitions.get(0), (consumer.position(partitions.get(0)) - 1L));
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
          System.out.println(record.toString()); // TODO
        }
        consumer.commitSync();


        // KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        // consumer.subscribe(Arrays.asList(kafkaTopicRead));
        // ConsumerRecords<String, String> records = consumer.poll(100);
        // for (ConsumerRecord<String, String> record : records) {
        //   System.out.println(record.toString()); // TODO
        // }


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
