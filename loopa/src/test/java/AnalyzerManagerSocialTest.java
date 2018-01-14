import org.loopa.element.functionallogic.enactor.analyzer.AnalyzerManagerSocial;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AnalyzerManagerSocialTest {
	AnalyzerManagerSocial am;
	Method methodIsWorkingProperly;
	Field fieldLT, fieldLTE, fieldMF, fieldMFCR;

	@Before
	public void initializeComponents() throws NoSuchFieldException, IllegalAccessException, ParseException, IllegalAccessException, NoSuchMethodException {
		am = new AnalyzerManagerSocial();

		fieldMF = AnalyzerManagerSocial.class.getDeclaredField("maxFreq");
		fieldMF.setAccessible(true);
		fieldMFCR = AnalyzerManagerSocial.class.getDeclaredField("maxFreqChangeRate");
		fieldMFCR.setAccessible(true);
		fieldLT = AnalyzerManagerSocial.class.getDeclaredField("lastTime");
		fieldLT.setAccessible(true);
		fieldLTE = AnalyzerManagerSocial.class.getDeclaredField("lastTimeElapsed");
		fieldLTE.setAccessible(true);

		methodIsWorkingProperly = AnalyzerManagerSocial.class.getDeclaredMethod("isWorkingProperly", Instant.class);
		methodIsWorkingProperly.setAccessible(true);
	}

	@Test
	public void testMethodIsWorkingProperlyWhenItShouldReturnTrue() throws IllegalAccessException, ParseException, InvocationTargetException {
		fieldMF.setInt(am, 30);
		fieldMFCR.setInt(am, 30);
		fieldLT.set(am, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:10").toInstant());
		fieldLTE.set(am, 0L);

		Instant currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:35").toInstant();
		boolean workingProperly = (boolean) methodIsWorkingProperly.invoke(am, currentTime);
		assertTrue(workingProperly);
	}

	@Test
	public void testMethodIsWorkingProperlyWhenItShouldReturnFalseDueToMaxFreq() throws IllegalAccessException, ParseException, InvocationTargetException {
		fieldMF.setInt(am, 30);
		fieldMFCR.setInt(am, 100);
		fieldLT.set(am, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:10").toInstant());
		fieldLTE.set(am, 0L);

		Instant currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:50").toInstant();
		boolean workingProperly = (boolean) methodIsWorkingProperly.invoke(am, currentTime);
		assertFalse(workingProperly);
	}

	@Test
	public void testMethodIsWorkingProperlyWhenItShouldReturnFalseDueToMaxFreqChangeRate() throws IllegalAccessException, ParseException, InvocationTargetException {
		fieldMF.setInt(am, 30);
		fieldMFCR.setInt(am, 20);
		fieldLT.set(am, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:10").toInstant());
		fieldLTE.set(am, 0L);

		Instant currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-12-15 10:20:35").toInstant();
		boolean workingProperly = (boolean) methodIsWorkingProperly.invoke(am, currentTime);
		assertFalse(workingProperly);
	}
}
