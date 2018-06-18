package conditionalstart

// import org.camunda.bpm.engine.runtime.ProcessInstance
//import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.model.bpmn.BpmnModelInstance

//brings in Camunda BPM Assertion + AssertJ core.api.Assertions
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
// http://joel-costigliola.github.io/assertj/core/api/index.html
// http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
// http://joel-costigliola.github.io/assertj/


// import org.junit.Rule
import org.junit.ClassRule

import spock.lang.*

import static org.camunda.spin.Spin.*
import groovy.json.JsonOutput

//import javax.script.ScriptEngine
//import javax.script.ScriptEngineManager

import org.camunda.bpm.model.bpmn.Bpmn
//import org.camunda.bpm.model.bpmn.instance.ExtensionElements
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaScript
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener


// @Title("Conditional Start Testing for Scripting with Camunda 7.9")
@Narrative("""
To test Conditional Start Events,
that were apart of Camunda 7.9 release,
to determine if there are implications for scaling.
""")
@See(["forum.camunda.org", "forum.bpmn.io", "See Spec document approved by Committee (Spec001-v4-FINAL.docx)"])
@Issue(["github.com/stephenott", "CAM-1234"])
@Title('Test Conidition Start Event Usage')
@Ignore
class ConditionalStartSpec extends Specification {

  // Use when you want to run the Camunda Engine
  @ClassRule
  @Shared public ProcessEngineRule processEngineRule = new ProcessEngineRule('camunda_config/camunda.cfg.xml')
  
  // @Shared ScriptEngine engine = new ScriptEngineManager().getEngineByName('nashorn')
  @Shared String deploymentId
  @Shared String executionId
  @Shared String processDefinitionId
  @Shared reportData = [:]

  // helper method to shorten the .addInputStream params in createDeployment()
  def resourceStream(String path){
    return this.class.getResource(path.toString()).newInputStream()
  }

  def addExecutionListener(BpmnModelInstance model, String elementId, String scriptResource, String scriptFormat){
    CamundaExecutionListener extLis = model.newInstance(CamundaExecutionListener.class)
    CamundaScript camScript = model.newInstance(CamundaScript.class)
    camScript.setCamundaResource(scriptResource)
    camScript.setCamundaScriptFormat(scriptFormat)
    extLis.setCamundaEvent('take')
    extLis.setCamundaScript(camScript)

    BpmnModelInstance newModel = model.getModelElementById(elementId).builder().addExtensionElement(extLis).done()
    return newModel
  }

  def setupSequenceFlowListeners(BpmnModelInstance model, String scriptResource, String scriptFormat){

    def sequenceFlows = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.SequenceFlow.class).collect {it.getId()}
    BpmnModelInstance newModel = model
    sequenceFlows.each {
      newModel = addExecutionListener(newModel, it, scriptResource, scriptFormat)
    }
    return newModel
  }

  def setupSpec(){
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/repository/DeploymentBuilder.html
    BpmnModelInstance baseModel = Bpmn.readModelFromStream(resourceStream('bpmn/conditionalstart/conditionalStart.bpmn'))
    BpmnModelInstance preppedModel = setupSequenceFlowListeners(baseModel, 'deployment://flownode.js', 'javascript')
    reportData['bpmnModel'] = Bpmn.convertToString((BpmnModelInstance)preppedModel)
    def deployment = repositoryService().createDeployment()
                                        .addModelInstance('conditionalStart.bpmn', preppedModel)
                                        // .addInputStream('conditionalStart.bpmn', resourceStream('bpmn/conditionalstart/conditionalStart.bpmn'))
                                        .addInputStream('script1.js', resourceStream('bpmn/conditionalstart/script1.js'))
                                        .addInputStream('script2.js', resourceStream('bpmn/conditionalstart/script2.js'))
                                        .addInputStream('flownode.js', resourceStream('bpmn/conditionalstart/flownode.js'))
                                        .name('conditionalstart')
                                        .enableDuplicateFiltering(false)
                                        .deploy()

    deploymentId = deployment.getId()
    println "Deployment ID: '${deploymentId}' has been created"
  }


  @See(["forum.camunda.org/123/123", "Document: abc123.docx on shared drive"])
  @Issue(["https://github.com/DigitalState/Camunda-Spock-Testing/issues/1", "https://github.com/DigitalState/Camunda-Spock-Testing/issues/2"])
  def 'Interactive Spec to show how to output data from the process'() {
    when: 'Setting up initial Process Variables'
      def json = S("{\"customer\": \"Kermit\"}")
      def startingVariables = [
                                'json':json,
                                'temp': 24,
                                'path1': false,
                                'path2': false
                                ]

      // we assume only one process instance will be returned
    and: 'Starting a process instance with Conditional Evaluation using the process variables '
      def processInstance = runtimeService().createConditionEvaluation()
                                      .setVariables(startingVariables)
                                      .evaluateStartConditions()
      executionId = processInstance[0].getId()
      processDefinitionId = processInstance[0].getProcessDefinitionId()

    then: 'Process is Active and waiting for user task completion'
      assertThat(processInstance[0]).isActive()
      
      // processInstanceQuery() is being exposed as part of:
      // http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
      // See import statement at top of test
      // assertThat(processInstanceQuery().count()).isEqualTo(1)

    then: 'The current process variables are equal to the starting variables'
    def processVariables = runtimeService().getVariables(executionId)
    assertThat(processVariables == startingVariables)
//    println processVariables.inspect()
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

@Issue("https://github.com/DigitalState/Camunda-Spock-Testing/issues/4")
// @Unroll
def 'Second feature to show missing coverage'( boolean path1, boolean path2) {
    when: 'Starting the process instance'
      def json = S("{\"customer\": \"Kermit\"}")
      def startingVariables = [
                                'json':json,
                                'temp': 24,
                                'path1': path1,
                                'path2': path2
                                ]
      // we assume only one process instance will be returned
      def processInstance = runtimeService().createConditionEvaluation()
                                      .setVariables(startingVariables)
                                      .evaluateStartConditions()
//      println processInstance.size()
      executionId = processInstance[0].getId()
      processDefinitionId = processInstance[0].getProcessDefinitionId()

    then: 'Process is Active and waiting for user task completion'
      assertThat(processInstance[0]).isActive()
      
      // processInstanceQuery() is being exposed as part of:
      // http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html
      // See import statement at top of test
      // assertThat(processInstanceQuery().count()).isEqualTo(1)

    then: 'Process Variables are:'
    def processVariables = runtimeService().getVariables(executionId)
//    println processVariables.inspect()
    // The .inspect() method is a special groovy method that
    // will print out the object:
    // http://docs.groovy-lang.org/latest/html/api/groovy/inspect/Inspector.html

    // You can uncomment these lines if you like, but is not needed.

    // then: 'Complete Final User Task'
    //   // assertThat(processInstance[0]).isWaitingAt("Task_0qacez5")
    //   complete(task(processInstance[0]))
    
    // then: 'Process has ended'
      // assertThat(processInstance[0]).isEnded()

    where:
        path1 | path2 
        false | false // Default Flow
        false | false
        true  | false // "Do something" Flow
        false | true  // "Do something else" Flow
  }




  def cleanup(){
    // Get Actvitity Events
    def events = historyService().createHistoricActivityInstanceQuery().processInstanceId(executionId).finished().orderPartiallyByOccurrence().asc().list()
    def activityEvents = events.findAll{it.activityType != 'sequenceFlow' && it.activityType != 'multiInstanceBody'}.collect {it.activityId}.countBy {it}
//    println '\nExecuted Activity Events:'
//    println activityEvents
    reportData['activityInstances'] = JsonOutput.toJson(activityEvents)
//    println reportData['activityInstances']

    // Activity Events That are still active
    def eventsStillActive = historyService().createHistoricActivityInstanceQuery().processInstanceId(executionId).unfinished().orderPartiallyByOccurrence().asc().list()
    def activityEventsStillActive = eventsStillActive.findAll{it.activityType != 'sequenceFlow'}.collect {it.activityId}
//    println '\nStill Active Activity Events:'
//    println activityEventsStillActive
    reportData['activityInstancesStillActive'] = JsonOutput.toJson(activityEventsStillActive)


    def sequenceFlows = events.findAll{it.activityType == 'sequenceFlow'}.collect {it.activityId}
//    println '\nExecuted Sequence Flows:'
//    println sequenceFlows
    reportData['executedSequenceFlows'] = JsonOutput.toJson(sequenceFlows)


//    println '\nAsync Element Configs:'
    def model = repositoryService().getBpmnModelInstance(processDefinitionId)
    def asyncData = model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.FlowNode.class).collect {[
                                                                                                        'id': it.getId(),
                                                                                                        'asyncBefore': it.isCamundaAsyncBefore(),
                                                                                                        'asyncAfter': it.isCamundaAsyncAfter(),
                                                                                                        'exclusive': it.isCamundaExclusive()
                                                                                                        ]}
    reportData['asyncData'] = JsonOutput.toJson(asyncData)
//    println asyncData

//    println '\nUser Tasks:'
    def userTasks =  model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.UserTask.class).collect {it.getId()}
//    println userTasks
    reportData['userTasks'] = JsonOutput.toJson(userTasks)
    
//    println '\nReceive Tasks:'
    def receiveTasks =  model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.ReceiveTask.class).collect {it.getId()}
//    println receiveTasks
    reportData['receiveTasks'] = JsonOutput.toJson(receiveTasks)
    
//    println '\nIntermediate Catch Events:'
    def intermediateCatchEvents =  model.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent.class).collect {it.getId()}
//    println intermediateCatchEvents
    reportData['intermediateCatchEvents'] = JsonOutput.toJson(intermediateCatchEvents)
    


    reportInfo(reportData.clone())
  }

  def cleanupSpec() {
    // https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/RepositoryService.html#deleteDeployment(java.lang.String,%20boolean,%20boolean,%20boolean)
    repositoryService().deleteDeployment(deploymentId, 
                                                 true, // cascade
                                                 true, // skipCustomListeners
                                                 true) // skipIoMappings
    println "\nDeployment ID: '${deploymentId}' has been deleted"

  }

}