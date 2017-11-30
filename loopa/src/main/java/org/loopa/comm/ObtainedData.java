import java.util.ArrayList;
import java.sql.Timestamp;
public class ObtainedData {
    private int configId;
    private int numDataItems;
    private ArrayList<DataItem> dataItems;
    private int idOutput;
    private Timestamp searchTimeStamp;

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

    public IMessage toMessage(String from, String to){
        String code = "001", type = "response";
        return new Message(to, from, code, type, getFieldsHashMap());
    }

    private HashMap<String, String> getFieldsHashMap(){ // so far it does it without the attributes of the tweet itself because we don't use it
      HashMap hm = new HashMap();
      for (Field field : this.getClass().getDeclaredFields()) {
          hm.put(field.getName(), field.get(this).toString());
      }
      return hm;
    }
}
