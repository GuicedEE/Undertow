package com.guicedee.guicedservlets.undertow;

import com.guicedee.logger.LogFactory;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

public class TestIT
{
	@Test
	public void testUndertow()
	{
		try
		{
			LogFactory.configureConsoleColourOutput(Level.FINE);
			GuicedUndertow.boot("localhost", 9999);
			System.out.println("done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
