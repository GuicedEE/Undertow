package com.jwebmp.undertow;

import com.jwebmp.logger.LogFactory;
import io.undertow.Undertow;
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
			Undertow ud = GuicedUndertow.boot("localhost", 9999);
			System.out.println("done");
			ud.stop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
