package com.guicedee.guicedservlets.undertow;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

public class TestIT
{
	@Test
	public void testUndertow()
	{
		try
		{
			GuicedUndertow.boot("localhost", 9999);
			System.out.println("done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
