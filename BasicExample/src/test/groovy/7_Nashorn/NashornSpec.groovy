// import org.camunda.bpm.engine.runtime.ProcessInstance
// import org.camunda.bpm.engine.test.Deployment
// import org.camunda.bpm.engine.test.ProcessEngineRule

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/

// Used for the Spock Mock of the 'execution' variable in the script
import org.camunda.bpm.engine.delegate.DelegateExecution

// import org.junit.Rule

import spock.lang.*

// import static org.camunda.spin.Spin.*


import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Narrative("""
As a bpmn developer I want to Unit Test Nashorn Scripts
""")
// @See("http://forum.camunda.org") // Can also be applied to methods. Can be a array.
// @Issue("http://my.issues.org/FOO-1") // Can also be applied to methods. Can be a array.
@Title("Nashorn Unit Testing Test")

class NashornSpec extends Specification {

  // Use when you want to run the Camunda Engine
  // @Rule
  // public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Shared ScriptEngine engine = new ScriptEngineManager().getEngineByName('nashorn');

  // Use when you want to deploy a resource with the Camunda engine
  // @Deployment(resources = ['bpmn/7_Nashorn/nashorn.bpmn', 'bpmn/7_Nashron/Transforms.js'])
  def 'Nashorn Unit Testing'() {
    setup:_ 'Setup Mocks and binding'
      // Generates a Spock Mock using the DelegateExecution interface from Camunda engine
      def execution = Mock(DelegateExecution)
      
      // When a script uses .getVariable('dog') then return the value 'Frank'
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_stubbing
      execution.getVariable('dog') >> 'Frank'

      // Create a bindning for the 'execution' Mock() and the Nashorn engine.
      // The binding allows the Nashorn engine to execute code from the Spock Test / the Spock Mock.
      engine.put('execution', execution)

    when:_ 'Execute overall .js script and call "transform" function'
      // Gets the specific .js script as text/a string.
      // The path being src/test/resources/bpmn/7_Nashron/Transforms.js
      // src/test/resources is defaulted 
      def source = this.class.getResource('bpmn/7_Nashron/Transforms.js').text

      // .eval executes the script, so anything that is not wrapped in a function will be called.
      // Given the way Camunda executes its scripts, this will usually mean that the full script will be executed.
      // evalResult is the returned value of the script. See the .js script for further comments.
      def evalResult = engine.eval(source);

      // Example of calling a specific function.
      Map result = engine.invokeFunction('transform', [name: [first: 'James', last: 'Bond']])

    then:_ 'Assert that Frank is returned by overall script, and that transform returns James Bond'

      // Use this line if you want to test the number of executions AND the outputted value
      // See: http://spockframework.org/spock/docs/1.0/interaction_based_testing.html#_combining_mocking_and_stubbing
      // 1 * execution.getVariable('dog') >> 'Franks'

      assertThat 'The js script returned the string "Frank"',
                  evalResult == 'Frank'

      assertThat 'The firstName is "James"',
                  result.firstName == 'James'

      assertThat 'The lastName is "Bond"',
                  result.lastName == 'Bond'

  }
}