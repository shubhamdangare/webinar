pipeline {
    agent any

    stages {

        stage('Compile') {
            steps {
                echo "Compiling..."
                sh "sbt compile"
            }
        }

        stage('Test') {
            steps {
                echo "Testing..."
                sh "sbt test"
            }
        }

        stage('Package') {
            steps {
                echo "Packaging..."
                sh "sbt package"
            }
        }
         stage('publish') {
              steps {
                        echo "publishing local docker image"
                        sh "sbt docker:publish"
                    }
                }

    }
    post {
        always {
            mail bcc: '', body: 'Please check the Jenkins build finished', cc: '', from: '', replyTo: '', subject: 'Jenkins demo', to: 'shubham.dangare@knoldus.in'

        }
    }
}
