package org.quiltmc.qsl.resource.loader.api;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.resource.loader.impl.StaticResourceLoaderImpl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface StaticResourceLoader {

	static StaticResourceLoader get(ResourceType type){
		return StaticResourceLoaderImpl.get(type);
	}
	List<Resource> getAllResources(Identifier id);
	Set<String> getNamespaces();
	List<ResourcePack> getPacks();
	Map<Identifier, List<Resource>> findResources(String startingPath, Predicate<Identifier> pathFilter);
	Stream<ResourcePack> streamPacks();
	Optional<Resource> getResource(Identifier id);
	Resource getResourceOrThrow(Identifier id) throws FileNotFoundException;
	InputStream open(Identifier id) throws IOException;
	BufferedReader openAsReader(Identifier id) throws IOException;

}
