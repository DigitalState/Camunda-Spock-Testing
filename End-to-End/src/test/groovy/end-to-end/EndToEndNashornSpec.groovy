//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

// Used for the Spock Mock of the 'execution' variable in the script
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.ProcessDefinition

import spock.lang.*

import static org.camunda.spin.Spin.*

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

@Narrative("""
As a bpmn developer I want to Unit Test Nashorn Scripts
""")

@Title("Nashorn Unit Testing Test")

class EndToEndNashornSpec extends Specification {

  @Shared ScriptEngine engine = new ScriptEngineManager().getEngineByName('nashorn');

  //
  // gateway_decision.js
  //
  def 'Nashorn: gateway_decision.js'(int number_dataTable, boolean gatewayDecision) {
    setup:_ 'Setup Mocks and binding'
      // Generates a Spock Mock using the DelegateExecution interface from Camunda engine
      def execution = Mock(DelegateExecution)
      
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_stubbing
      execution.getVariable('number') >> number_dataTable

      // Create a bindning for the 'execution' Mock() and the Nashorn engine.
      // The binding allows the Nashorn engine to execute code from the Spock Test / the Spock Mock.
      engine.put('execution', execution)


    when:_ 'Execute Script'
      // Gets the specific .js script as text/string.
      def source = this.class.getResource('bpmn/end-to-end/gateway_decision.js').text

      // .eval executes the script, so anything that is not wrapped in a function will be called.
      // Given the way Camunda executes its scripts, this will usually mean that the full script will be executed.
      // evalResult is the returned value of the script. See the .js script for further comments.
      def evalResult = engine.eval(source);

    then:_ 'Gateway Decision matches expection'

      assertThat 'The script retuned the exepected result',
                  evalResult == gatewayDecision
      println "Script Response: ${evalResult}"
      println "DataTable Expected Response: ${gatewayDecision}"

    where:
        number_dataTable || gatewayDecision
       -1 || false
        0 || false
        1 || false
        2 || false
        3 || false
        4 || false
        5 || true
        6 || true
        7 || true
        8 || true
        9 || true
       10 || true
       11 || false
  }

  //
  // script1.js
  //
  def 'Nashorn: script1.js'() {
    setup:_ 'Setup Mocks and binding'
      // Generates a Spock Mock using the DelegateExecution interface from Camunda engine
      def execution = Mock(DelegateExecution)
      
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_stubbing
      execution.setVariable('someVar', 'Some String Value') >> null

      // Create a bindning for the 'execution' Mock() and the Nashorn engine.
      // The binding allows the Nashorn engine to execute code from the Spock Test / the Spock Mock.
      engine.put('execution', execution)

      when:_ 'Execute Script'
      // Gets the specific .js script as text/string.
      def source = this.class.getResource('bpmn/end-to-end/script1.js').text

      // Bidnings do not appear to function for importing the S variable into the engine.
      // Based on how SPIN library does it, we copy the same pattern:
      // https://github.com/camunda/camunda-spin/blob/master/core/src/main/resources/script/env/javascript/spin.js
      // https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/scripting/env/ScriptingEnvironment.java#L91
      engine.eval('var S = org.camunda.spin.Spin.S')

      // .eval executes the script, so anything that is not wrapped in a function will be called.
      // Given the way Camunda executes its scripts, this will usually mean that the full script will be executed.
      // evalResult is the returned value of the script. See the .js script for further comments.
      def evalResult = engine.eval(source);

    then:_ 'Script1.js output matches the expected result'

      def jsonSlurper = new JsonSlurper()
      def expectedResult_json = jsonSlurper.parseText '''
        {
          "someKey1": "some Value 1",
          "someKey2": "some Value 2",
          "someKey3": {
              "someSubKey1": "some Sub Value 1",
              "someSubKey2": [
                {
                  "someArrayKey1": "Some Array Value 1"
                },
                {
                  "someArrayKey2": "Some Array Value 2",
                  "someArrayKey3": "Some Array Value 3"
                }
              ]
          }
        }
        '''

      def expectedResult_Spin = S(JsonOutput.toJson(expectedResult_json))

      assertThat 'The script retuned the exepected result',
                  evalResult == expectedResult_Spin
  }

  //
  // script2.js
  //
  def 'Nashorn: script2.js'() {
    setup:_ 'Setup Mocks and binding'
      // Generates a Spock Mock using the DelegateExecution interface from Camunda engine
      def execution = Mock(DelegateExecution)
      
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_stubbing
      execution.getVariable('number') >> 1 // Just a number to test the overall execution of the script.

      // Create a bindning for the 'execution' Mock() and the Nashorn engine.
      // The binding allows the Nashorn engine to execute code from the Spock Test / the Spock Mock.
      engine.put('execution', execution)

    when:_ 'Execute Script'
      // Gets the specific .js script as text/string.
      def source = this.class.getResource('bpmn/end-to-end/script2.js').text

      // .eval executes the script, so anything that is not wrapped in a function will be called.
      // Given the way Camunda executes its scripts, this will usually mean that the full script will be executed.
      // evalResult is the returned value of the script. See the .js script for further comments.
      def evalResult = engine.eval(source);

    then:_ 'Script2.js output matches the expected result'

      assertThat 'script2.js returned the expected result',
                  evalResult == 1
  }

  //
  // start_event.js
  //
  def 'Nashorn: start_event.js'() {
    setup:_ 'Setup Mocks and binding'
      // Generates a Spock Mock using the DelegateExecution interface from Camunda engine
      def execution = Mock(DelegateExecution)

      // When a script uses .getVariable('dog') then return the value 'Frank'
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_stubbing
      def mock_processDefId = 'def123'
      def mock_deploymentId = 'deploy123'
      def mock_configFileName = 'config.json'
      
      def mock_resource = this.class.getResource('bpmn/end-to-end/config.json')

      // Each engine service and used class in the chained calls needs to be Mocked
      // @TODO look into pattern or helper class for deep stubs similar to how Mockito functions
      def processEngineServices = Mock(ProcessEngineServices)
      def repositoryService = Mock(RepositoryService)
      def processDefinitonInterface = Mock(ProcessDefinition)
      execution.getProcessDefinitionId() >> mock_processDefId
      execution.getProcessEngineServices() >> processEngineServices
      execution.getProcessEngineServices().getRepositoryService() >> repositoryService

      execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mock_processDefId) >> processDefinitonInterface
      execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mock_processDefId).getDeploymentId() >> mock_deploymentId
      execution.getProcessEngineServices().getRepositoryService().getResourceAsStream(mock_deploymentId, mock_configFileName) >> mock_resource.newInputStream()
      execution.setVariable('_config', _) >> null

      // Create a bindning for the 'execution' Mock() and the Nashorn engine.
      // The binding allows the Nashorn engine to execute code from the Spock Test / the Spock Mock.
      engine.put('execution', execution)

      when:_ 'Execute Script'
      // Gets the specific .js script as text/string.
      def source = this.class.getResource('bpmn/end-to-end/start_event.js').text

      // Bidnings do not appear to function for importing the S variable into the engine.
      // Based on how SPIN library does it, we copy the same pattern:
      // https://github.com/camunda/camunda-spin/blob/master/core/src/main/resources/script/env/javascript/spin.js
      // https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/scripting/env/ScriptingEnvironment.java#L91
      engine.eval('var S = org.camunda.spin.Spin.S')

      // .eval executes the script, so anything that is not wrapped in a function will be called.
      // Given the way Camunda executes its scripts, this will usually mean that the full script will be executed.
      // evalResult is the returned value of the script. See the .js script for further comments.
      def evalResult = engine.eval(source);

    then:_ 'start_event.js output matches the expected result'

      def jsonSlurper = new JsonSlurper()
      def expectedResult_json = jsonSlurper.parse(mock_resource.newInputStream(), 'UTF-8')

      def expectedResult_Spin = S(JsonOutput.toJson(expectedResult_json)).prop('script_paths')

      assertThat 'The script retuned the exepected result',
                  evalResult == expectedResult_Spin
  }
}