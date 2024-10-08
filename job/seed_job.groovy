#!groovy

// Jenkins Job DSL
// https://github.com/jenkinsci/job-dsl-plugin
import javaposse.jobdsl.dsl.DslFactory

final String MANIFEST_GIT_CREDENTIAL = "Bitbucket"
//docker ecr 주소입니다.
final String JENKINSFILE_GIT_URL = "git@github.com:Jason-Shin-git/Jenkinsfile.git"
// final String JENKINSFILE_GIT_URL = "git@bitbucket.org:LaonPeople/assistant-common-jenkinsfile.git"
//빌드용 : Gitlab에 접근을 위한 credential id
final String GIT_CREDENTIALS = "Bitbucket"

/*
 * 아래는 고정된 파라미터 및 변수 입니다.
 */
final String param_FOLDER = "${FOLDER}".trim()
final String param_PROJECT_NAME = "${PROJECT_NAME}".trim()
final String param_MANIFEST_GIT_URL = "${MANIFEST_GIT_URL}"


final String param_LANG_NAME = "${LANG_NAME}"
final String param_PROJECT_GIT_URL = "git@bitbucket.org:LaonPeople/${GIT_NAME}.git"
final String param_GIT_NAME = "${GIT_NAME}".trim()
final String param_ENVIRONMENTS = "${ENVIRONMENTS}".trim()
final String param_APP_IMAGE_REGISTRY = "${APP_IMAGE_REGISTRY}"
final String param_APP_IMAGE_CREDENTIALS = "${APP_IMAGE_CREDENTIALS}"
// final String param_ ="${WORKSPACE}".tokenize('workspace/')
// 이미지 생성 Job

if (param_FOLDER) {
    // 폴더 경로를 '/'로 분리하여 각 폴더를 생성합니다.
    def folders = param_FOLDER.split('/')
    def folderPath = ""

    folders.each { folderName ->
        folderPath = folderPath ? "${folderPath}/${folderName}" : folderName
        folder(folderPath) {
            displayName("${folderName}")
            description("Automatically created folder: ${folderPath}")
        }
    }
}
pipelineJob("${param_FOLDER}/${param_ENVIRONMENTS}-${param_GIT_NAME}") {
    description("""

    """)
    displayName("[${param_ENVIRONMENTS}] ${param_GIT_NAME}")
    properties {
        authorizeProjectProperty {
            strategy {
                triggeringUsersAuthorizationStrategy()
            }
        }
    }

    parameters {
        stringParam('BRANCH_NAME', "$param_ENVIRONMENTS", '')
        stringParam('PROJECT_NAME', "$param_PROJECT_NAME", '')
        stringParam('GIT_NAME', "$param_GIT_NAME", '')
        stringParam('APP_IMAGE_REGISTRY', "$param_APP_IMAGE_REGISTRY", '')
        stringParam('LANG_NAME', "$param_LANG_NAME", '')
        stringParam('SCM_URL', "$param_PROJECT_GIT_URL", '')
        stringParam('MANIFEST_GIT_URL', "$param_MANIFEST_GIT_URL", '')
    }
    triggers {
        genericTrigger {
            genericVariables {
                genericVariable {
                    key("BRANCH_NAME")
                    value("\$.push.changes[0].new.name")
                    expressionType("JSONPath") //Optional, defaults to JSONPath
                    regexpFilter("") //Optional, defaults to empty string
                    defaultValue("") //Optional, defaults to empty string
                }
            }
            token("${param_GIT_NAME}")
            causeString("Generic Cause")
            regexpFilterExpression("^${param_ENVIRONMENTS}")
            regexpFilterText("\$BRANCH_NAME")
        }
    }

    logRotator {
        numToKeep 30
    }

    properties {
        disableResume()
        disableConcurrentBuilds {
            abortPrevious false
        }
    }

    definition {
        cpsScm {
            scriptPath "app/${param_LANG_NAME}/${param_ENVIRONMENTS}/Jenkinsfile"
            lightweight(true)
            scm {
                git {
                    branch "*/main"

                    remote {
                        name 'origin'
                        url JENKINSFILE_GIT_URL
                        // refspec '+refs/heads/*:refs/remotes/origin/* +refs/merge-requests/*/head:refs/remotes/origin/merge-requests/*'
                        credentials GIT_CREDENTIALS
                    }

                }
            } /* scm */
        }
    } /* definition */
} /* pipelineJob */

// for (def environment in param_ENVIRONMENTS) {
//     for (def module in param_MODULES) {
//         final GString jobName = "${module}-${environment}"
//         final String jenkinsfilePath = "Jenkinsfile"

//         pipelineJob(jobName) {
//             displayName("[$environment] ${module}")
//             parameters {
//                 choiceParam('MODULE_NAME', [module], '')
//                 choiceParam('PROJECT_NAME', [PROJECT_NAME], '')
//                 choiceParam('APP_IMAGE_REGISTRY', [APP_IMAGE_REGISTRY], '')
//                 choiceParam('SCM_URL', [param_PROJECT_GIT_URL], '')
//                 // Registry 내 Repository 및 태그.
//                 imageTag{
//                       name('APP_IMAGE')
//                       image("${PROJECT_NAME}/${module}")
//                       registry ('https://905418085337.dkr.ecr.ap-northeast-2.amazonaws.com')
//                       filter('.*')
//                       credentialId("ecr:ap-northeast-2:${param_APP_IMAGE_CREDENTIALS}")
//                 }
            
//                 choiceParam('ENVIRONMENT', [environment], '')
//             }

//             logRotator {
//                 numToKeep 30
//             }

//             properties {
//                 disableResume()
//                 disableConcurrentBuilds {
//                     abortPrevious false
//                 }
//             }

//             definition {
//                 cpsScm {
//                     scriptPath jenkinsfilePath
//                     lightweight(true)
//                     scm {
//                         git {
//                             branch 'refs/heads/master'

//                             remote {
//                                 name 'origin'
//                                 url K8S_YAML_DEPLOYMENT_GIT_URL
//                                 credentials K8S_DEPLOY_GIT_CREDENTIAL
//                             }

//                             extensions {
//                                 pruneBranches()
//                                 pruneTags {
//                                     pruneTags(true)
//                                 }
//                                 cleanAfterCheckout()
//                                 cloneOptions {
//                                     noTags true
//                                     shallow true
//                                     depth 1
//                                     timeout 1
//                                 }
//                             } /* extensions */
//                         }
//                     } /* scm */
//                 }
//             } /* definition */
//         } /* pipelineJob */

//     }
    
    
// }
