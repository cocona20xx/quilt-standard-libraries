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
