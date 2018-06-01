// import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/


// import org.junit.Rule
import org.junit.ClassRule

import spock.lang.*

import static org.camunda.spin.Spin.*

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


@Title("Conditional Start Testing for Scripting with Camunda 7.9")

class ConditionalStartSpec extends Specification {

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
                                        .addInputStream('end-to-end.bpmn', resourceStream('bpmn/ConditionalStart/conditionalStart.bpmn'))
                                        .addInputStream('script1.js', resourceStream('bpmn/ConditionalStart/script1.js'))
                                        .addInputStream('script2.js', resourceStream('bpmn/ConditionalStart/script2.js'))
                                        .name('ConditionalStart')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def 'Interactive Spec to show how to output data from the process'() {
    when:_ 'Starting the process instance'

      //@TODO Move variable creation and management into its own class.
      def json = S("{\"customer\": \"Kermit\"}")
      def startingVariables = [
                                'json':json,
                                'temp': 24
                                ]
      // we assume only one process instance will be returned
      def processInstance = runtimeService().createConditionEvaluation()
                                      .setVariables(startingVariables)
                                      .evaluateStartConditions();

      executionId = processInstance[0].getId()

    then:_ 'Process is Active and waiting for user task completion'
      assertThat(processInstance[0]).isActive()
      
      // processInstanceQuery() is being exposed as part of:
      // http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
      // See import statement at top of test
      assertThat(processInstanceQuery().count()).isEqualTo(1)

    then:_ 'Process Variables are:'
    def processVariables = runtimeService().getVariables(executionId)
    println processVariables.inspect()
    // The .inspect() method is a special groovy method that
    // will print out the object:
    // http://docs.groovy-lang.org/latest/html/api/groovy/inspect/Inspector.html

    // You can uncomment these lines if you like, but is not needed.

    // then:_ 'Complete Final User Task (The placeholder user task)'
    //   assertThat(processInstance).isWaitingAt("Task_0qacez5")
    //   complete(task(processInstance))
    
    // then:_ 'Process has ended'
    //   assertThat(processInstance).isEnded()


  }

  def cleanupSpec() {
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "Deployment ID: '${deploymentId}' has been deleted"

  }

}