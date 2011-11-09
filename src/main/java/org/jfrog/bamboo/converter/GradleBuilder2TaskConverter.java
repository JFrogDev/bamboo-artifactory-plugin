package org.jfrog.bamboo.converter;

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskDefinitionImpl;
import com.atlassian.bamboo.task.conversion.AbstractBuilder2TaskConverter;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jfrog.bamboo.util.PluginUtils;

import java.util.List;

/**
 * @author Tomer Cohen
 */
public class GradleBuilder2TaskConverter extends AbstractBuilder2TaskConverter {
    private static final Logger log = Logger.getLogger(GradleBuilder2TaskConverter.class);
    private static final String KEY = PluginUtils.getPluginDescriptorKey() + ":artifactoryGradleTask";

    @NotNull
    public List<TaskDefinition> builder2TaskList(@NotNull BuildConfiguration buildConfiguration) {
        log.info("Converting Gradle builder: " + buildConfiguration + " to tasks");
        List<TaskDefinition> result = Lists.newArrayList();
        result.add(
                new TaskDefinitionImpl(incrementTaskId.get(), KEY, "", stripBuilderParameters(buildConfiguration, "")));
        return result;
    }
}
