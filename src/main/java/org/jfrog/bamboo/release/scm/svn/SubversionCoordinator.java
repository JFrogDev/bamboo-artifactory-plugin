package org.jfrog.bamboo.release.scm.svn;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.repository.Repository;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import org.jfrog.bamboo.context.AbstractBuildContext;
import org.jfrog.bamboo.release.scm.AbstractScmCoordinator;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Tomer Cohen
 */
public class SubversionCoordinator extends AbstractScmCoordinator {
    private SubversionManager scmManager;
    private SVNClientManager svnClientManager;
    private final Map<String, String> configuration;
    private final PlanKey planKey;
    private boolean tagCreated;

    public SubversionCoordinator(Repository repository, Map<String, String> configuration, PlanKey planKey,
            BuildLogger buildLogger) {
        super(repository, buildLogger);
        this.configuration = configuration;
        this.planKey = planKey;
    }

    public void prepare() throws IOException {
        svnClientManager = getClientManager();
        scmManager = new SubversionManager(repository, svnClientManager, planKey, buildLogger);
    }

    public void beforeReleaseVersionChange() throws IOException {
    }

    public void afterSuccessfulReleaseVersionBuild() throws IOException {
        AbstractBuildContext context = AbstractBuildContext.createContextFromMap(configuration);
        boolean createTag = context.releaseManagementContext.isCreateVcsTag();
        if (createTag) {
            String tagUrl = context.releaseManagementContext.getTagUrl();
            String tagComment = context.releaseManagementContext.getTagComment();
            try {
                scmManager.createTag(tagUrl, tagComment);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            tagCreated = true;
        }
    }

    @Override
    public void afterDevelopmentVersionChange(boolean modified) throws IOException, InterruptedException {
        AbstractBuildContext context = AbstractBuildContext.createContextFromMap(configuration);
        super.afterDevelopmentVersionChange(modified);
        if (modified) {
            String comment = context.releaseManagementContext.getNextDevelopmentComment();
            scmManager.commitWorkingCopy(comment);
        }
    }

    public void buildCompleted(BuildContext buildContext) throws IOException, InterruptedException {
        AbstractBuildContext context = AbstractBuildContext.createContextFromMap(configuration);
        CurrentBuildResult result = buildContext.getBuildResult();
        if (!BuildState.SUCCESS.equals(result.getBuildState())) {
            // build has failed, make sure to delete the tag and revert the working copy
            scmManager.safeRevertWorkingCopy();
            if (tagCreated) {
                String tagUrl = context.releaseManagementContext.getTagUrl();
                String tagComment = context.releaseManagementContext.getTagComment();
                scmManager.safeRevertTag(tagUrl, tagComment);
            }
        }
    }

    public String getRemoteUrlForPom() {
        return scmManager.getRemoteUrl();
    }

    public boolean isGit() {
        return false;
    }

    private SVNClientManager getClientManager() {
        try {
            Method svnClientManagerMethod = repository.getClass().getDeclaredMethod("getSvnClientManager");
            svnClientManagerMethod.setAccessible(true);
            return (SVNClientManager) svnClientManagerMethod.invoke(repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
