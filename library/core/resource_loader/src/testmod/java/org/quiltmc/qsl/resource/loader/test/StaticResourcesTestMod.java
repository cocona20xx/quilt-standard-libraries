package org.quiltmc.qsl.resource.loader.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class StaticResourcesTestMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltStaticResourcesTestMod");
	@Override
	public void onInitialize(ModContainer mod) {
		MultiPackResourceManager staticManager = ResourceLoader.getStaticResourceManager(ResourceType.SERVER_DATA);
		LOGGER.info("Loaded static namespaces: {}", staticManager.getAllNamespaces());
		Map<Identifier, Resource> blockRes = staticManager.findResources("add_block", identifier -> true);
		Map<Identifier, JsonElement> blockElements = new HashMap<>();
		for(Map.Entry<Identifier, Resource> r : blockRes.entrySet()){
			try {
				JsonElement element = JsonParser.parseReader(r.getValue().openBufferedReader());
				blockElements.put(r.getKey(), element);
			} catch (Exception e) {
				LOGGER.error(e.toString());
			}
		}
		for(Map.Entry<Identifier, JsonElement> r : blockElements.entrySet()) {
			String blockName = r.getValue().getAsJsonObject().get("block").getAsString();
			Registry.register(Registries.BLOCK,
				new Identifier(r.getKey().getNamespace(), blockName),
				new Block(AbstractBlock.Settings.copy(Blocks.RED_WOOL)));
			LOGGER.info("Registered block: {} via data!", new Identifier(r.getKey().getNamespace(), blockName));
		}
	}
}
