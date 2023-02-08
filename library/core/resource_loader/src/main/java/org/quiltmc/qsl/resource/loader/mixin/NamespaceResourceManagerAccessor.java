/*
 * Copyright 2021-2023 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.ResourceMetadata;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

@Mixin(NamespaceResourceManager.class)
public interface NamespaceResourceManagerAccessor {
	@Accessor
	ResourceType getType();

	@Invoker
	static ResourceMetadata invokeGetResourceMetadataFromInputStream(ResourceIoSupplier<InputStream> readSupplier) throws IOException {
		throw new IllegalStateException("Invoker injection failed.");
	}

	@Invoker
	static Identifier invokeGetMetadataPath(Identifier id) {
		throw new IllegalStateException("Invoker injection failed.");
	}
}
