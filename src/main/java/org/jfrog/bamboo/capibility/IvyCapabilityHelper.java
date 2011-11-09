package org.jfrog.bamboo.capibility;

import com.atlassian.bamboo.v2.build.agent.capability.CapabilityDefaultsHelper;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;
import org.jetbrains.annotations.NotNull;

/**
 * @author Tomer Cohen
 */
public class IvyCapabilityHelper implements CapabilityDefaultsHelper {

    @NotNull
    public CapabilitySet addDefaultCapabilities(@NotNull CapabilitySet capabilitySet) {
        return capabilitySet;
    }
}
