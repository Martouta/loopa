import static org.junit.Assert.*;

import java.util.*;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class ObtainedDataTest {
    int configId;
    int numDataItems;
    int idOutput;
    Timestamp searchTimeStamp;
    ObtainedData obtainedData;

  	@Before
  	public void initializeComponents() {
        configId = 1;
        idOutput = 1;
        searchTimeStamp = new Timestamp(new Date().getTime());
        numDataItems = 2;
        obtainedData = new ObtainedData(configId, numDataItems, idOutput, searchTimeStamp);
  	}

  	@Test
  	public void testToString() {
        String expected = "ObtainedData [configId="+configId+", numDataItems="+numDataItems+", idOutput="+idOutput+", searchTimeStamp="+searchTimeStamp+"]";
        assertEquals(expected, obtainedData.toString());
  	}
}
