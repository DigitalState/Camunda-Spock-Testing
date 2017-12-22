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
A simple test to demonstrate
how external scripts are loaded for testing
""")
// @See("http://forum.camunda.org") // Can also be applied to methods. Can be a array.
// @Issue("http://my.issues.org/FOO-1") // Can also be applied to methods. Can be a array.
@Title('External Script Test')

class ExternalScriptSpec extends Specification {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Issue('https://forum.camunda.org/t/unit-testing-external-scripts-deployment-file-js-cannot-find-resource-at-path/5661')
  @Deployment(resources = ["bpmn/3_ExternalScriptTest/myProcess.bpmn", "bpmn/3_ExternalScriptTest/file.js" ])
  def "External Script Test"() {
    when:_ 'Starting Process Instance'

      //@TODO Move variable creation and management into its own class.
      def json = JSON("{\"customer\": \"Kermit\"}")
      def variableMap = ['myJSON':json]

      def processInstance = runtimeService().startProcessInstanceByKey("myProcess", variableMap)
      def executionId = processInstance.getId()
      println processInstance.getProcessDefinitionId()

    then:_ "Process has the mySpinVar variable"
      assertThat(processInstance).hasVariables('mySpinVar', 'myJSON' )

    and:_ 'myJson is SPIN JSON'
      def spinVar = runtimeService().getVariable(executionId, 'myJSON')
      assert spinVar.getClass().getName() == 'org.camunda.spin.impl.json.jackson.JacksonJsonNode'
      println spinVar.getClass().getSimpleName()

    and:_ 'The process is waiting at the placeholder user task'
      assertThat(processInstance).isWaitingAt("Task_11aspuv")

    and:_ 'We complete the task'
      complete(task(processInstance))

    and:_ 'The process has ended'
      assertThat(processInstance).isEnded()

  }
}