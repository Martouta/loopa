import static org.junit.Assert.*;

import java.util.*;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class DataItemTwitterTest {
    Timestamp timestamp;
    String author;
    String link;
    Long id;
    String message;
    DataItem dataItem;


  	@Before
  	public void initializeComponents() {
        timestamp = new Timestamp(new Date().getTime());
        author = "authorName";
        link = "http://twitter.com/routeTweet";
        id = 114749583439036416L;
        message = "Tweet Button, Follow Button, and Web Intents javascript now support SSL http://t.co/9fbA0oYy ^TS";
    		dataItem = new DataItemTwitter(timestamp, author, link, id, message);
  	}

  	@Test
  	public void testToString() {
        String expected = "DataItem [timeStamp="+timestamp+", author="+author+", link="+link+", id="+id+", message="+message+"]";
        assertEquals(expected, dataItem.toString());
  	}
}
