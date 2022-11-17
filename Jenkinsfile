pipeline {
    agent any
    environment {
        imagename = "onezo/god-life-apigateway-service"
        registryCredential = 'jenkins-docker'
        dockerImage = ''
    }
    stages {
        stage('git clone') {
            steps {
                echo 'Clonning Repository'
                git url: 'git@github.com:live-god-life/god-life-apigateway-service.git',
                branch: 'release',
                credentialsId: 'jenkins-git'
            }
            post {
                success {
                    echo 'Successfully Cloned Repository'
                }
                failure {
                    echo 'Git Clone fail'
                }
            }
        }

        stage('Build docker') {
            steps {
                echo 'Build Docker'
                script {
                    dockerImage = docker.build imagename
                }
            }
            post {
                success {
                    echo 'Successfully Docker Build'
                }
                failure {
                    echo 'Docker Build fail'
                }
            }
        }

        stage('Push Docker') {
            steps {
                echo 'Push Docker'
                script {
                    docker.withRegistry( '', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
            post {
                success {
                    echo 'Successfully Docker Push'
                }
                failure {
                    echo 'Docker Push fail'
                }
            }
        }

        stage('Run Docker Deploy Script') {
            steps {
                echo 'Pull Docker Image & Docker Image Run'
                sh "ssh -o StrictHostKeyChecking=no root@106.10.40.232 -p 2222 '/root/DeployScript/apigateway-service-deploy.sh'"
            }
            post {
                success {
                    echo 'Successfully Run Docker Deploy Script'
                }
                failure {
                    echo 'Docker Run fail'
                }
            }
        }
    }
}