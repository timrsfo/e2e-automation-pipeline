#!groovy
node {
 // Define the docker container variables to stop them at the end of the pipeline
    def sel_hub
    def sel_chrome
    def sel_firefox

    properties([
      parameters([
        choice (name: 'CHOICE_ENV', choices: 'QA\nDEV\nPROD', description: 'The choice environment'),
        string (name: 'DEPLOY_ENV', defaultValue: 'TESTING', description: 'The target environment')
       ])
    ])

    echo "DEPLOY_ENV is ${params.DEPLOY_ENV}"
    echo "CHOICE_ENV is ${params.CHOICE_ENV}"
    
      try{
         
      // In the preparation phase the three containers are run to parallelize the execution of chrome and firefox testing
        stage('Preparation') {
            sel_hub = docker.image('selenium/hub:3.4.0').run('-p 4444:4444 --name se-grid-hub')
            sel_chrome = docker.image('selenium/node-chrome:3.4.0').run('-p 5901:5900 --link se-grid-hub:hub')
            sel_firefox = docker.image('selenium/node-firefox:3.4.0').run('-p 5902:5900 --link se-grid-hub:hub')
        } 

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
      } finally {
        sel_hub.stop()
        sel_firefox.stop()
        sel_chrome.stop()
      }

}