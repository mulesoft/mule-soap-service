def UPSTREAM_PROJECTS_LIST = [ "Mule-runtime/mule/4.3.x-MAY-DRY-RUN" ]

Map pipelineParams = [ "upstreamProjects" : UPSTREAM_PROJECTS_LIST.join(','),
                       "mavenSettingsXmlId" : "mule-runtime-maven-settings-MuleSettings",
                       "projectType" : "Runtime" ]

runtimeBuild(pipelineParams)
