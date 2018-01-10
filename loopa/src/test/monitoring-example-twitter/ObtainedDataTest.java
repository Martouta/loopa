import static org.junit.Assert.*;

import java.util.*;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class ObtainedDataTest {
    int configId;
    int numDataItems;
    ArrayList<DataItem> dataItems = new ArrayList();
    int idOutput;
    Timestamp searchTimeStamp;
    ObtainedData obtainedData;

  	@Before
  	public void initializeComponents() {
        configId = 1;
        idOutput = 1;
        searchTimeStamp = new Timestamp(new Date().getTime());
        generateDataItems();
        obtainedData = new ObtainedData(configId, numDataItems, dataItems, idOutput, searchTimeStamp);
  	}

    private void generateDataItems() {
      String[] messages = {
          "Tweet Button, Follow Button, and Web Intents javascript now support SSL http://t.co/9fbA0oYy ^TS",
          "Second twitter message :)"
      };
      for (int i = 1; i <= 2; i++) { // create dataItems
          Timestamp timestamp = new Timestamp(new Date().getTime());
          String author = "authorName"+i;
          String link = "http://twitter.com/routeTweet"+i;
          Long id = 114749583439036416L + i;
          String message = messages[i-1];
          dataItems.add(new DataItemTwitter(timestamp, author, link, id, message));
      }
      numDataItems = dataItems.size();
    }

  	@Test
  	public void testToString() {
        String expected = "ObtainedData [configId="+configId+", numDataItems="+numDataItems+", DataItems=["+dataItems.get(0).toString()+", "+dataItems.get(1).toString()+"], idOutput="+idOutput+", searchTimeStamp="+searchTimeStamp+"]";
        assertEquals(expected, obtainedData.toString());
  	}
}
