package com.github.bryncooke.babelgradle

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.StopExecutionException

class BabelGradlePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        checkExists(project, 'package.json')

        def projectJson = project.file('package.json').getText('UTF-8')

        project.plugins.apply(NodePlugin.class)
        def babelTask = project.tasks.create("babel", BabelTask.class) {

        }
        project.tasks.create("install-babel-dev", NpmTask.class) {
            doFirst {
                if (projectJson.contains('babel-cli')) {
                    throw new StopExecutionException()
                }
            }
            args = ['install', 'babel-cli', '--save-dev']
        }
        def installBabelProdTask = project.tasks.create("install-babel-prod", NpmTask.class) {
            doFirst {

                if (babelTask.presets.findAll { !projectJson.contains(it) }.isEmpty()) {
                    throw new StopExecutionException()
                }
            }
        }


        babelTask.dependsOn("install-babel-dev", "install-babel-prod")
        project.afterEvaluate {

            def presets = babelTask.presets.collect { 'babel-preset-' + it }
            def npmArgs = ['install', *presets, '--save']
            installBabelProdTask.args = npmArgs
            def processResources = project.tasks.findByName('processResources')
            if (processResources != null) {
                processResources.dependsOn('babel')
            }
            if(project.hasProperty('sourceSets')) {
                project.sourceSets {
                    main {
                        resources {
                            srcDir 'build/resources/babel'
                        }
                    }
                }
            }
        }


    }


    def checkExists(Project project, String s) {
        if (!project.file(s).exists()) {
            throw new IllegalStateException("File or directory '" + s + "' not found")
        }
    }
}
