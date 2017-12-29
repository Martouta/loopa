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

    /**
  	* Add a new DataItem to the list
  	*/
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

    /**
    * Create a Message from this ObtainedData's data in the body
    * @return
    */
    public IMessage toMessage(String from, String to, String type, int code){
      return new Message(from, to, code, type, getFieldsHashMap());
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

    /**
  	* Create a Obtained from a Message with its data
  	* @return
  	*/
    public static ObtainedData fromMessage(IMessage message) { // so far it does it without the attributes of the tweet itself because we don't use it
        Map<String, String> hmBodyMessage = message.getMessageBody();
        ObtainedData od = new ObtainedData();

        try {
            for (Field field : ObtainedData.class.getDeclaredFields()) {
                String value = hmBodyMessage.get(field.getName());
                if (value != null) { setValueToField(od, field, value); }
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

    private static void setValueToField(ObtainedData od, Field field, String value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException { // so far it does it without the attributes of the tweet itself because we don't use it
        if (int.class.equals(field.getType())) {
            field.set(od, Integer.parseInt(value));
        } else if(field.getName() != "dataItems") {
            Method parseMethod = field.getType().getMethod("valueOf", String.class);
            field.set(od, parseMethod.invoke(field, value));
        }
    }
}
