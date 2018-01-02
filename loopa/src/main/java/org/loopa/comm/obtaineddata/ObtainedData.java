package org.loopa.comm.obtaineddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.lang.reflect.*;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.Message;
import org.loopa.element.receiver.IReceiver;

public class ObtainedData {
    private int configId;
    private int numDataItems;
    private ArrayList<DataItem> dataItems;
    private int idOutput;
    private Timestamp searchTimeStamp;
    private static String splitRegexPattern = ";";

    /**
  	* ObtainedData constructor
  	*/
  	public ObtainedData() { this.dataItems = new ArrayList(); }

    /**
  	* ObtainedData constructor
  	*/
  	public ObtainedData(int configId, int numDataItems, ArrayList<DataItem> dataItems, int idOutput, Timestamp searchTimeStamp) {
    		this.configId = configId;
    		this.numDataItems = numDataItems;
    		this.dataItems = dataItems;
    		this.idOutput = idOutput;
    		this.searchTimeStamp = searchTimeStamp;
  	}

    /**
  	* ObtainedData constructor
  	*/
  	public ObtainedData(int configId, int numDataItems, int idOutput, Timestamp searchTimeStamp) {
    		this.configId = configId;
    		this.numDataItems = numDataItems;
    		this.dataItems = new ArrayList();
    		this.idOutput = idOutput;
    		this.searchTimeStamp = searchTimeStamp;
  	}

    public void addDataItem(DataItem dataItem) {
        dataItems.add(dataItem);
    }

  	/**
  	* Create string representation of ObtainedData for printing
  	* @return
  	*/
  	@Override
  	public String toString() {
  		  return "ObtainedData [configId=" + configId + ", numDataItems=" + numDataItems + ", DataItems=" + dataItems.toString() + ", idOutput=" + idOutput + ", searchTimeStamp=" + searchTimeStamp + "]";
  	}

    public IMessage toMessage(String from, String to, int code, String type){
      return new Message(from, to, code, type, getFieldsHashMap());
    }

    public static IMessage toMessage(ArrayList<ObtainedData> arrayObtainedData, String from, String to, int code, String type) {
      return new Message(from, to, code, type, ObtainedData.getFieldsHashMap(arrayObtainedData));
    }

    public static ArrayList<Object> getValuesFromFieldnameInHashMap(Map<String, String> monData, String fieldKey) {
      String[] arrayStr = monData.get(fieldKey).split(splitRegexPattern);
      ArrayList<Object> arrayRealType = new ArrayList();
      for (String strValue : arrayStr) {
        try {
          arrayRealType.add(convertValueFromStringToRealType(ObtainedData.class.getDeclaredField(fieldKey), strValue));
        } catch (NoSuchFieldException e) {
          System.err.println("NoSuchFieldException: " + e.getMessage());
        }
      }
      return arrayRealType;
    }

    private static HashMap<String, String> getFieldsHashMap(ArrayList<ObtainedData> arrayObtainedData) { // so far it does it without the attributes of the tweet itself because we don't use it
      HashMap<String, String> hmBodyMessage = arrayObtainedData.get(0).getFieldsHashMap(); // initialized with the first obtainedData
      hmBodyMessage.put("type", "setMonData");
      int totalRecords = arrayObtainedData.size();
      for (int indexOD = 1; indexOD < totalRecords; indexOD++) { // Iterates the rest of ObtainedDatas of the array if there are more
        ObtainedData obtainedData = arrayObtainedData.get(indexOD);
        try {
          for (Field field : ObtainedData.class.getDeclaredFields()) {
              String fieldKey = field.getName(), newValue = field.get(obtainedData).toString();
              hmBodyMessage.put(fieldKey, hmBodyMessage.get(fieldKey) + splitRegexPattern + newValue);
          }
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException: " + e.getMessage());
        }
      }
      return hmBodyMessage;
    }

    private HashMap<String, String> getFieldsHashMap(){ // so far it does it without the attributes of the tweet itself because we don't use it
      HashMap<String, String> hmBodyMessage = new HashMap();
      hmBodyMessage.put("type", "setMonData");
      try {
        for (Field field : this.getClass().getDeclaredFields()) {
            hmBodyMessage.put(field.getName(), field.get(this).toString());
        }
      } catch (IllegalAccessException e) {
          System.err.println("IllegalAccessException: " + e.getMessage());
      }
      return hmBodyMessage;
    }

    public static ObtainedData fromMessage(IMessage message) { // so far it does it without the attributes of the tweet itself because we don't use it
        Map<String, String> hmBodyMessage = message.getMessageBody();
        ObtainedData od = new ObtainedData();

        for (Field field : ObtainedData.class.getDeclaredFields()) {
            String value = hmBodyMessage.get(field.getName());
            if (value != null) { setValueToField(od, field, value); }
        }

        return od;
    }

    private static void setValueToField(ObtainedData od, Field field, String value) { // so far it does it without the attributes of the tweet itself because we don't use it
        try {
          field.set(od, convertValueFromStringToRealType(field, value));
        } catch (IllegalAccessException e) {
          System.err.println("IllegalAccessException: " + e.getMessage());
        }
    }

    private static Object convertValueFromStringToRealType(Field field, String strValue) { // so far it does it without the attributes of the tweet itself because we don't use it
        Object convertedValue = null;
        try {
            if (int.class.equals(field.getType())) {
                convertedValue = Integer.parseInt(strValue);
            } else if(field.getName() != "dataItems") {
                Method parseMethod = field.getType().getMethod("valueOf", String.class);
                convertedValue = parseMethod.invoke(field, strValue);
            }
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            System.err.println("NoSuchMethodException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            System.err.println("InvocationTargetException: " + e.getMessage());
        }
        return convertedValue;
    }
}
