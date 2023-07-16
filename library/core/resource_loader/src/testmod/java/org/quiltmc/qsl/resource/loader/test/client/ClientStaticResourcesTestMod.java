package org.quiltmc.qsl.resource.loader.test.client;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

public class ClientStaticResourcesTestMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltClientStaticResourcesTest");
	@Override
	public void onInitializeClient(ModContainer mod) {
		ResourceManager clientManager = ResourceLoader.getStaticResourceManager(ResourceType.CLIENT_RESOURCES);
		ResourceManager serverManager = ResourceLoader.getStaticResourceManager(ResourceType.SERVER_DATA);
		try (BufferedReader reader = clientManager.openAsReader(new Identifier("cronch", "test_client.txt"))) {
			Stream<String> stringStream = reader.lines();
			List<String> strings = stringStream.collect(Collectors.toCollection((Supplier<List<String>>) ArrayList::new));
			for (String s : strings) {
				LOGGER.info("String read in clientside resource 'cronch:test_client': {}", s);
			}
		} catch (Exception e) {
			LOGGER.error("Exception while reading clientside resource: {}", e.toString());
		}
		try (BufferedReader reader = serverManager.openAsReader(new Identifier("test", "bar_test/testfile.txt"))) {
			Stream<String> stringStream = reader.lines();
			List<String> strings = stringStream.collect(Collectors.toCollection((Supplier<List<String>>) ArrayList::new));
			for (String s : strings) {
				LOGGER.info("String read in serverside resource 'test:bar_test/testfile' on clientside: {}", s);
			}
		} catch (Exception e) {
			LOGGER.error("Exception while reading serverside resource from client: {} ", e.toString());
		}
	}
}
