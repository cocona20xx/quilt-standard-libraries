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

package org.quiltmc.qsl.resource.loader.api;

import com.google.gson.JsonElement;
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

/**
 * Resource loader for the Static Resources API.<br>
 * Unlike 'regular' resources loaded through vanilla's resource loader and {@link ResourceLoader},
 * static resources are accessible <i>as soon as a StaticResourceLoader instance exists to get them,</i>
 * and are never reloaded.
 */
public interface StaticResourceLoader {
	/**
	 * Gets the StaticResourceLoader instance for a given {@link ResourceType}.
	 * @param type the resource type for which the corresponding StaticResourceLoader was requested.
	 * @return the corresponding instance of StaticResourceLoader.
	 * @throws IllegalStateException if clientside resources are requested on the logical/dedicated server via {@link ResourceType#CLIENT_RESOURCES}.
	 */
	static StaticResourceLoader get(ResourceType type) throws IllegalStateException {
		return StaticResourceLoaderImpl.get(type);
	}

	/**
	 * Gets all static {@link Resource}s in this loader that correspond to an {@link Identifier}.
	 * @param id the identifier to get resources for.
	 * @return a list of resources corresponding to the passed id, or an empty list ({@link List#of()}) if no such resources exist.
	 */
	List<Resource> getAllResources(Identifier id);

	/**
	 * Gets the namespaces found within this loader.<br>
	 * @return the namespaces, as {@link String}s.
	 */
	Set<String> getNamespaces();

	/**
	 * Gets the {@link ResourcePack} objects loaded by this loader.
	 * @return the list of packs.
	 */
	List<ResourcePack> getPacks();

	/**
	 * Finds all resources corresponding to a
	 * @param startingPath
	 * @param pathFilter
	 * @return
	 */
	Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> pathFilter);
	Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> pathFilter);
	Stream<ResourcePack> streamPacks();
	Optional<Resource> getResource(Identifier id);
	Resource getResourceOrThrow(Identifier id) throws FileNotFoundException;
	InputStream open(Identifier id) throws IOException;
	BufferedReader openAsReader(Identifier id) throws IOException;
	Map<Identifier, JsonElement> findJsonElements(String namespace, String startingPath);

}
