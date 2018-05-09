#!groovy
node {

    properties([
      parameters([
        choice (name: 'CHOICE_ENV', choices: 'QA\nDEV\nPROD', description: 'The choice environment'),
        string (name: 'DEPLOY_ENV', defaultValue: 'TESTING', description: 'The target environment')
       ])
    ])

    echo "DEPLOY_ENV is ${params.DEPLOY_ENV}"
    echo "CHOICE_ENV is ${params.CHOICE_ENV}"

    stage('Git checkout') { // for display purposes
        git 'https://github.com/timrsfo/e2e-automation-pipeline.git'
    }
    stage('Smoke') {
        try {
            sh "mvn clean verify -Dtags='type:Smoke'"
        } catch (err) {

        } finally {
            publishHTML (target: [
                    reportDir: 'target/site/serenity',
                    reportFiles: 'index.html',
                    reportName: "Smoke tests report"
            ])
        }
    }
    stage('API') {
        try {
            sh "mvn clean verify -Dtags='type:API' -Dproperties=remote.properties"
        } catch (err) {

        } finally {
            publishHTML (target: [
                    reportDir: 'target/site/serenity',
                    reportFiles: 'index.html',
                    reportName: "API tests report"
            ])
        }
    }
    stage('UI') {
        try {
            sh "mvn clean verify -Dtags='type:UI' -Dproperties=remote.properties"
        } catch (err) {

        } finally {
            publishHTML (target: [
                    reportDir: 'target/site/serenity',
                    reportFiles: 'index.html',
                    reportName: "UI tests report"
            ])
        }
    }
    stage('Results') {
        junit '**/target/failsafe-reports/*.xml'
    }
}