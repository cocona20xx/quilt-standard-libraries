/*
 * Copyright 2022 The Quilt Project
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

import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.qsl.resource.loader.api.ResourceLoader;

/**
 * Internal resource loader hooks into {@link net.minecraft.resource.MultiPackResourceManager}.
 */
@ApiStatus.Internal
public interface QuiltMultiPackResourceManagerHooks {
	/**
	 * Appends the top resource packs that have been registered from {@link ResourceLoader#getRegisterTopPackEvent()}.
	 */
	void quilt$appendTopPacks();

	/**
	 * Makes the multi-pack resource manager recomputes the discovered namespaces when the set of resource packs change.
	 */
	void quilt$recomputeNamespaces();
}
