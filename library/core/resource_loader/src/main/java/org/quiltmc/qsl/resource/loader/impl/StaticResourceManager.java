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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resource.*;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.metadata.ResourceFilterMetadata;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.resource.loader.mixin.MultiPackResourceManagerMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Basically a glorified re-implementation of {@link MultiPackResourceManager} to avoid mixins to said class effecting static data loading.
 */
@ApiStatus.Internal
class StaticResourceManager implements AutoCloseableResourceManager {

	public static final Logger LOGGER = LoggerFactory.getLogger("QuiltLazyStaticResourceManagerImpl");
	private final ResourceType type;
	private final LinkedHashMap<String, NamespaceResourceManager> namespaceManagers = new LinkedHashMap<>();
	private final ArrayList<ResourcePack> resourcePacks = new ArrayList<>();
	public StaticResourceManager(ResourceType type, List<ResourcePack> packs){
		this.type = type;
		this.resourcePacks.addAll(List.copyOf(packs));
		computeNamespaces();
	}

	@Override
	public Set<String> getAllNamespaces() {
		return ImmutableSet.copyOf(namespaceManagers.keySet());
	}

	public List<ResourcePack> getPacks(){
		return ImmutableList.copyOf(resourcePacks);
	}

	@Override
	public List<Resource> getAllResources(Identifier id) {
		ResourceManager resourceManager = this.namespaceManagers.get(id.getNamespace());
		return resourceManager != null ? resourceManager.getAllResources(id) : List.of();
	}

	@Override
	public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> pathFilter) {
		checkTrailingDirectoryPath(startingPath);
		Map<Identifier, Resource> map = new TreeMap<>();

		for(NamespaceResourceManager namespaceResourceManager : this.namespaceManagers.values()) {
			map.putAll(namespaceResourceManager.findResources(startingPath, pathFilter));
		}

		return map;
	}
	@Override
	public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> pathFilter) {
		checkTrailingDirectoryPath(startingPath);
		Map<Identifier, List<Resource>> map = new TreeMap<>();

		for(NamespaceResourceManager namespaceResourceManager : this.namespaceManagers.values()) {
			map.putAll(namespaceResourceManager.findAllResources(startingPath, pathFilter));
		}
		return map;
	}

	@Override
	public Stream<ResourcePack> streamResourcePacks() {
		return this.resourcePacks.stream();
	}

	@Override
	public Optional<Resource> getResource(Identifier identifier) {
		ResourceManager resourceManager = this.namespaceManagers.get(identifier.getNamespace());
		return resourceManager != null ? resourceManager.getResource(identifier) : Optional.empty();
	}

	@Override
	public Resource getResourceOrThrow(Identifier identifier) throws FileNotFoundException {
		Optional<Resource> getResourceResult = getResource(identifier);
		if(getResourceResult.isEmpty()) throw new FileNotFoundException("No resource found at id " + identifier);
		else return getResourceResult.get();
	}

	@Override
	public InputStream open(Identifier identifier) throws IOException {
		return AutoCloseableResourceManager.super.open(identifier);
	}

	@Override
	public BufferedReader openAsReader(Identifier identifier) throws IOException {
		return AutoCloseableResourceManager.super.openAsReader(identifier);
	}

	@Override
	public void close() {
		this.resourcePacks.forEach(ResourcePack::close);
	}

	/**
	 * Based heavily on {@link MultiPackResourceManagerMixin#quilt$recomputeNamespaces()}
	 */
	private void computeNamespaces(){
		this.namespaceManagers.clear();
		List<String> namespaces = resourcePacks.stream().flatMap(resourcePack -> resourcePack.getNamespaces(this.type).stream()).distinct().toList();
		for (ResourcePack pack : resourcePacks) {
			ResourceFilterMetadata filterMetadata = getFilter(pack);
			Predicate<Identifier> pathFilterPredicate = filterMetadata != null ? id -> filterMetadata.matchPath(id.getPath()) : null;
			Set<String> namespaceSet = pack.getNamespaces(type);

			for (String namespace : namespaces) {
				boolean hasNamespace = namespaceSet.contains(namespace);
				boolean namespaceFiltered = filterMetadata != null && filterMetadata.matchNamespace(namespace);
				if (hasNamespace || namespaceFiltered) {
					NamespaceResourceManager namespaceResourceManager = namespaceManagers.computeIfAbsent(namespace,
						n -> new NamespaceResourceManager(type, n));
					if (hasNamespace && namespaceFiltered) {
						namespaceResourceManager.addPack(pack, pathFilterPredicate);
					} else if (hasNamespace) {
						namespaceResourceManager.addPack(pack);
					} else {
						namespaceResourceManager.addPack(pack.getName(), pathFilterPredicate);
					}
				}
			}
		}
	}

	/**
	 * Basically copy-pasted 1-to-1 from {@link MultiPackResourceManager}'s private getFilter method.<br>
	 * Cannot be inlined due to a lambda expression.
	 */
	@Nullable
	private ResourceFilterMetadata getFilter(ResourcePack pack) {
		try {
			return pack.parseMetadata(ResourceFilterMetadata.TYPE);
		} catch (IOException exception) {
			LOGGER.error("Failed to get filter section from pack {}", pack.getName());
			return null;
		}
	}

	/**
	 * Copy-pasted 1-to-1 from {@link MultiPackResourceManager}.
	 */
	private static void checkTrailingDirectoryPath(String path) {
		if (path.endsWith("/")) {
			throw new IllegalArgumentException("Trailing slash in path " + path);
		}
	}
}
