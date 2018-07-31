package conditionalstart

import org.camunda.bpm.engine.test.ProcessEngineRule
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.repositoryService
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

class FluentApiBuilderFeatureSpec extends Specification implements BpmnFluentBuilder {

  @ClassRule
  @Shared ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  @Shared String deploymentId

  // helper method to shorten the .addInputStream params in createDeployment()
  def resourceStream(String path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def setupSpec(){
    def deployment = repositoryService().createDeployment()
                                        .addModelInstance('model1.bpmn', model1())
                                        .addInputStream('fluentModelScript1.js', resourceStream('/bpmn/conditionalStart/fluentModelScript1.js'))
                                        .name('Fluent Model Builder')
                                        .enableDuplicateFiltering(false)
                                        .deploy()
    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def 'Fluent API Builder Test 1'() {
    when: 'Creating a instance of model1 process definition'
      def processInstance = runtimeService().startProcessInstanceByKey('model1')

    then: 'Process is Active'
      assertThat(processInstance).isActive()

    and: 'Process has the dogsName variable'
      assertThat(processInstance).hasVariables('dogsName')
//      reportInfo(generateCoverageData(processEngine(), processInstance, "externalTask.bpmn"))
  }

    def cleanupSpec() {
//     https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
      repositoryService().deleteDeployment(deploymentId,
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
       println "Deployment ID: '${deploymentId}' has been deleted"
  }

}

