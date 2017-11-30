import java.sql.Timestamp;
import org.json.JSONObject;

public class DataItemTwitter extends DataItem {
    private Timestamp timeStamp;
    private String author;
    private String link;
    private Long id;
    private String message;

    /**
  	* Default DataItemTwitter constructor
  	*/
  	public DataItemTwitter(Timestamp timeStamp, String author, String link, Long id, String message) {
    		super();
    		this.timeStamp = timeStamp;
    		this.author = author;
    		this.link = link;
    		this.id = id;
    		this.message = message;
  	}

  	/**
  	* Create string representation of DataItemTwitter for printing
  	* @return
  	*/
  	@Override
  	public String toString() {
  		  return "DataItem [timeStamp=" + timeStamp + ", author=" + author + ", link=" + link + ", id=" + id + ", message=" + message + "]";
  	}

    /**
  	* Create a DataItemTwitter from a JSONObject with its data
  	* @return
  	*/
    public static DataItem fromJSONObject(JSONObject jsonDataItem) {
        Timestamp timestamp = Timestamp.valueOf( jsonDataItem.getString("timeStamp") );
        String author = jsonDataItem.getString("author");
        String link = jsonDataItem.getString("link");
        Long id = jsonDataItem.getLong("id");
        String twitterMessage = jsonDataItem.getString("message");
        return new DataItemTwitter(timestamp, author, link, id, twitterMessage);
    }
}
