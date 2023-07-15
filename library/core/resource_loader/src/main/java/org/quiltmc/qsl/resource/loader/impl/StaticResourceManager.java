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


import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;


/**
 * Exists solely for determining whether a manager is involved in static resource loading or regular resource loading.<br>
 * This is used to avoid activating certain mixins when they are not necessary.
 */
@ApiStatus.Internal
public class StaticResourceManager extends MultiPackResourceManager implements StaticResourceManagerWrapper{
	public StaticResourceManager(ResourceType type, List<ResourcePack> packs) {
		super(type, packs);
	}

	@Override
	public boolean quilt$isStaticManager() {
		return true;
	}
}
