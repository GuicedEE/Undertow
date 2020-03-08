package com.guicedee.guicedservlets.undertow.implementations;

import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleExclusions;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class UndertowModuleExclusions
		implements IGuiceScanModuleExclusions<UndertowModuleExclusions>
{
	@Override
	public @NotNull Set<String> excludeModules()
	{
		Set<String> strings = new HashSet<>();
		strings.add("com.guicedee.undertow");
		strings.add("undertow.core");
		strings.add("undertow.servlet");
		strings.add("xnio.api");
		strings.add("undertow.websockets.jsr");
		return strings;
	}
}
