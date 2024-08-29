pipelineJob('seed_job') {
    description('This job creates other jobs and folders based on provided parameters.')

    parameters {
        stringParam('FOLDER', '', '# 폴더로 구분하고자할 때만 사용 # 예시 : Test1/test2')
        stringParam('PROJECT_NAME', 'assistant', '# 프로젝트 구분으로 Manifest Git의 폴더 구분 예시 : assistant')
        stringParam('GIT_NAME', 'assistant-management', 'Bitbucket의 Source Git이름 예시 : assistant-management')
        choiceParam('LANG_NAME', ['java', 'python'], '# 선택 필수')
        choiceParam('ENVIRONMENTS', ['dev', 'prod'], '# 변경 시 아래 2개 파라미터도 변경 필수 # APP_IMAGE_REGISTRY # APP_IMAGE_CREDENTIALS')
        stringParam('APP_IMAGE_REGISTRY', 'devaiassistant.azurecr.io', '# 배포 환경에 맞는 Container Registry 주소 ## Dev devaiassistant.azurecr.io ## Prod prodaiassistant.azurecr.io')
        credentialsParam('APP_IMAGE_CREDENTIALS') {
            type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
            description('SELECTME')
            defaultValue('dev-assistant-acr')
            required(true)
        }
        stringParam('MANIFEST_GIT_URL', 'git@bitbucket.org:LaonPeople/assistant-common-manifest.git', '# 기본값으로 사용')
    }

    definition {
        cps {
            script("""
                // Job 생성 로직을 정의
                final String param_FOLDER = params.FOLDER?.trim() ?: ''
                final String projectName = params.PROJECT_NAME.trim()
                final String gitName = params.GIT_NAME.trim()
                final String environment = params.ENVIRONMENTS.trim()
                final Map<String, String> folderInfo = [
                    "Test1": "테스트1",
                    "Test2": "테스트2"
                ]

                if (param_FOLDER) {
                    def folders = param_FOLDER.split('/')
                    def folderPath = ""

                    folders.each { folderName ->
                        folderPath = folderPath ? "\${folderPath}/\${folderName}" : folderName
                        def displayName = folderInfo[folderName] ?: folderName
                        folder(folderPath) {
                            displayName(displayName)
                            description("Automatically created folder: \${displayName}")
                        }
                    }

                    // 최종 폴더 내에 Job 생성
                    pipelineJob("\${folderPath}/\${environment}-\${gitName}") {
                        definition {
                            cps {
                                script("""
                                    pipeline {
                                        agent any
                                        stages {
                                            stage('Build') {
                                                steps {
                                                    echo 'Building...'
                                                }
                                            }
                                        }
                                    }
                                """)
                                sandbox()
                            }
                        }
                    }
                } else {
                    error("FOLDER parameter is required to create nested folders.")
                }
            """)
            sandbox(true)
        }
    }
}
