package org.jfrog.bamboo.release.condition;

import com.atlassian.bamboo.build.DefaultJob;
import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plugins.web.conditions.AbstractPlanPermissionCondition;
import com.atlassian.bamboo.security.acegi.acls.BambooPermission;
import com.atlassian.bamboo.task.TaskDefinition;
import org.jfrog.bamboo.context.AbstractBuildContext;

import java.util.Map;

/**
 * A condition that checks whether the release management tab should be displayed.
 *
 * @author Tomer Cohen
 */
public class UserAllowedReleaseCondition extends AbstractPlanPermissionCondition {

    private PlanManager planManager;

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }

    public boolean shouldDisplay(Map<String, Object> context) {
        String planKey = (String) context.get("planKey");
        Job job = (DefaultJob) planManager.getPlanByKey(PlanKeys.getPlanKey(planKey));
        if (job == null) {
            return false;
        }
        TaskDefinition definition = job.getBuildDefinition().getTaskDefinitions().get(0);
        AbstractBuildContext buildContext = AbstractBuildContext.createContextFromMap(definition.getConfiguration());
        if (buildContext == null) {
            return false;
        }
        return checkPlanPermission(context, BambooPermission.BUILD) &&
                buildContext.releaseManagementContext.isReleaseMgmtEnabled();
    }
}
