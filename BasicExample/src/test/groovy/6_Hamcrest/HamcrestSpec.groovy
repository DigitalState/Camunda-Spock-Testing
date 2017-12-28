import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/


import org.junit.Rule;

import spock.lang.*

import static org.camunda.spin.Spin.*;

// import static org.hamcrest.CoreMatchers.is;
// import static org.hamcrest.Matchers.equalTo;
// import static org.hamcrest.Matchers.hasItems;
// import static org.hamcrest.Matchers.instanceOf;
// import static org.hamcrest.Matchers.both;
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.hasProperty;

import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

@Narrative("""
Cleaner Hamcrest Asserting
""")
// @See("http://forum.camunda.org") // Can also be applied to methods. Can be a array.
// @Issue("http://my.issues.org/FOO-1") // Can also be applied to methods. Can be a array.
@Title("Hamcrest Test for cleaner Asserting")

class HamcrestSpec extends Specification {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Deployment(resources = ["bpmn/6_Hamcrest/hamcrest.bpmn"])
  def "SPIN JSON Validation"() {
    when:_ "Starting Process Instance"

      //@TODO Move variable creation and management into its own class.
      def json = JSON("{\"customer\": \"Kermit\"}")
      def variableMap = [
        'myJSON':json
        ]

      def processInstance = runtimeService().startProcessInstanceByKey("hamcrest", variableMap)

    then:_ "Process has ended"
      assertThat(processInstance).isEnded()

    and:_ "Variable Value is desired value"
      def processInstanceId = processInstance.getProcessInstanceId()
      def historicVariableInstanceQuery = historyService().createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)

      //creates a map of [VarName:VarValue] for each variable in the process instance
      def varValues = historicVariableInstanceQuery.list().collectEntries({
                                                                            [(it.getName().toString()) : it.getValue()]
                                                                          })
      println varValues.sort().toString()

      // Generate Groovy parsed JSON objects for each of the Spin variables
      def jsonSlurper = new JsonSlurper()
      def mySpinVar_jsonObject = jsonSlurper.parseText '''
          {
            "myProperty":"myValue"
          }
        '''

      def myJSON_jsonObject = jsonSlurper.parseText '''
          { 
            "customer":"Kermit"
          }
        '''

      // Parse the Groovy JSON into SPIN JSON Objects. the "S()" is the SPIN method.
      def mySpinVar_SPIN = S(JsonOutput.toJson(mySpinVar_jsonObject))
      def myJSON_SPIN = S(JsonOutput.toJson(myJSON_jsonObject))

      def desiredResult2 = [
        'mySpinVar': mySpinVar_SPIN,
        'someNumber': 1234,
        'myJSON': myJSON_SPIN
      ]
      println desiredResult2.sort().toString()

      // always convert to string at the end for final comparison.
      // Groovy, Maps, and SPIN dont seem to like each other for comparison
      // String also provides better error reporting with difference calculation
      // Sort to ensure that maps are the same order, because we use string keys in our map (https://stackoverflow.com/a/5360502)
      // assert varValues.sort().toString() == desiredResult2.sort().toString()
      assertThat 'Variables must match the desired result validation', 
                  varValues.sort().toString() == desiredResult2.sort().toString()
  }
}