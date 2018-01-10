import static org.junit.Assert.*;

import java.util.*;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.*;

public class IReceivedTest {
    int configId;
    int numDataItems;
    ArrayList<DataItem> dataItems = new ArrayList();
    int idOutput;
    Timestamp searchTimeStamp;
    ObtainedData od;
    IReceived received = new Received();

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  	@Before
  	public void initializeComponents() {
        configId = 1;
        idOutput = 1;
        searchTimeStamp = new Timestamp(new Date().getTime());
        generateDataItems();
        od = new ObtainedData(configId, numDataItems, dataItems, idOutput, searchTimeStamp);
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

  	@Ignore @Test
  	public void testDoOperation() {
        // TODO WIP
        received.doOperation(od);
        assertEquals(od.toString()+"\n", systemOutRule.getLog());
  	}
}
