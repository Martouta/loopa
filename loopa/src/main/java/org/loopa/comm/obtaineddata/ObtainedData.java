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
    private int idOutput;
    private Timestamp searchTimeStamp;
    private static String splitRegexPattern = ";";

    /**
  	* ObtainedData constructor
  	*/
  	private ObtainedData() {}

    /**
  	* ObtainedData constructor
  	*/
  	public ObtainedData(int configId, int numDataItems, int idOutput, Timestamp searchTimeStamp) {
    		this.configId = configId;
    		this.numDataItems = numDataItems;
    		this.idOutput = idOutput;
    		this.searchTimeStamp = searchTimeStamp;
  	}

  	/**
  	* Create string representation of ObtainedData for printing
  	* @return
  	*/
  	@Override
  	public String toString() {
  		  return "ObtainedData [configId=" + configId + ", numDataItems=" + numDataItems + ", idOutput=" + idOutput + ", searchTimeStamp=" + searchTimeStamp + "]";
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

    private static HashMap<String, String> getFieldsHashMap(ArrayList<ObtainedData> arrayObtainedData) {
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

    private HashMap<String, String> getFieldsHashMap() {
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

    public static ObtainedData fromMessage(IMessage message) {
        Map<String, String> hmBodyMessage = message.getMessageBody();
        ObtainedData od = new ObtainedData();

        for (Field field : ObtainedData.class.getDeclaredFields()) {
            String value = hmBodyMessage.get(field.getName());
            if (value != null) { setValueToField(od, field, value); }
        }

        return od;
    }

    private static void setValueToField(ObtainedData od, Field field, String value) {
        try {
          field.set(od, convertValueFromStringToRealType(field, value));
        } catch (IllegalAccessException e) {
          System.err.println("IllegalAccessException: " + e.getMessage());
        }
    }

    private static Object convertValueFromStringToRealType(Field field, String strValue) {
        Object convertedValue = null;
        try {
            if (int.class.equals(field.getType())) {
                convertedValue = Integer.parseInt(strValue);
            } else {
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
