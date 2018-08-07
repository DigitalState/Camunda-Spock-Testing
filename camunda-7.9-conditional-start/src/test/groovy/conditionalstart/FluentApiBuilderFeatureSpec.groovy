package conditionalstart

import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.DeploymentBuilder
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.repositoryService
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService


class FluentApiBuilderFeatureSpec extends Specification implements BpmnFluentBuilder {

    @ClassRule
    @Shared ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
    @Shared String deploymentId
    @Shared Map<String, String> filesToDeploy = [:]

    // helper method to shorten the .addInputStream params in createDeployment()
    String resourceAsString(String path){
        return this.class.getResource(path.toString()).getText('UTF-8')
    }

    String modelInstanceAsString(BpmnModelInstance modelInstance){
        String model = Bpmn.convertToString(modelInstance)
        return model
    }

    def setupSpec(){
        filesToDeploy.putAll(
                [
                        'model1.bpmn': modelInstanceAsString(model2()),
                        'fluentModelScript1.js': resourceAsString('/bpmn/conditionalStart/fluentModelScript1.js')
                ]
        )

        DeploymentBuilder deployment = repositoryService().createDeployment()
        filesToDeploy.each { file ->
            deployment.addString(file.getKey(), file.getValue())
        }
        deployment.name('FluentModelBuilder')
                .enableDuplicateFiltering(false)
        Deployment deployed = deployment.deploy()

        deploymentId = deployed.getId()
    }

    def 'Fluent API Builder Test 1'() {
        when: 'Creating a instance of model1 process definition'
        def processInstance = runtimeService().startProcessInstanceByKey('model')

        then: 'Process is Active'
        assertThat(processInstance).isActive()

//        and: 'Process has the dogsName variable'
//        assertThat(processInstance).hasVariables('dogsName')
//      reportInfo(generateCoverageData(processEngine(), processInstance, "externalTask.bpmn"))
    }

    def cleanupSpec() {
//     https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
        exportDeploymentFromCamundaDB(deploymentId)
        exportDeploymentFromSource(filesToDeploy)

        repositoryService().deleteDeployment(deploymentId,
                true, // cascade
                true, // skipCustomListeners
                true) // skipIoMappings
        println "Deployment ID: '${deploymentId}' has been deleted"
    }

    void exportDeploymentFromCamundaDB(String deploymentId){
        Map<String, InputStream> files = [:]
        repositoryService().getDeploymentResources(deploymentId).each {
            InputStream fileInputStream = repositoryService().getResourceAsStreamById(deploymentId, it.getId())
            String fileName = it.getName()
            files.put(fileName , fileInputStream)
        }
        FileTreeBuilder treeBuilder = new FileTreeBuilder()
        files.each { file ->
            treeBuilder {
                target {
                    'camunda-deployment-files-from-db' {
                        "${file.getKey()}" file.getValue().getText('UTF-8')
                    }
                }
            }
        }
    }

    void exportDeploymentFromSource(Map<String, String> files = [:]){
        FileTreeBuilder treeBuilder = new FileTreeBuilder()
        files.each { file ->
            treeBuilder {
                target {
                    'camunda-deployment-files-from-source' {
                        "${file.getKey()}" file.getValue()
                    }
                }
            }
        }
    }

}

