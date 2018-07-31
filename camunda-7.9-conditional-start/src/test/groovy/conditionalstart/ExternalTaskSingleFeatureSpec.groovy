package conditionalstart

import org.camunda.bpm.engine.history.HistoricActivityInstance
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.junit.ClassRule
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static io.digitalstate.camunda.coverage.BpmnCoverageBuilder.*
import static org.assertj.core.api.Assertions.assertThat
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.spin.Spin.S

// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

@Ignore
class ExternalTaskSingleFeatureSpec extends Specification  {

  @ClassRule
  @Shared ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  @Shared String deploymentId

  def setupSpec(){
    def deployment = repositoryService().createDeployment()
                                        .addInputStream(sequenceFlowHistoryFileName(), sequenceFlowHistoryGenerator())
                                        .addModelInstance('externalTask.bpmn', prepModelForCoverage('bpmn/conditionalstart/externalTask.bpmn'))
                                        .name('CallActivitiesCoverage')
                                        .enableDuplicateFiltering(false)
                                        .deploy()
    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def 'Manage CallActivityCoverage1'() {
    when: 'Setting up variables'
    def json = S("{\"customer\": \"Kermit\"}")
    def startingVariables = [
            'json': json
    ]

    and: 'We start the External Task process definition'
      def processInstance = runtimeService().startProcessInstanceByKey('externalTask')

    then: 'Process is Active and waiting for user task completion'
      assertThat(processInstance).isActive()
      reportInfo(generateCoverageData(processEngine(), processInstance, "externalTask.bpmn"))
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

