#!/usr/bin/env groovy

node('linux') {
    stage('Checkout') {
        checkout scm
    }
    stage('Build') {
        String mvntool = tool name: "mvn", type: 'hudson.tasks.Maven$MavenInstallation'
        String jdktool = tool name: "jdk8", type: 'hudson.model.JDK'

        List mvnEnv = ["PATH+MVN=${mvntool}/bin", "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}", "MAVEN_HOME=${mvntool}"]

        withEnv(mvnEnv) {
            sh 'mvn -Dmaven.test.failure.ignore=true --batch-mode --quiet package'
        }
    }
}
