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
        numDataItems = 2;
        od = new ObtainedData(configId, numDataItems, idOutput, searchTimeStamp);
  	}

  	@Ignore @Test
  	public void testDoOperation() {
        // TODO WIP
        received.doOperation(od);
        assertEquals(od.toString()+"\n", systemOutRule.getLog());
  	}
}
