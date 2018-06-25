package conditionalstart

import org.camunda.bpm.engine.history.HistoricActivityInstance
import org.camunda.bpm.engine.runtime.ProcessInstance
import spock.lang.*

import org.junit.ClassRule
import org.camunda.bpm.engine.test.ProcessEngineRule

import static org.camunda.spin.Spin.*

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

import static io.digitalstate.camunda.coverage.BpmnCoverageBuilder.*

@Stepwise
class CallActivitySpec extends Specification  {

  @ClassRule
  @Shared ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  @Shared String deploymentId
  @Shared sharedFeatureData = [:]

  def setupSpec(){
    def deployment = repositoryService().createDeployment()
                                        .addInputStream(sequenceFlowHistoryFileName(), sequenceFlowHistoryGenerator())
                                        .addModelInstance('CallActivityCoverage.bpmn', prepModelForCoverage('bpmn/conditionalstart/CallActivityCoverage.bpmn'))
                                        .addModelInstance('CallActivityCoverage2.bpmn', prepModelForCoverage('bpmn/conditionalstart/CallActivityCoverage2.bpmn'))
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

    and: 'We start the CallActivityCoverage process definition'
    def processInstance = runtimeService().startProcessInstanceByKey('CallActivityCoverage')

    then: 'Process is Active and waiting for user task completion'
    assertThat(processInstance).isActive()

    then: 'The current process variables are equal to the starting variables'
    def processVariables = runtimeService().getVariables(processInstance.getProcessInstanceId())
    assertThat(processVariables == startingVariables)

    then: 'The process instance should be waiting for the Call Activity to Complete'
    assertThat(processInstance).isWaitingAt('Task_1gdn63n')

    cleanup:
      sharedFeatureData['CallActivityCoverage1'] = processInstance
      reportInfo(generateCoverageData(processEngine(), processInstance, "CallActivityCoverage1.bpmn Waiting for Called Process"))
  }

def 'Manage CallActivityCoverage2'() {
  setup:
    ProcessInstance callActivityCoverage1ProcessInstance = (ProcessInstance) sharedFeatureData['CallActivityCoverage1']

  when: 'CallActivityCoverage1 was started, get the called called process instance'
    HistoricActivityInstance callActInstance = historyService().createHistoricActivityInstanceQuery()
                                                              .processInstanceId(callActivityCoverage1ProcessInstance.getProcessInstanceId())
                                                              .activityId('Task_1gdn63n')
                                                              .singleResult()

    ProcessInstance callActivityCoverage2ProcessInstance = calledProcessInstance(processInstanceQuery().processInstanceId(callActInstance.getCalledProcessInstanceId()))

  then: 'CallActivityCoverage2 is running'
    assertThat(callActivityCoverage2ProcessInstance).isActive()

  then: 'CallActivityCoverage2 is waiting at the User Task'
    assertThat(callActivityCoverage2ProcessInstance).isWaitingAt('Task_0xjkfyv')

  then: 'Complete the User Task'
    complete(task(callActivityCoverage2ProcessInstance))

  then: 'CallActivityCoverage2 has completed'
    assertThat(callActivityCoverage2ProcessInstance).isEnded()

  then: 'CallActivityCoverage1 has ended'
    assertThat(callActivityCoverage1ProcessInstance).isEnded()

  cleanup:
    reportInfo(generateCoverageData(processEngine(), callActivityCoverage2ProcessInstance, "CallActivityCoverage2.bpmn Completion"))
    reportInfo(generateCoverageData(processEngine(), callActivityCoverage1ProcessInstance, "CallActivityCoverage1.bpmn Completion"))
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

