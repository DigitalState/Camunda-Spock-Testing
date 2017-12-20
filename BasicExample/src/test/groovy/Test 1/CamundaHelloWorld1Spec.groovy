import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.junit.Rule;

import spock.lang.*

@Narrative("""
As a user of Camunda
I want to run a basic test of the engine
to see if the engine will function with Spock Testing
""")
@See("http://forum.camunda.org") // Can also be applied to methods. Can be a array.
@Issue("http://my.issues.org/FOO-1") // Can also be applied to methods. Can be a array.
@Title("Camunda Test Process Example")

class CamundaHelloWorld1Spec extends Specification {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Deployment(resources = ["bpmn/testProcess.bpmn"])
  def "Test testProcess.bpmn"() {
    when:_ "Starting Process Instance"
      def processInstance = runtimeService().startProcessInstanceByKey("testProcess")
      println processInstance.getProcessDefinitionId()

    then:_ "Process is active"
      assertThat(processInstance).isActive()

    and:_ "only 1 instance is running"
      assertThat(processInstanceQuery().count()).isEqualTo(1)

    and:_ "there is a active task"
      assertThat(task(processInstance)).isNotNull()

    and:_ "We complete the task"
      complete(task(processInstance))

    and:_ "The process has ended"
      assertThat(processInstance).isEnded()
  }
}