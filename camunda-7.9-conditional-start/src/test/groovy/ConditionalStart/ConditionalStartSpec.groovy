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

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance.ExtensionElements
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaScript
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener


@Title("Conditional Start Testing for Scripting with Camunda 7.9")

class ConditionalStartSpec extends Specification {

  // Use when you want to run the Camunda Engine
  @ClassRule
  @Shared public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  
  @Shared ScriptEngine engine = new ScriptEngineManager().getEngineByName('nashorn')
  @Shared String deploymentId
  @Shared String executionId
  @Shared String processDefinitionId
  @Shared reportData = [:]

  // helper method to shorten the .addInputStream params in createDeploment()
  def resourceStream(path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def addExecutionListener(model, elementId, scriptResource, scriptFormat){
    // @TODO NOTE: The estLis had to be new for every instance
    CamundaExecutionListener extLis = model.newInstance(CamundaExecutionListener.class);
    CamundaScript camScript = model.newInstance(CamundaScript.class);
    camScript.setCamundaResource(scriptResource)
    camScript.setCamundaScriptFormat(scriptFormat)
    extLis.setCamundaEvent('take')
    extLis.setCamundaScript(camScript)

    def newModel = model.getModelElementById(elementId).builder().addExtensionElement(extLis).done()
    return newModel
  }

  def setupSequenceFlowListeners(model, scriptResource, scriptFormat){

    def sequenceFlows = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SequenceFlow.class).collect {it.getId()}

    def newModel = model
    sequenceFlows.each {
      newModel = addExecutionListener(newModel, it, scriptResource, scriptFormat)
    }
    return newModel
  }

  def setupSpec(){
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/repository/DeploymentBuilder.html
    def baseModel = Bpmn.readModelFromStream(resourceStream('bpmn/ConditionalStart/conditionalStart.bpmn'))
    def preppedModel = setupSequenceFlowListeners(baseModel, 'deployment://flownode.js', 'javascript')
    reportData['bpmnModel'] = Bpmn.convertToString(preppedModel)
    def deployment = repositoryService().createDeployment()
                                        .addModelInstance('conditionalStart.bpmn', preppedModel)
                                        // .addInputStream('conditionalStart.bpmn', resourceStream('bpmn/ConditionalStart/conditionalStart.bpmn'))
                                        .addInputStream('script1.js', resourceStream('bpmn/ConditionalStart/script1.js'))
                                        .addInputStream('script2.js', resourceStream('bpmn/ConditionalStart/script2.js'))
                                        .addInputStream('flownode.js', resourceStream('bpmn/ConditionalStart/flownode.js'))
                                        .name('ConditionalStart')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }

  def 'Interactive Spec to show how to output data from the process'() {
    when: 'Starting the process instance'
      // reportInfo "DOGGGs"
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
      println processInstance.size()
      executionId = processInstance[0].getId()
      processDefinitionId = processInstance[0].getProcessDefinitionId()

    then: 'Process is Active and waiting for user task completion'
      assertThat(processInstance[0]).isActive()
      
      // processInstanceQuery() is being exposed as part of:
      // http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
      // See import statement at top of test
      assertThat(processInstanceQuery().count()).isEqualTo(1)

    then: 'Process Variables are:'
    def processVariables = runtimeService().getVariables(executionId)
    println processVariables.inspect()
    // The .inspect() method is a special groovy method that
    // will print out the object:
    // http://docs.groovy-lang.org/latest/html/api/groovy/inspect/Inspector.html

    // You can uncomment these lines if you like, but is not needed.

    then: 'Complete Final User Task (The placeholder user task)'
      assertThat(processInstance[0]).isWaitingAt("Task_0qacez5")
      complete(task(processInstance[0]))
    
    then: 'Process has ended'
      assertThat(processInstance[0]).isEnded()


  }

  def cleanup(){
          // Get Actvitity Events
    def events = historyService().createHistoricActivityInstanceQuery().processInstanceId(executionId).orderPartiallyByOccurrence().asc().list().collect {it.activityId}
    println '\nExecuted Activity Events:'
    println events
    reportData['activityInstances'] = events

    println '\nAsync Element Configs:'
    def model = repositoryService().getBpmnModelInstance(processDefinitionId)
    def asyncData = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.FlowNode.class).collect {[
                                                                                                        'id': it.getId(),
                                                                                                        'asyncBefore': it.isCamundaAsyncBefore(),
                                                                                                        'asyncAfter': it.isCamundaAsyncAfter(),
                                                                                                        'exclusive': it.isCamundaExclusive()
                                                                                                        ]}
    reportData['asyncData'] = asyncData
    println asyncData

    println '\nUser Tasks:'
    def userTasks =  model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.UserTask.class).collect {it.getId()}
    println userTasks
    reportData['userTasks'] = userTasks
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)

    reportInfo(reportData)
  }

  def cleanupSpec() {

    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "\nDeployment ID: '${deploymentId}' has been deleted"

  }

}