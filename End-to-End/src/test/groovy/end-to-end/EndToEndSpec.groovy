// import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

// Used for the Spock Mock of the 'execution' variable in the script
import org.camunda.bpm.engine.delegate.DelegateExecution

// import org.junit.Rule
import org.junit.ClassRule

import spock.lang.*

import static org.camunda.spin.Spin.*

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Narrative("""
As a bpmn developer I want to have end to end tests
that perform unit tests for BPMN
""")
@Title("End to End Testing of BPMN")

class EndToEndSpec extends Specification {

  // Use when you want to run the Camunda Engine
  @ClassRule
  @Shared public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  
  @Shared ScriptEngine engine = new ScriptEngineManager().getEngineByName('nashorn')
  @Shared String deploymentId
  @Shared String executionId

  // helper method to shorten the .addInputStream params in createDeploment()
  def resourceStream(path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def setupSpec(){
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/repository/DeploymentBuilder.html
    def deployment = repositoryService().createDeployment()
                                        .addInputStream('end-to-end.bpmn', resourceStream('bpmn/end-to-end/end-to-end.bpmn'))
                                        .addInputStream('config.json', resourceStream('bpmn/end-to-end/config.json'))
                                        .addInputStream('start_event.js', resourceStream('bpmn/end-to-end/start_event.js'))
                                        .addInputStream('script1.js', resourceStream('bpmn/end-to-end/script1.js'))
                                        .addInputStream('script2.js', resourceStream('bpmn/end-to-end/script2.js'))
                                        .addInputStream('gateway_decision.js', resourceStream('bpmn/end-to-end/gateway_decision.js'))
                                        .name('myDeployment')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def 'End to End BPMN Test with Data Tables'(int number_dataTable) {
    // "number_dataTable" input comes from the "where:" block below with the datatable

    when:_ 'Starting the process instance'

      //@TODO Move variable creation and management into its own class.
      def json = S("{\"customer\": \"Kermit\"}")
      def startingVariables = [
                                'json':json,
                                'number': number_dataTable
                                ]
      def processInstance = runtimeService().startProcessInstanceByKey("end-to-end", startingVariables)
      executionId = processInstance.getId()

    then:_ 'Process is Active and waiting for user task completion'
      assertThat(processInstance).isActive()
      assertThat(processInstanceQuery().count()).isEqualTo(1)

      def numberValue = runtimeService().getVariable(executionId, 'number')
      println "'Number' value is: ${numberValue}"
      
      if (numberValue >= 5 && numberValue <= 10){
        assertThat(processInstance).isWaitingAt("Task_1bvdtdv") // "Do Something" User Task
      
      } else {
        assertThat(processInstance).isWaitingAt("Task_1xjtauh") // "Do Something Else" User Task
        complete(task(processInstance))
      
        println 'Completed "Do Something Else" task'
      }

    then:_ 'Complete Final User Task'
      assertThat(processInstance).isWaitingAt("Task_1bvdtdv") // "Do Something" User Task
      complete(task(processInstance))
    
    then:_ 'Process has ended'
      assertThat(processInstance).isEnded()

    // where statement using datatables to test multiple number inputs
    // See http://spockframework.org/spock/docs/1.0/data_driven_testing.html for info about data tables
    where:
      number_dataTable | _
                     1 | _
                     7 | _
                     0 | _
  }
  // Cleanup the deployment after the Spec has completed.
  def cleanupSpec() {
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "Deployment ID: '${deploymentId}' has been deleted"

  }

}