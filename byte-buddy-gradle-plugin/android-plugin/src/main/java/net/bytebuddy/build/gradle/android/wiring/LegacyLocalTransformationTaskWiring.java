/*
 * Copyright 2014 - Present Rafael Winterhalter
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
package net.bytebuddy.build.gradle.android.wiring;

import com.android.build.api.artifact.MultipleArtifact;
import com.android.build.api.variant.Variant;
import com.android.build.gradle.BaseExtension;
import net.bytebuddy.build.gradle.android.LegacyByteBuddyLocalClassesEnhancerTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.TaskProvider;

/**
 * Wires the local transformation task using the legacy API from AGP < 7.4.0.
 */
public class LegacyLocalTransformationTaskWiring implements LocalTransformationTaskWiring {
    /**
     * The current Gradle project.
     */
    private final Project project;

    public LegacyLocalTransformationTaskWiring(Project project) {
        this.project = project;
    }

    @Override
    public void wireTask(Variant variant, Configuration configuration, FileCollection classPath) {
        TaskProvider<LegacyByteBuddyLocalClassesEnhancerTask> localClassesTransformation = project.getTasks().register(variant.getName() + "BytebuddyLocalTransform",
                LegacyByteBuddyLocalClassesEnhancerTask.class,
                new LegacyByteBuddyLocalClassesEnhancerTask.ConfigurationAction(configuration, project.getExtensions().getByType(BaseExtension.class), classPath));

        variant.getArtifacts().use(localClassesTransformation)
                .wiredWith(LegacyByteBuddyLocalClassesEnhancerTask::getLocalClassesDirs, LegacyByteBuddyLocalClassesEnhancerTask::getOutputDir)
                .toTransform(MultipleArtifact.ALL_CLASSES_DIRS.INSTANCE);
    }
}
