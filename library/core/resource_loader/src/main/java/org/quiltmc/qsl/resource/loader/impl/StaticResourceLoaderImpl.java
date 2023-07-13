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
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.resource.loader.api.StaticResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApiStatus.Internal
public class StaticResourceLoaderImpl implements StaticResourceLoader {

	private static StaticResourceLoaderImpl resourcesInstance = null;
	private static StaticResourceLoaderImpl dataInstance = null;
	private static final Logger LOGGER = LoggerFactory.getLogger("QuiltStaticResourceLoaderImpl");
	private static final Gson GSON = new Gson();
	private static final String DATA_PATH_HEAD = "static/data";
	private static final String RESOURCES_PATH_HEAD = "static/assets";
	private final StaticResourceManager manager;

	public static StaticResourceLoaderImpl get(ResourceType type) throws IllegalStateException {
		if(type.equals(ResourceType.CLIENT_RESOURCES)) {
			if(!MinecraftQuiltLoader.getEnvironmentType().equals(EnvType.CLIENT)) throw new IllegalStateException("Static resources in 'static/resources' are client-only.");
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
		if (type.equals(ResourceType.CLIENT_RESOURCES)) {
			pathHead = RESOURCES_PATH_HEAD;
		} else if (type.equals(ResourceType.SERVER_DATA)) {
			pathHead = DATA_PATH_HEAD;
		} else throw new RuntimeException("Invalid resource type passed when loading static packs.");
		Path gameDirPath = QuiltLoader.getGameDir();
		Path gameDirResPath = gameDirPath.resolve(pathHead);
		File gameDirResPathFile = gameDirResPath.toFile();
		List<ResourcePack> returnList = new ArrayList<>(getUserPacks(gameDirResPathFile));
		returnList.addAll(getUserPacks(gameDirPath.resolve("resources").resolve(pathHead).toFile()));
		for(ModContainer container : QuiltLoader.getAllMods()) {
			returnList.addAll(getPacksFromMod(container, pathHead));
		}
		return returnList;
	}

	private static List<ResourcePack> getUserPacks(File directoryFile) {
		List<ResourcePack> returnList = new ArrayList<>();
		File[] potentialPackFiles = directoryFile.listFiles(filterPath -> {
			//path should be 1 longer than root path if it is a pack
			return filterPath.toPath().getNameCount() - directoryFile.toPath().getNameCount() == 1;
		});
		if(Objects.isNull(potentialPackFiles)) {
			//if we're in the actual runtime directory, create static resource dirs if they don't yet exist
			if(directoryFile.toPath().equals(QuiltLoader.getGameDir().resolve(RESOURCES_PATH_HEAD))) {
				QuiltLoader.getGameDir().resolve(RESOURCES_PATH_HEAD).toFile().mkdirs();
			} else if (directoryFile.toPath().equals(QuiltLoader.getGameDir().resolve(DATA_PATH_HEAD))) {
				QuiltLoader.getGameDir().resolve(DATA_PATH_HEAD).toFile().mkdirs();
			}
		} else {
			for (File fi : potentialPackFiles) {
				if (fi.isFile()) {
					if (fi.toPath().toString().endsWith(".zip"))
						returnList.add(new ZipResourcePack(fi.getName(), fi, false));
					else
						LOGGER.error("Files outside of packs are not supported by the Quilt Static Resource Loader. Loose file: {}", fi);
				} else returnList.add(new NioResourcePack(fi.getName(), fi.toPath(), false));
			}
		}
		return returnList;
	}

	private static List<ResourcePack> getPacksFromMod(ModContainer modContainer, String pathHead) {
		Path resolvedPath = modContainer.rootPath().resolve(pathHead);
		List<ResourcePack> returnList = new ArrayList<>();
		try(Stream<Path> pathStream = Files.list(resolvedPath)) {
			Iterator<Path> iterator = pathStream.iterator();
			iterator.forEachRemaining(path -> {
				if(path.getNameCount() - resolvedPath.getNameCount() == 1) {
					if(Files.isDirectory(path)) {
						try {
							returnList.add(new NioResourcePack(path.getName(path.getNameCount() - 1).toString(), path, false));
						} catch (Exception e){
							LOGGER.error(e.toString());
						}
					} else if (Files.isRegularFile(path) && path.endsWith(".zip")) {
						LOGGER.error(".zip packs are not supported for static packs within mod directories. Zip pack found at {}", path);
					}
				}
			});
		} catch (Exception e) {
			// do nothing
		}
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
	public Map<Identifier, JsonElement> findJsonElements(@Nullable String namespace, @Nullable String startingPath) {
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
				LOGGER.warn("Exception caught during static json loading: {}", e.getMessage());
			}
		}
		return returnMap;
	}

}
