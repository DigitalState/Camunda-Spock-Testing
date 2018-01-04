# Basic Usage Example

# How to use

1. Write spock specifications in `src/test/groovy/...` (whatever folder path).
    1. Spec file names and class names must end with `Spec.groovy`.  Example: `CamundaHelloWorld1Spec.groovy`. 
1. Add your BPMN files to `src/test/resources/bpmn/...` (whatever sub-folder paths you link as long as you configure them in your `@Deployment` annotation)
1. In Terminal, navigate to the root of the folder/project and run `mvn clean test`.
1. Output will be created in the Terminal, and you can look at `./build-reports` folder for JSON reports on your tests.

# Requirements

1. Java 8
1. Maven


# Known Issues

1.<s> `@Deployment` can only be applied to `def` methods inside of the class.  As a result you cannot use Spock's `@Stepwise` feature to chain multiple `def` steps together for a single test/deployment.  Still Researching other ways to implement</s>  This has been resolved with used the @ClassRule annotation.  See the [stepwise example](https://github.com/DigitalState/Camunda-Spock-Testing/blob/master/BasicExample/src/test/groovy/8_Stepwise/StepwiseSpec.groovy)


# Business-centric Features:

1. `@Narative`
1. `@Issue`
1. `@See`
1. `@Title`
1. `_` method after each block that outputs the text in the block to the terminal and build-reports


# Build Reports

Build Reports can be configured in the `./src/test/resources/SpockConfig.groovy` file in the `report` object.

Additional reports and details can be seen in the `target` folder after a attempt of `mvn clean test`.

The `./build-reports` folder has been inlcuded in this folder for example purposes only.  The folder can be deleted, as when `mvn clean test` is run, the folder will be created when required.

# Sample Spec Tests

```groovy
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
```


# Supported Groovy File Extensions:

- .groovy
- .gvy
- .gy
- .gsh

Each extension is equivalent


# Notes about Spock

1. Splitting up a `then:` block with `and:` does not impose any ordering, as `and:` is only meant for documentation purposes and doesn’t carry any semantics.
1. Spock does not provide auto-support for deep stubs/mocks, meaning that when method chaining is used in scripts.  See the [End-to-End start_event.js unit test](https://github.com/DigitalState/Camunda-Spock-Testing/blob/master/End-to-End/src/test/groovy/end-to-end/EndToEndNashornSpec.groovy#L188-L199) example for how to manage method chain Mocks/Stubs.