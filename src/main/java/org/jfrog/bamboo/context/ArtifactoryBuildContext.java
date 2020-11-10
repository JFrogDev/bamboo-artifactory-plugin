package org.jfrog.bamboo.context;

import com.atlassian.bamboo.v2.build.BuildContext;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.build.api.util.Log;

import java.util.Map;

import static org.jfrog.bamboo.configuration.AbstractArtifactoryConfiguration.*;
import static org.jfrog.bamboo.security.provider.SharedCredentialsDataProvider.*;

/**
 * Created by Bar Belity on 08/10/2020.
 */
public abstract class ArtifactoryBuildContext {
    public static final String RESOLVER_OVERRIDE_CREDENTIALS_CHOICE = "resolver.overrideCredentialsChoice";
    public static final String DEPLOYER_OVERRIDE_CREDENTIALS_CHOICE = "deployer.overrideCredentialsChoice";
    public static final String RESOLVER_SHARED_CREDENTIALS = "resolver.sharedCredentials";
    public static final String DEPLOYER_SHARED_CREDENTIALS = "deployer.sharedCredentials";
    public static final String SERVER_ID_PARAM = "artifactoryServerId";
    public static final String USERNAME_PARAM = "username";
    public static final String PASSWORD_PARAM = "password";
    public static final String BUILD_NAME = "artifactory.task.buildName";
    public static final String BUILD_NUMBER = "artifactory.task.buildNumber";
    public static final String DEFAULT_BUILD_NAME = "${bamboo.buildPlanName}";
    public static final String DEFAULT_BUILD_NUMBER = "${bamboo.buildNumber}";

    protected final Map<String, String> env;
    protected String prefix;

    public ArtifactoryBuildContext(Map<String, String> env) {
        this.env = env;
    }

    public ArtifactoryBuildContext(String credentialsPrefix, Map<String, String> env) {
        this(env);
        this.prefix = credentialsPrefix;
    }

    public long getArtifactoryServerId() {
        String serverId = env.get(prefix + SERVER_ID_PARAM);
        if (org.apache.commons.lang.StringUtils.isBlank(serverId)) {
            return -1;
        }
        return Long.parseLong(serverId);
    }

    public String getResolverOverrideCredentialsChoice() {
        return env.get(RESOLVER_OVERRIDE_CREDENTIALS_CHOICE);
    }

    public String getDeployerOverrideCredentialsChoice() {
        return env.get(DEPLOYER_OVERRIDE_CREDENTIALS_CHOICE);
    }

    public String getResolverSharedCredentials() {
        return env.get(RESOLVER_SHARED_CREDENTIALS);
    }

    public String getDeployerSharedCredentials() {
        return env.get(DEPLOYER_SHARED_CREDENTIALS);
    }

    public String getUsername() {
        return env.get(prefix + USERNAME_PARAM);
    }

    public String getPassword() {
        return env.get(prefix + PASSWORD_PARAM);
    }

    public String getResolverUsername() {
        return getUsername();
    }

    public String getDeployerUsername() {
        return getUsername();
    }

    public String getResolverPassword() {
        return getPassword();
    }

    public String getDeployerPassword() {
        return getPassword();
    }

    /**
     * Get build name from context.
     * Empty build-name in context occurs when running a task which was configured prior to allowing customizable build name.
     * In this case, return the plan-name generated by Bamboo to allow backward compatibility.
     */
    public String getBuildName(BuildContext buildContext) {
        String buildName = env.get(BUILD_NAME);
        if (StringUtils.isNotBlank(buildName)) {
            return buildName;
        }
        // Extract backward compatible build-name.
        return buildContext.getPlanName();
    }

    /**
     * Get build number from context.
     * Empty build name in context occurs when running a task which was configured prior to allowing customizable build number.
     * In this case, return the build-number generated by Bamboo to allow backward compatibility.
     */
    public String getBuildNumber(BuildContext buildContext) {
        String buildNumber = env.get(BUILD_NUMBER);
        if (StringUtils.isNotBlank(buildNumber)) {
            return buildNumber;
        }
        // Extract backward compatible build-number.
        return String.valueOf(buildContext.getBuildNumber());
    }

    public String getOverriddenUsername(Map<String, String> runtimeTaskContext, Log log, boolean deployer) {
        switch (StringUtils.defaultString(deployer ? getDeployerOverrideCredentialsChoice() : getResolverOverrideCredentialsChoice())) {
            case CVG_CRED_NO_OVERRIDE:
                return "";
            case CVG_CRED_SHARED_CREDENTIALS:
                String username = runtimeTaskContext.get(deployer ? DEPLOYER_SHARED_CREDENTIALS_USER : RESOLVER_SHARED_CREDENTIALS_USER);
                String credentialsId = deployer ? getDeployerSharedCredentials() : getResolverSharedCredentials();
                log.info("Using Artifactory username '" + username + "' configured in credentials ID '" + credentialsId + "'");
                return username;
            case CVG_CRED_USERNAME_PASSWORD:
                username = deployer ? getDeployerUsername() : getResolverUsername();
                log.info("Using Artifactory username '" + username + "' configured in job");
                return username;
            default:
                // Backward compatibility
                username = deployer ? getDeployerUsername() : getResolverUsername();
                if (StringUtils.isNotBlank(username)) {
                    log.info("Using Artifactory username '" + username + "' configured in job");
                    return username;
                }
                return "";
        }
    }

    public String getOverriddenPassword(Map<String, String> runtimeTaskContext, Log log, boolean deployer) {
        switch (StringUtils.defaultString(deployer ? getDeployerOverrideCredentialsChoice() : getResolverOverrideCredentialsChoice())) {
            case CVG_CRED_NO_OVERRIDE:
                return "";
            case CVG_CRED_SHARED_CREDENTIALS:
                String credentialsId = deployer ? getDeployerSharedCredentials() : getResolverSharedCredentials();
                log.info("Using Artifactory password configured in credentials ID '" + credentialsId + "'");
                return runtimeTaskContext.get(deployer ? DEPLOYER_SHARED_CREDENTIALS_PASSWORD : RESOLVER_SHARED_CREDENTIALS_PASSWORD);
            case CVG_CRED_USERNAME_PASSWORD:
                log.info("Using Artifactory password configured in job");
                return deployer ? getDeployerPassword() : getResolverPassword();
            default:
                // Backward compatibility
                String password = deployer ? getDeployerPassword() : getResolverPassword();
                if (StringUtils.isNotBlank(password)) {
                    log.info("Using Artifactory password configured in job");
                    return password;
                }
                return "";
        }
    }
}