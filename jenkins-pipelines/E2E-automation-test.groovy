#!groovy
node {

//    properties([
//      parameters([
//        choice(name: 'DEPLOY_ENV', choices: ["QA\nDEV\nPROD"], description: 'The target environment')
//      ])
//   ])

    properties([
      parameters([
        string(name: 'DEPLOY_ENV', defaultValue: 'TESTING', description: 'The target environment', )
       ])
    ])

    echo "DEPLOY_ENV is ${params.DEPLOY_ENV}"

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
            sh "mvn clean verify -Dtags='type:API'"
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
            sh "mvn clean verify -Dtags='type:UI'"
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