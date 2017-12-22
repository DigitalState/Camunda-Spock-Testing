import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.junit.Rule;

import spock.lang.*

import static org.camunda.spin.Spin.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

@Narrative("""
Testing the validation of SPIN JSON objects in the BPMN
against the test SPIN JSON objected created in this spec
""")
// @See("http://forum.camunda.org") // Can also be applied to methods. Can be a array.
// @Issue("http://my.issues.org/FOO-1") // Can also be applied to methods. Can be a array.
@Title("SPIN JSON Validation Test")

class SpinJsonSpec extends Specification {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Deployment(resources = ["bpmn/4_SpinJsonSpec/myProcess.bpmn"])
  def "SPIN JSON Validation"() {
    when:_ "Starting Process Instance"

      //@TODO Move variable creation and management into its own class.
      def json = JSON("{\"customer\": \"Kermit\"}")
      def variableMap = ['myJSON':json]

      def processInstance = runtimeService().startProcessInstanceByKey("myProcess", variableMap)
      def executionId = processInstance.getId()
      println processInstance.getProcessDefinitionId()

    then:_ "Process has the mySpinVar variable"
      assertThat(processInstance).hasVariables('mySpinVar', 'myJSON' )

    and:_ "myJson is SPIN JSON"
      def spinVar = runtimeService().getVariable(executionId, 'myJSON')
      assert spinVar.getClass().getName() == 'org.camunda.spin.impl.json.jackson.JacksonJsonNode'
      println spinVar.getClass().getSimpleName()

    and:_ "The process is waiting at the placeholder user task"
      assertThat(processInstance).isWaitingAt("Task_11aspuv")

    and:_ "We complete the task"
      complete(task(processInstance))

    and:_ "The process has ended"
      assertThat(processInstance).isEnded()

    and:_ "Process Instance had Variables"
      def processInstanceId = processInstance.getProcessInstanceId()
      def historicVariableInstanceQuery = historyService().createHistoricVariableInstanceQuery().processInstanceId(processInstanceId)

      assertThat(historicVariableInstanceQuery.list(), hasItems(
                                                                both(hasProperty("name", equalTo("myJSON"))).and(instanceOf(org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class)),
                                                                both(hasProperty("name", equalTo("mySpinVar"))).and(instanceOf(org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity.class)),
                                                                ));

    and:_ "Variables Match Desired Result"

      // Remaps list of variables to a [Name:Type] structure
      // Always ensure to toString your key and values as there is a difference between single and double quotes in groovy
      // aka GString vs String for comparison: https://stackoverflow.com/a/10528486
      def varNameType = historicVariableInstanceQuery.list().collectEntries({
                                                                            [(it.getName().toString()) : (it.getTypeName().toString())]
                                                                            })
      println "Variables with Types:"
      println varNameType
      
      // List of Variable Names and Variable Types that should exist when process is finished
      // Always use single quotes to ensure proper string usage: https://stackoverflow.com/a/10528486
      def desiredResult1 = [
        'mySpinVar': 'json',
        'myJSON': 'json',
        'someNumber': 'integer'
      ]

      assert varNameType == desiredResult1

    and:_ "Variable Value is desired value"

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
      assert varValues.sort().toString() == desiredResult2.sort().toString()
  }
}