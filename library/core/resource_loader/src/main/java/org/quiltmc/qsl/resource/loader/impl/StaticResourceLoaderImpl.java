/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.resource.loader.impl;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.NioResourcePack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ZipResourcePack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.resource.loader.api.StaticResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApiStatus.Internal
public class StaticResourceLoaderImpl implements StaticResourceLoader {

	private static StaticResourceLoaderImpl resourcesInstance = null;
	private static StaticResourceLoaderImpl dataInstance = null;
	private static final Logger LOGGER = LoggerFactory.getLogger("QuiltStaticResourceLoaderImpl");
	private static final Gson GSON = new Gson();
	private static final String DATA_PATH_HEAD = "static/data";
	private static final String RESOURCES_PATH_HEAD = "static/resources";
	private static final int PACK_DEPTH = 3;
	private final StaticResourceManager manager;

	public static StaticResourceLoaderImpl get(ResourceType type) {
		if(type.equals(ResourceType.CLIENT_RESOURCES)) {
			if(!MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.CLIENT)) throw new RuntimeException("Static resources in 'static/resources' are client-only.");
			if(Objects.isNull(resourcesInstance)) {
				resourcesInstance = new StaticResourceLoaderImpl(type);
			}
			return resourcesInstance;
		} else if (type.equals(ResourceType.SERVER_DATA)) {
			if(Objects.isNull(dataInstance)) {
				dataInstance = new StaticResourceLoaderImpl(type);
			}
			return dataInstance;
		} else throw new RuntimeException("Invalid resource type passed when requesting static loader instance.");
	}

	StaticResourceLoaderImpl(ResourceType type){
		manager = new StaticResourceManager(type, getPacksForType(type));
	}

	private static List<ResourcePack> getPacksForType(@NotNull ResourceType type) {
		String pathHead;
		List<ResourcePack> returnList = new ArrayList<>();
		if (type.equals(ResourceType.CLIENT_RESOURCES)) {
			pathHead = RESOURCES_PATH_HEAD;
		} else if (type.equals(ResourceType.SERVER_DATA)) {
			pathHead = DATA_PATH_HEAD;
		} else throw new RuntimeException("Invalid resource type passed when loading static packs.");
		Path path = QuiltLoader.getGameDir();
		path = path.resolve(pathHead);
		path.iterator().forEachRemaining(path1 -> {
			//consumer lambda
			if(path1.getNameCount() == PACK_DEPTH) {
				File file = path1.toFile();
				if(file.isDirectory()) {
					returnList.add(new NioResourcePack(file.getName(), path1, false));
				} else if (file.isFile() && file.getName().endsWith(".zip")) {
					String name = file.getName();
					int i = name.lastIndexOf(".zip");
					name = name.substring(0, i);
					returnList.add(new ZipResourcePack(name, file, false));
				}
			}
		});
		return returnList;
	}

	@Override
	public List<Resource> getAllResources(Identifier id) {
		return manager.getAllResources(id);
	}
	@Override
	public Set<String> getNamespaces() {
		return manager.getAllNamespaces();
	}

	@Override
	public List<ResourcePack> getPacks() {
		return manager.getPacks();
	}

	@Override
	public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> pathFilter) {
		return manager.findAllResources(startingPath, pathFilter);
	}

	@Override
	public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> pathFilter) {
		return manager.findResources(startingPath, pathFilter);
	}

	@Override
	public Stream<ResourcePack> streamPacks() {
		return manager.streamResourcePacks();
	}

	@Override
	public Optional<Resource> getResource(Identifier id) {
		return manager.getResource(id);
	}

	@Override
	public Resource getResourceOrThrow(Identifier id) throws FileNotFoundException {
		return manager.getResourceOrThrow(id);
	}

	@Override
	public InputStream open(Identifier id) throws IOException {
		return manager.open(id);
	}

	@Override
	public BufferedReader openAsReader(Identifier id) throws IOException {
		return manager.openAsReader(id);
	}

	@Override
	public Map<Identifier, JsonElement> findJsonObjects(@Nullable String namespace, @Nullable String startingPath) {
		String nullablePath = Objects.isNull(startingPath) ? "" : startingPath;
		Map<Identifier, Resource> resourceMap = manager.findResources(nullablePath, identifier -> {
			boolean namespaceTest = Objects.isNull(namespace) || namespace.equals("") || namespace.equals(identifier.getNamespace());
			if(namespaceTest) {
				return identifier.getPath().endsWith(".json");
			} else return false;
		});
		Map<Identifier, JsonElement> returnMap = new HashMap<>();
		for(Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()) {
			Resource resource = entry.getValue();
			try(BufferedReader reader = resource.openBufferedReader()) {
				JsonElement element = JsonHelper.deserialize(GSON, reader, JsonElement.class);
				returnMap.put(entry.getKey(), element);
			} catch (Exception e) {
				LOGGER.warn("Exception caught during static json loading: " + e.getMessage());
			}
		}
		return returnMap;
	}
}
