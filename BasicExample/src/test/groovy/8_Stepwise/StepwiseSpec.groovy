import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*

import org.junit.ClassRule
import org.junit.Rule

import spock.lang.*

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy


@Narrative("""
Using Spock Stepwise as basic usage
In this example the Wiremock server and 
camunda process engine are setup only during
SetupSpec.  The WireMock Server is reset each step (@Rule)
and Process Engine is setup only once (@ClassRule)
""")
@Title("Stepwise Spec - Basic Usage")

@Stepwise
class StepwiseSpec extends Specification {

  def wmPort = 8081

  @ClassRule
  @Shared public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');
  
  @Shared String deploymentId

  @Rule
  WireMockRule wireMockRule = new WireMockRule(wmPort)
  
  // helper method to shorten the .addInputStream params in createDeploment()
  def resourceStream(path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def setupSpec(){
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/repository/DeploymentBuilder.html
    def deployment = repositoryService().createDeployment()
                                        .addInputStream('stepwise.bpmn', resourceStream('bpmn/8_Stepwise/stepwise.bpmn'))
                                        .name('myStepwiseDeployment')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def "WireMock Test 1"() {
    given: _ "Web Server is running for Test 1"
      def wireMockGroovy = new WireMockGroovy(wmPort)
      wireMockGroovy.stub {
        request {
          method "GET"
          url "/some/thing"
        }
        response {
          status 200
          body "Some body"
          headers {
            "Content-Type" "text/plain"
          }
        }
      }

    when:_ "Starting Process Instance"
      def startingVars = [
        '_env': 'unit-test'
      ]
      def processInstance = runtimeService().startProcessInstanceByKey("stepwise", startingVars)

    then:_ "Process has the Wiremock Response and _env variable"
      def processInstanceId = processInstance.getProcessInstanceId()
      def historicVariableInstanceQuery = historyService().createHistoricVariableInstanceQuery()
                                                          .processInstanceId(processInstanceId)

      def varValues = historicVariableInstanceQuery.list().collectEntries({[(it.getName().toString()) : it.getValue()]})
      println varValues.sort().toString()
      def desiredValue = ['ws_response':'Some body', '_env':'unit-test']
      
      assert varValues.sort().toString() == desiredValue.sort().toString()
  }

  def "WireMock Test 2"() {
    given: _ "Web Server is running Test 2"
      def wireMockGroovy = new WireMockGroovy(wmPort)
      wireMockGroovy.stub {
        request {
          method "GET"
          url "/some/thing"
        }
        response {
          status 200
          body "Some body"
          headers {
            "Content-Type" "text/plain"
          }
        }
      }

    when:_ "Starting Process Instance"
      def startingVars = [
        '_env': 'unit-test'
      ]
      def processInstance = runtimeService().startProcessInstanceByKey("stepwise", startingVars)

    then:_ "Process has the Wiremock Response and _env variable"
      def processInstanceId = processInstance.getProcessInstanceId()
      def historicVariableInstanceQuery = historyService().createHistoricVariableInstanceQuery()
                                                          .processInstanceId(processInstanceId)

      def varValues = historicVariableInstanceQuery.list().collectEntries({[(it.getName().toString()) : it.getValue()]})
      println varValues.sort().toString()
      def desiredValue = ['ws_response':'Some body', '_env':'unit-test']
      
      assert varValues.sort().toString() == desiredValue.sort().toString()
  }


  // Cleanup the deployment after the Spec has completed.
  // This can go next to the setupSpec, but was added at the bottom for demo visual purposes.
  def cleanupSpec() {
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "Deployment ID: '${deploymentId}' has been deleted"
  }


}