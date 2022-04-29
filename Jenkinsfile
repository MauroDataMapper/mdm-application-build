pipeline {
    agent any

    environment {
        JENKINS = 'true'
    }

    tools {
        jdk 'jdk-17'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        skipStagesAfterUnstable()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    stages {

        stage('Clean') {
            // Only clean when the last build failed
            when {
                expression {
                    currentBuild.previousBuild?.currentResult == 'FAILURE'
                }
            }
            steps {
                sh "./gradlew clean"
            }
        }

        stage('Info') {
            steps {
                sh './gradlew -v' // Output gradle version for verification checks
                sh './grailsw -v' // Output grails version for verification checks
            }
        }

        stage('Test cleanup & Compile') {
            steps {
                sh './gradlew compile'
            }
        }

        stage('License Header Check') {
            steps {
                warnError('Missing License Headers') {
                    sh './gradlew --build-cache license'
                }
            }
        }

        stage('Sonarqube') {
            when {
                branch 'main'
            }
            steps {
                withSonarQubeEnv('JenkinsQube') {
                    sh "./gradlew sonarqube"
                }
            }
        }

//        stage('Deploy to Artifactory') {
//            when {
//                allOf {
//                    anyOf {
//                       branch 'main'
//                        branch 'develop'
//                    }
//                    expression {
//                        currentBuild.currentResult == 'SUCCESS'
//                    }
//                }
//
//            }
//            steps {
//                script {
//                    sh "./gradlew --build-cache publish"
//                }
//            }
//        }
    }

    post {
        always {
            zulipNotification(topic: 'mdm-application-build')
        }
    }
}