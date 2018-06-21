package io.digitalstate.camunda.coverage

import groovy.json.JsonOutput
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent
import org.camunda.bpm.model.bpmn.instance.ReceiveTask
import org.camunda.bpm.model.bpmn.instance.SequenceFlow
import org.camunda.bpm.model.bpmn.instance.UserTask
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaScript
import org.camunda.bpm.model.bpmn.instance.FlowNode

class BpmnCoverageBuilder {
//    ProcessEngine processEngine
//
//    BpmnCoverage(processEngine){
//        this.processEngine = processEngine
//    }

    // helper method to shorten the .addInputStream params in createDeployment()
    static InputStream resourceStream(String path){
        return this.getClassLoader().getResource(path.toString()).newInputStream()
    }

    static InputStream sequenceFlowHistoryGenerator(){
        return resourceStream("io/digitalstate/camunda/coverage/${sequenceFlowHistoryFileName()}")
    }
    static String sequenceFlowHistoryFileName(){
        return "sequenceFlowHistoryEventGenerator.js"
    }

    static BpmnModelInstance prepModelForCoverage( String modelPath,
                                                   String scriptResource = 'deployment://sequenceFlowHistoryEventGenerator.js',
                                                   String scriptFormat = 'javascript') {

       InputStream resource = resourceStream(modelPath)
       BpmnModelInstance model = Bpmn.readModelFromStream(resource)
       BpmnModelInstance preppedModel = setupSequenceFlowListeners(model, scriptResource, scriptFormat)
       return preppedModel
    }

    private static BpmnModelInstance addExecutionListener(BpmnModelInstance model, String elementId, String scriptResource, String scriptFormat){
        CamundaExecutionListener extLis = model.newInstance(CamundaExecutionListener.class)
        CamundaScript camScript = model.newInstance(CamundaScript.class)
        camScript.setCamundaResource(scriptResource)
        camScript.setCamundaScriptFormat(scriptFormat)
        extLis.setCamundaEvent('take')
        extLis.setCamundaScript(camScript)

        BpmnModelInstance newModel = model.getModelElementById(elementId).builder().addExtensionElement(extLis).done()
        return newModel
    }

    private static BpmnModelInstance setupSequenceFlowListeners(BpmnModelInstance model, String scriptResource, String scriptFormat){
        def sequenceFlows = model.getModelElementsByType(SequenceFlow.class).collect {it.getId()}
        BpmnModelInstance newModel = model
        sequenceFlows.each {
            newModel = addExecutionListener(newModel, it, scriptResource, scriptFormat)
        }
        return newModel
    }

    static generateCoverageData(ProcessEngine processEngine, ProcessInstance processInstance, String coverageDataName = null, Integer coverageDataWeight = 0){
        CoverageData coverageData = new CoverageData()
        coverageData.name = coverageDataName
        coverageData.weight = coverageDataWeight

        String processInstanceId = processInstance.getProcessInstanceId()
        def reportData = [:]

        // Get Activity Events
        def events = processEngine.getHistoryService()
                                    .createHistoricActivityInstanceQuery()
                                    .processInstanceId(processInstanceId)
                                    .finished()
                                    .orderPartiallyByOccurrence()
                                    .asc()
                                    .list()
        def activityEvents = events.findAll {it.activityType != 'sequenceFlow' && it.activityType != 'multiInstanceBody'}
                                    .collect {it.activityId}
                                    .countBy {it}
//        reportData['activityInstances'] = JsonOutput.toJson(activityEvents)
        coverageData.activityInstancesFinished = activityEvents

        // Activity Events That are still active
        def eventsStillActive = processEngine.getHistoryService()
                                                .createHistoricActivityInstanceQuery()
                                                .processInstanceId(processInstanceId)
                                                .unfinished()
                                                .orderPartiallyByOccurrence()
                                                .asc()
                                                .list()
        def activityEventsStillActive = eventsStillActive.findAll {it.activityType != 'sequenceFlow'}
                                                            .collect {it.activityId}
//        reportData['activityInstancesStillActive'] = JsonOutput.toJson(activityEventsStillActive)
        coverageData.activityInstancesUnfinished = activityEventsStillActive

        def sequenceFlows = events.findAll  {it.activityType == 'sequenceFlow'}
                                    .collect {it.activityId}
//        reportData['executedSequenceFlows'] = JsonOutput.toJson(sequenceFlows)
        coverageData.sequenceFlowsFinished = sequenceFlows

        String processDefinitionId = processEngine.getHistoryService()
                                                    .createHistoricProcessInstanceQuery()
                                                    .processInstanceId(processInstanceId)
                                                    .singleResult()
                                                    .getProcessDefinitionId()

        def model = processEngine.getRepositoryService()
                                    .getBpmnModelInstance(processDefinitionId)

        def asyncData = model.getModelElementsByType(FlowNode.class).collect {[
                                                                                'id': it.getId(),
                                                                                'asyncBefore': it.isCamundaAsyncBefore().toBoolean(),
                                                                                'asyncAfter': it.isCamundaAsyncAfter().toBoolean(),
                                                                                'exclusive': it.isCamundaExclusive().toBoolean()
                                                                                 ]}
//        reportData['asyncData'] = JsonOutput.toJson(asyncData)
        coverageData.modelAsyncData = asyncData

        def userTasks =  model.getModelElementsByType(UserTask.class).collect {it.getId()}
//        reportData['userTasks'] = JsonOutput.toJson(userTasks)
        coverageData.modelUserTasks = userTasks

        def receiveTasks =  model.getModelElementsByType(ReceiveTask.class).collect {it.getId()}
//        reportData['receiveTasks'] = JsonOutput.toJson(receiveTasks)
        coverageData.modelReceiveTasks = receiveTasks

        def intermediateCatchEvents =  model.getModelElementsByType(IntermediateCatchEvent.class).collect {it.getId()}
//        reportData['intermediateCatchEvents'] = JsonOutput.toJson(intermediateCatchEvents)
        coverageData.modelIntermediateCatchEvents = intermediateCatchEvents

//        reportData['bpmnModel'] = Bpmn.convertToString(model).replaceAll("[\n\r]", "")
        coverageData.bpmnModel = Bpmn.convertToString(model).replaceAll("[\n\r]", "")
        return coverageData
//        return reportData
    }
}
