import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

import org.junit.ClassRule;

import spock.lang.*

@Narrative("""
Show how a custom deployment is created
using the Deployment Builder provided
in the Camunda Java API
""")

@Title('Camunda Deployment Builder Test Process')

class CustomDeploySpec extends Specification {

  @ClassRule
  @Shared public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml');

  @Shared deploymentId

  // helper method to shorten the .addInputStream params in createDeploment()
  def resourceStream(path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def setupSpec(){
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/repository/DeploymentBuilder.html
    def deployment = repositoryService().createDeployment()
                                        .addInputStream('customDeploy.bpmn', resourceStream('bpmn/9_CustomDeploy/customDeploy.bpmn'))
                                        .name('CustomDeployExample')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  // Cleanup the deployment after the Spec has completed.
  def cleanupSpec() {
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "Deployment ID: '${deploymentId}' has been deleted"
  }


  def 'Custom Deploy Test'() {
    when:_ 'Starting Process Instance'
      def processInstance = runtimeService().startProcessInstanceByKey('customDeploy')
      println processInstance.getProcessDefinitionId()

    then:_ 'Process is active'
      assertThat(processInstance).isActive()

    and:_ 'only 1 instance is running'
      assertThat(processInstanceQuery().count()).isEqualTo(1)

    and:_ 'there is a active task'
      assertThat(task(processInstance)).isNotNull()

    and:_ 'We complete the task'
      complete(task(processInstance))

    and:_ 'The process has ended'
      assertThat(processInstance).isEnded()
  }
}