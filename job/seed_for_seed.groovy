job('seed_job') {
    description('This job creates other jobs and folders based on provided parameters.')
    properties {
        authorizeProjectProperty {
            strategy {
                triggeringUsersAuthorizationStrategy()
            }
        }
    }

    parameters {
        stringParam('FOLDER', '', '# 폴더로 구분하고자할 때만 사용 \n# 예시 : Test1/test2')
        stringParam('PROJECT_NAME', 'assistant', '# 프로젝트 구분으로 Manifest Git의 폴더 구분 \n예시 : assistant')
        stringParam('GIT_NAME', 'assistant-management', 'Bitbucket의 Source Git이름 \n예시 : assistant-management')
        choiceParam('LANG_NAME', ['java', 'python'], '# 선택 필수')
        choiceParam('ENVIRONMENTS', ['dev', 'prod'], '# 변경 시 아래 2개 파라미터도 변경 필수 \n# APP_IMAGE_REGISTRY \n# APP_IMAGE_CREDENTIALS')
        stringParam('APP_IMAGE_REGISTRY', 'devaiassistant.azurecr.io', '# 배포 환경에 맞는 Container Registry 주소 \n## Dev \ndevaiassistant.azurecr.io \n## Prod \nprodaiassistant.azurecr.io')
        credentialsParam('APP_IMAGE_CREDENTIALS') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            description('SELECTME')
            defaultValue('dev-assistant-acr')
            required(true)
        }
        stringParam('MANIFEST_GIT_URL', 'git@bitbucket.org:LaonPeople/assistant-common-manifest.git', '# 기본값으로 사용')
    }

    scm {
        git {
            remote {
                url('git@github.com:Jason-Shin-git/Jenkinsfile.git')
                credentials('Bitbucket')
            }
            branches('*/main')
        }
    }
    steps {
        // Job DSL Plugin configuration
        jobDsl {
            targets('job/seed_job.groovy')
            ignoreExisting()
            sandbox()
        }
    }

    triggers {
        // Add any triggers if needed
    }

}
