package org.quiltmc.qsl.resource.loader.test;

import com.google.gson.JsonElement;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.StaticResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class StaticResourcesTestMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltStaticResourcesTestMod");
	@Override
	public void onInitialize(ModContainer mod) {
		StaticResourceLoader dataLoader = StaticResourceLoader.get(ResourceType.SERVER_DATA);
		Set<String> expectedNamespaces = Set.of("foo_test", "bar_test");
		if(dataLoader.getNamespaces().equals(expectedNamespaces)) LOGGER.info("Expected namespaces found in test!");
		else LOGGER.error("Expected and actual namespaces do not match. Expected: {}, Actual: {}", expectedNamespaces, dataLoader.getNamespaces());
		Map<Identifier, JsonElement> elements = dataLoader.findJsonElements("test", "foo_test");
		LOGGER.info(elements.keySet().toString());
	}
}
