package bpmnqa

import groovy.json.JsonOutput
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector

import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.CoreMatchers.notNullValue
import static spock.util.matcher.HamcrestSupport.that
import static spock.util.matcher.HamcrestSupport.that
import static spock.util.matcher.HamcrestSupport.that

trait bpmnEvents{
    List<Map<String,Object>> getMessageEvents(String activityType = null, List <String> ignoredMessageEvents = []){
        BpmnModelInstance model = (BpmnModelInstance)this
        Collection<MessageEventDefinition> messageEventDefinitions = model.getModelElementsByType(MessageEventDefinition.class)

        if (activityType != null){
            messageEventDefinitions = messageEventDefinitions.findAll {
                it.getParentElement().getElementType().getTypeName() == activityType
            }
        }
        List<Map<String,Object>> events = messageEventDefinitions.collect {
            [
                    ('activityType') : it.getParentElement().getElementType().getTypeName(),
                    ('activityId') : it.getParentElement().getAttributeValue('id'),
                    ('eventId')  : it.getMessage()?.getId(),
                    ('eventName') : it.getMessage()?.getName(),
                    ('implementation') : [
                            ('delegateExpression') : it.getCamundaDelegateExpression(),
                            ('camundaType') : it.getCamundaType(),
                            ('expression') : it.getCamundaExpression(),
                            ('resultVariable') : it.getCamundaResultVariable(),
                            ('topic') : it.getCamundaTopic(),
                            ('operation') : it.getOperation(),
                            ('extension') : it.getExtensionElements()?.getElementsQuery()?.filterByType(CamundaConnector.class)?.singleResult()?.getElementType()?.getTypeName()
                    ]
            ]
        }
        events.removeAll{
            ignoredMessageEvents.any {it['activityId']}
        }
        println JsonOutput.prettyPrint(JsonOutput.toJson(events))
        return events
    }

    void externalEventIsConfigured(event){
//        Map<String, Object>event = (Map<String, Object>)this
            assert that(event['implementation']['topic'], notNullValue())
            assert that(event['eventId'], notNullValue())
            assert that(event['eventName'], notNullValue())
    }
}