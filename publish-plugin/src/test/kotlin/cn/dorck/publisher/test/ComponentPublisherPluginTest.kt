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

    fun `plugin registers test`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("cn.dorck.component.publisher")
        // Check if plugin extension and tasks registers succeed.
        assertThat(project.extensions.findByName("publishOptions")).isNotNull()
        assertThat(project.tasks.findByName("publishComponent")).isNotNull()
    }

    @Test
    fun `check for plugin extension`() {
        val project = ProjectBuilder.builder().build()

    }
}