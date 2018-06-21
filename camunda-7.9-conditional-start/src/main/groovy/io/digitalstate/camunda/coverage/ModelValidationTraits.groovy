package io.digitalstate.camunda.coverage

import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.SequenceFlow

// WARNING: Work in progress.  Not currently functional pattern

trait bpmnSequenceFlows {
//    https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/?org/camunda/bpm/model/bpmn/instance/package-summary.html
    ArrayList getSequenceFlowsSourceTargetList(Map<String,String> ignoredSequenceFlows = [:]){
        BpmnModelInstance model = (BpmnModelInstance)this
        Collection<SequenceFlow> sequenceFlows = model.getModelElementsByType(SequenceFlow.class)
        ArrayList targetSource = sequenceFlows.collect {
            [(it.getSource().getId()) : it.getTarget().getId()]
        }
        targetSource.removeAll(ignoredSequenceFlows)
        return targetSource
    }

    void assertNoDuplicateSequenceFlows(Map<String,String> ignoredSequenceFlows = [:]){
        BpmnModelInstance model = (BpmnModelInstance)this
        assertThat(model.getSequenceFlowsSourceTargetList(ignoredSequenceFlows)).doesNotHaveDuplicates()
    }
}

trait bpmnMessageEvents {
    List<Map<String,String>> getintermediateCatchEvents(List<String> ignoredMessageEvents = []){
        BpmnModelInstance model = (BpmnModelInstance)this
        Collection<IntermediateCatchEvent> intermediateCatchEvents = model.getModelElementsByType(IntermediateCatchEvent.class)

        List<Map<String,String>> catchEvents = intermediateCatchEvents.collect {
             [
               ('activityId') : it.getId(),
               ('eventId')  : it.getEventDefinitions().getAt(0).getMessage().getId(),
               ('eventName'): it.getEventDefinitions().getAt(0).getMessage().getName()
            ]
        }
        catchEvents.removeAll{
            ignoredMessageEvents.any {it['activityId']}
        }
        println catchEvents
        return catchEvents
    }

    void assertIntermediateCatchEventsAreConfigured(List<String> ignoredMessageEvents = []){
        BpmnModelInstance model = (BpmnModelInstance)this
        List<Map<String, String>> events = model.getintermediateCatchEvents(ignoredMessageEvents)
    }
}