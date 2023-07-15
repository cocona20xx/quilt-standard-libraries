package org.quiltmc.qsl.resource.loader.impl;

import net.minecraft.resource.MultiPackResourceManager;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wrapper/duck interface for {@link MultiPackResourceManager} instances.
 * @see StaticResourceManager
 */
@ApiStatus.Internal
public interface StaticResourceManagerWrapper {
	/**
	 * Casts the provided object and runs {@link StaticResourceManagerWrapper#quilt$isStaticManager()}
	 * @param managerToWrap The object to wrap
	 * @return False by default, true if the object is an instance of {@link StaticResourceManager}
	 */
	static boolean quilt$wrapAndCheck(Object managerToWrap){
		return ((StaticResourceManagerWrapper) managerToWrap).quilt$isStaticManager();
	}

	/**
	 * Method called by {@link StaticResourceManagerWrapper#quilt$wrapAndCheck(Object)} to check if a manager is used in static loading from within mixins.<br>
	 * @return Whether the duck interfaced/wrapped manager is for static resources. Defaults to false.
	 */
	default boolean quilt$isStaticManager() {
		return false;
	}
}
