//  Original maven command for Jenkins build
//
//  clean deploy -Dsencha.ext.dir=copl/com.elster.jupiter.extjs/src/main/web/js/ext -Psencha-build,enforce-version
//
def MAVEN_REPO = "/home2/src/maven/repository"
def MIRROR_CLONE = "/home2/src/maven/mirror"
def MAXIMUM_COVERITY_ISSUES = 402
SENCHA_4 = "/home2/tools/y/Sencha/Cmd/4.0.5.87"

pipeline {
  agent {
    label 'linux && java'
  }
  parameters {
    string(
      defaultValue: '',
      description: 'Version number for release',
      name: 'releaseVersion'
    )
    booleanParam(
      defaultValue: true,
      description: 'Run unit tests',
      name: 'runTests'
    )
    booleanParam(
      defaultValue: isRelease(),
      description: 'Deploy the artifacts',
      name: 'doDeploy'
    )
    booleanParam(
      defaultValue: shouldRunAnalysis(),
      description: 'Run Coverity and Black Duck',
      name: 'runAnalysis'
    )
  }
  triggers {
    pollSCM('H/1 * * * *')  // Poll every minute
  }
  options {
    skipDefaultCheckout()
    buildDiscarder(logRotator(
                     artifactDaysToKeepStr: '',
                     artifactNumToKeepStr: '',
                     daysToKeepStr: '',
                     numToKeepStr: '5')
    )
  }
  stages {
    stage("Checkout") {
      steps {
        deleteDir()
        checkout([$class: 'GitSCM',
                  branches: scm.branches,
                  extensions: [[$class: 'CloneOption',
                  depth: 2,
                  noTags: false,
                  reference: MIRROR_CLONE, shallow: true]],
                  userRemoteConfigs: scm.userRemoteConfigs])
        script {
          DIRECTORIES = getBuildDirectories()
        }
      }
    }
    stage("Get POM") {
      environment {
        POM_VERSION = getPomVersion()
      }
      steps {
        echo "Detected pom version is '$POM_VERSION'"
        script {
          POM_VERSION = "$POM_VERSION"
        }
      }
    }
    stage("Set Version") {
      when {
        expression { return getBranchVersion().length() > 0 }
      }
      environment {
        NEW_VERSION = getBranchVersion()
      }
      steps {
        withMaven(maven: 'Maven 3.6.3',
            mavenSettingsConfig: 'ehc-mirror',
            publisherStrategy: 'EXPLICIT',
            options: [],
            mavenLocalRepo: MAVEN_REPO) {
          runMaven("clean versions:set -DnewVersion=$env.NEW_VERSION")
        }
      }
    }
    stage('Build') {
      environment {
        COMMAND = mavenCommand()
        EXTRA_PARAMS = getMavenExtras()
        SENCHA = "-Dsencha.ext.dir=$env.WORKSPACE/copl/com.elster.jupiter.extjs/src/main/web/js/ext"
        PROFILES = '-Psencha-build,coverage'
        DIRECTORIES = "$DIRECTORIES"
      }
      steps {
        catchError(buildResult: 'SUCCESS', message: 'WARNING: Could not initialize sencha package repo', stageResult: 'SUCCESS') {
          sh 'echo \$PATH or $SENCHA_4'
          sh "${SENCHA_4}/sencha package repo init -name 'Elster Jupiter Project' -email 'Jupiter-Core@elster.com'"
        }
        lock(resource: "$env.JOB_NAME$env.BRANCH_NAME", inversePrecedence: true) {
          withMaven(maven: 'Maven 3.6.3',
              mavenSettingsConfig: 'ehc-mirror',
              mavenOpts: '-Xmx5g',
              publisherStrategy: 'EXPLICIT',
              options: [openTasksPublisher()],
              mavenLocalRepo: MAVEN_REPO) {
            catchError(buildResult: 'FAILURE', message: 'FAILURE: Maven build did not complete properly', stageResult: 'UNSTABLE') {
              runMaven("$env.COMMAND $env.DIRECTORIES $env.EXTRA_PARAMS $env.SENCHA $env.PROFILES")
            }
            //  These stashes are really too large. Need to find another way to do this...
            stash name:"java_reports", allowEmpty: true, includes: "**/target/surefire-reports/TEST*.xml,**/target/jacoco.exec"
            stash name:"java_classes", allowEmpty: true, includes: "**/target/**/classes/**"
            stash name:"zip_files", allowEmpty: true, includes: "**/*.zip"
          }
        }
      }
    }
    stage('results') {
      parallel {
        stage('Coverity') {
          agent {
            label 'coverity'
          }
          when {
            expression { params.runAnalysis }
          }
          environment {
            STREAM = getCoverityStream()
            TARGET_DIR = "target"
            COVERITY_TOOL_HOME = "$COVERITY_TOOL_HOME"
            MAXIMUM_COVERITY_ISSUES = "$MAXIMUM_COVERITY_ISSUES"
          }
          steps {
            checkout([$class: 'GitSCM',
                      poll: false,
                      changelog: false,
                      branches: scm.branches,
                      extensions: [[$class: 'CloneOption',
                      depth: 2,
                      noTags: false,
                      reference: MIRROR_CLONE, shallow: true]],
                      userRemoteConfigs: scm.userRemoteConfigs])
            unstash "java_classes"
            lock(resource: "Coverity", inversePrecedence: false) {
              runCoverity("$MAXIMUM_COVERITY_ISSUES".toInteger())
            }
          }
        }
        stage('Analysis') {
          when {
            expression { params.runTests }
          }
          stages {
            stage('Static') {
              environment {
                DIRECTORIES = "$DIRECTORIES"
                EXTRA_PARAMS = getMavenExtras()
              }
              steps {
                checkout([$class: 'GitSCM',
                          branches: scm.branches,
                          poll: false,
                          changelog: false,
                          extensions: [[$class: 'CloneOption',
                          depth: 2,
                          noTags: false,
                          reference: MIRROR_CLONE, shallow: true]],
                          userRemoteConfigs: scm.userRemoteConfigs])
                withMaven(maven: 'Maven 3.6.3',
                          mavenSettingsConfig: 'ehc-mirror',
                          mavenOpts: '-Xmx5g',
                          publisherStrategy: 'EXPLICIT',
                          options: [],
                          mavenLocalRepo: MAVEN_REPO) {
                  catchError(buildResult: 'UNSTABLE',
                             message: 'UNSTABLE: Maven static analysis failed',
                             stageResult: 'UNSTABLE') {
                    // Clean out specific directory that has issues
                    runMaven("-pl coko/com.elster.jupiter.calendar clean")
                    // Static code analysis
                    runMaven("compile spotbugs:spotbugs pmd:pmd checkstyle:checkstyle -DskipTests=true -P'!enforce-version' $env.EXTRA_PARAMS $env.DIRECTORIES")
                  }
                  stash name:"bug_reports",
                        allowEmpty: true,
                        includes: "**/spotbugsXml.xml,**/pmd.xml,**/checkstyle-result.xml"
                }
              }
            }
            stage('Reports') {
              steps {
                echo "Collecting Java JUnit and static analysis reports"
                unstash "java_reports"
                unstash "bug_reports"
                unstash "java_classes"
                recordIssues aggregatingResults: true,
                             enabledForFailure: true,
                             qualityGates: [[threshold: 4, type: 'TOTAL_ERROR', unstable: false],
                                            [threshold: 97, type: 'TOTAL_HIGH', unstable: true],
                                            [threshold: 21900, type: 'TOTAL_NORMAL', unstable: true]],
                             tools: [junitParser(pattern: '**/Test-*.xml'),
                                     pmdParser(),
                                     checkStyle(),
                                     spotBugs(useRankAsPriority: true)]
                junit allowEmptyResults: true, healthScaleFactor: 100.0, testResults: '**/TEST-*.xml'
                jacoco buildOverBuild: false,
                       changeBuildStatus: true,
                       exclusionPattern: '**/*Test*.class',
                       // Must exceed these values or the build will be unstable
                       maximumBranchCoverage: '14',
                       maximumClassCoverage: '42',
                       maximumComplexityCoverage: '21',
                       maximumLineCoverage: '26',
                       maximumMethodCoverage: '28',
                       // Must exceed these values or the build will fail
                       minimumBranchCoverage: '14',
                       minimumClassCoverage: '20',
                       minimumComplexityCoverage: '20',
                       minimumLineCoverage: '20',
                       minimumMethodCoverage: '20'
              }
            }
          }
        }
        stage("Archive") {
          stages {
            stage("Jenkins") {
              steps {
                archiveArtifacts allowEmptyArchive: true,
                                 artifacts: '**/connexo-extra-jars*.zip,**/connexo-insight*.zip,**/connexo-kore*.zip,**/multisense*.zip',
                                 fingerprint: false,
                                 followSymlinks: false
              }
            }
            stage('Artifactory') {
              when {
                expression { params.doDeploy }
              }
              environment {
                FOLDER = "connexo-maven-unstable-local/$env.BRANCH_NAME/$env.BUILD_NUMBER"
                ARTIFACT_VERSION = getArtifactTag("$POM_VERSION")
              }
              steps {
                echo "Sending artifacts version $ARTIFACT_VERSION to Artifactory to $FOLDER"
                unstash "zip_files"
                rtUpload (
                  serverId: "Artifactory",
                  spec: '''{
                    "files": [{
                      "pattern": "**/connexo-extra-jars*.zip",
                      "target": "${FOLDER}/connexo-extra-jars-${ARTIFACT_VERSION}.zip",
                      "flat": true
                    },{
                      "pattern": "**/connexo-insight*.zip",
                      "target": "${FOLDER}/connexo-insight-${ARTIFACT_VERSION}.zip",
                      "flat": true
                    },{
                      "pattern": "**/connexo-kore*.zip",
                      "target": "${FOLDER}/connexo-kore-${ARTIFACT_VERSION}.zip",
                      "flat": true
                    },{
                      "pattern": "**/multisense*.zip",
                      "target": "${FOLDER}/multisense-${ARTIFACT_VERSION}.zip",
                      "flat": true
                  }]}'''
                )
              }
            }
          }
        }
      }
    }
  }
  post {
    failure {
      step([$class: 'Mailer',
            notifyEveryUnstableBuild: false,
            recipients: emailextrecipients([culprits(), requestor()])]
      )
    }
  }
}

def isRelease() {
  echo "Release: " + env.BRANCH_NAME.startsWith('release')
  return env.BRANCH_NAME.startsWith('release')
}

def shouldRunAnalysis() {
  if (isRelease()) {
    return true
  }
  if (env.BRANCH_NAME.startsWith('develop')) {
    return true
  }
  if (env.BRANCH_NAME.startsWith('master')) {
    return true
  }
  return false
}

def mavenCommand() {
  if (params.doDeploy) {
    return "deploy"
  }
  return "install -DobrRepository"
}

def getMavenExtras() {
  return getRunTestString() + getBuildConfiguration()
}

def getRunTestString() {
  if (params.runTests) {
    return "";
  }
  return " -DskipTests"
}

def getBuildConfiguration() {
  return " -Dmaven.test.failure.ignore=true"
}

def getBranchVersion() {
  if (params.releaseVersion.trim().length() > 0) {
    echo "releaseVersion set, use ${params.releaseVersion}"
    return params.releaseVersion.trim()
  }
  if (env.BRANCH_NAME.startsWith('release') || env.BRANCH_NAME.startsWith('develop')) {
    return ""
  }
  version = "1.0.0"
  return "$version" + "-${env.BRANCH_NAME}-SNAPSHOT".replaceAll("[^A-Za-z0-9]", "-")
}

def getPomVersion() {
  results = ""
  try {
    results = sh returnStdout: true,
                 script: "xmllint pom.xml --xpath \"/*[local-name()='project']/*[local-name()='version']/text()\""
  } catch (Exception e) {
    echo "Problem parsing pom.xml : " + e
  }
  return results.trim()
}

def getArtifactTag(pom_version) {
  if (params.releaseVersion.trim().length() > 0) {
    return params.releaseVersion.trim()
  }
  if (pom_version.length() < 1) {
    echo "Could not determine pom version"
    return "Unknown"
  }
  return pom_version
}

def runMaven(command) {
  if (isUnix()) {
    sh "mvn $command"
  } else {
    bat "mvn $command"
  }
}

def getBuildDirectories() {
  def results = " "
  if (env.BUILD_NUMBER.toInteger() < 2) {
    echo "Detected initial build, will build everything"
    return results
  }
  if (env.doDeploy == "true") {
    echo "Detected a deployment, will build everything"
    return results
  }
  def changeLogSets = currentBuild.changeSets
  for (int i = 0; i < changeLogSets.size(); i++) {
    def entries = changeLogSets[i].items
    echo "Count of changed items : ${entries.length}"
    for (int j = 0; j < entries.length; j++) {
        def entry = entries[j]
        echo "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}"
        def files = new ArrayList(entry.affectedFiles)
        for (int k = 0; k < files.size(); k++) {
            def file = files[k]
            echo "${file.path}"
            //  Check for two "slash" characters in the file path
            if (file.path ==~ /.*\/.*\/.*/) {
              lastChar = file.path.indexOf("/", file.path.indexOf("/") + 1)
              results += " -pl " + file.path.substring(0, lastChar)
            } else {
              echo "Detected a top level file change, will build everything"
              return " "
            }
        }
    }
  }
  echo "============================================================"
  echo "Maven build directories:"
  echo "$results"
  echo "============================================================"
  return results
}

def runCoverity(maxIssues) {
  sh "mkdir -p $TARGET_DIR"
  results = sh script:"coverity/script.sh", returnStatus: true
  if (results != 0) {
    unstable("There was a problem with the coverity script " + results)
  }
  PROJECT = getCoverityProject()
  results = coverityIssueCheck coverityInstanceUrl: 'https://coverity.swtools.honeywell.com:8443', projectName: "$PROJECT", returnIssueCount: true, viewName: "Outstanding Issues"
  if (results > maxIssues) {
    unstable("Found $results Coverity issues, maximum is $maxIssues")
  }
}

def getCoverityProject() {
 if (isRelease()) {
   return "MULTISENSE-RELEASE"
 }
 return "MULTISENSE"
}

def getCoverityStream() {
 return getCoverityProject() + "-MASTER"
}
