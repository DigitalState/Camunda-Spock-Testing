import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*

import org.junit.Rule

import spock.lang.*

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomjankes.wiremock.WireMockGroovy


@Narrative("""
Using Wiremock as basic usage
""")
@Title("Wiremock - Basic Usage")

class WiremockSpec extends Specification {

  def wmPort = 8081

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');
  
  @Rule
  WireMockRule wireMockRule = new WireMockRule(wmPort)
  
  def wireMockGroovy = new WireMockGroovy(wmPort)

  @Deployment(resources = ["bpmn/5_Wiremock/wiremock.bpmn"])
  def "WireMock Test"() {
    given: _ "Web Server is running"
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
      def processInstance = runtimeService().startProcessInstanceByKey("wiremock", startingVars)

    then:_ "Process has the Wiremock Response and _env variable"
      def processInstanceId = processInstance.getProcessInstanceId()
      def historicVariableInstanceQuery = historyService().createHistoricVariableInstanceQuery()
                                                          .processInstanceId(processInstanceId)

      def varValues = historicVariableInstanceQuery.list().collectEntries({[(it.getName().toString()) : it.getValue()]})
      println varValues.sort().toString()
      def desiredValue = ['ws_response':'Some body', '_env':'unit-test']
      
      assertThat(processInstance).hasVariables('ws_response')
      assert varValues.sort().toString() == desiredValue.sort().toString()
  }
}