node {
    def sbtHome = "${tool 'Sbt-0.13.15'}"
    env.sbt= "${sbtHome}/bin/sbt -no-colors -batch -mem 4096"

    def isTag

    stage('Clone repository') {
        checkout scm
        isTag = sh(returnStdout: true, script: "git tag --contains | head -1").trim()
    }

    stage('Running tests') {
        sh '${sbt} clean test'
    }

    if(env.BRANCH_NAME == 'develop' || env.BRANCH_NAME.startsWith('branch-') || isTag){
      stage('Publish artifacts') {
        sh '${sbt} +publish'
      }
    }
}