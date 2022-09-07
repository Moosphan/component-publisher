package cn.dorck.publisher.test

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import com.google.common.truth.Truth.assertThat

/**
 * Unit tests for `cn.dorck.component.publisher` plugin.
 * @author Dorck
 * @since 2022/09/06
 */
class ComponentPublisherPluginTest {

    @Test
    fun `plugin registers extension test`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("cn.dorck.component.publisher")
        // Check if plugin extension registers succeed.
        assertThat(project.extensions.findByName("publishOptions")).isNotNull()
    }

    @Test
    fun `plugin registers task test`() {
        val project = ProjectBuilder.builder().build()
        // Check if plugin tasks registers succeed.
        assertThat(project.tasks.findByName("publishComponent")).isNotNull()
    }
}