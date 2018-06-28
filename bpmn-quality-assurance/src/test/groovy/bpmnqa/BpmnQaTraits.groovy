package bpmnqa

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.SelfType
import org.camunda.bpm.engine.impl.calendar.CycleBusinessCalendar
import org.camunda.bpm.engine.impl.calendar.DueDateBusinessCalendar
import org.camunda.bpm.engine.impl.calendar.DurationBusinessCalendar
import org.camunda.bpm.engine.impl.util.ClockUtil
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector

import static org.hamcrest.CoreMatchers.notNullValue
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

@CompileStatic
trait bpmnTimers{
    // reference: https://github.com/camunda/camunda-bpm-platform/tree/master/engine/src/main/java/org/camunda/bpm/engine/impl/calendar
    // https://docs.camunda.org/manual/7.9/reference/bpmn20/events/timer-events

//    @TODO Add support for Camunda DateTime Util to use the same date formats as a use case
//    https://github.com/camunda/camunda-bpm-platform/blob/master/engine/src/main/java/org/camunda/bpm/engine/impl/calendar/DateTimeUtil.java

    Collection<TimerEventDefinition> getTimers(){
        BpmnModelInstance model = (BpmnModelInstance)this
        Collection<TimerEventDefinition> timerEventDefinitions = model.getModelElementsByType(TimerEventDefinition.class)
        return timerEventDefinitions
    }
    TimerEventDefinition getTimerById(String activityId){
        BpmnModelInstance model = (BpmnModelInstance)this
        TimerEventDefinition timerEventDefinition = model.getModelElementsByType(TimerEventDefinition.class).find {
            it.getParentElement().getAttributeValue('id') == activityId
        }
        return timerEventDefinition
    }

    Map<String, Date> evaluateTimers(Date customCurrentTime = null) {
        BpmnModelInstance model = (BpmnModelInstance) this
        Collection<TimerEventDefinition> timerEventDefinitions = model.getModelElementsByType(TimerEventDefinition.class)
        Map<String, Date> timers = [:]
        println customCurrentTime
        if (customCurrentTime){
            setCurrentTime(customCurrentTime)
        }
        println ClockUtil.getCurrentTime()

        timerEventDefinitions.each { timer ->
            Map<String, Date> timerEval = evaluateTimer(timer)
            timers.putAll(timerEval)
        }

        if (customCurrentTime) {
            resetCurrentTime()
        }

        return timers
    }

    private void setCurrentTime(Date customCurrentTime){
        ClockUtil.setCurrentTime(customCurrentTime)
    }
    private void resetCurrentTime(){
        ClockUtil.reset()
    }

    /*
    For use with a TimerEventDefinition:
        myTimerDefinition.occurrencesByCount(10, myDate)
    This is a convenience method for getTimerOccurrencesByCount()
     */
    List<Date> occurrencesByCount(Integer count, Date customCurrentTime = null){
        TimerEventDefinition timer = (TimerEventDefinition)this
        return getTimerOccurrencesByCount(timer, count, customCurrentTime)
    }

    List<Date> getTimerOccurrencesByCount(TimerEventDefinition timer, Integer count, Date customCurrentTime = null){
        String activityId = timer.getParentElement().getAttributeValue('id')
        if (activityId == null){
            throw new IOException('Could not get Activity Id of Timer Event Definition')
        }
        if (customCurrentTime){
            setCurrentTime(customCurrentTime)
        }

        List<Date> timerOccurrences = new ArrayList<Date>()
        Date timerEvalDate = customCurrentTime

        1.upto(count, {
            Date evalResult = evaluateTimer(timer, timerEvalDate).get(activityId)
            timerEvalDate = evalResult
            timerOccurrences << evalResult
        })
        return timerOccurrences
    }

    Map<String, Date> evaluateTimer(TimerEventDefinition timer, Date customCurrentTime = null){
        Map<String,String> timerInfo = getTimerValue(timer)
        String activityId = timer.getParentElement().getAttributeValue('id')
        if (activityId == null){
            throw new IOException('Could not get Activity Id of Timer Event Definition')
        }

        if (customCurrentTime){
            setCurrentTime(customCurrentTime)
        }

        switch (timerInfo) {
            case { it['type'] == 'date'}:
                DueDateBusinessCalendar dueDateCalendar = new DueDateBusinessCalendar()
                Date dueDate = dueDateCalendar.resolveDuedate(timerInfo.value)

                if (customCurrentTime){
                    resetCurrentTime()
                }

                return [(activityId) : dueDate]
            case {it['type'] == 'cycle'}:
                CycleBusinessCalendar cycleBusinessCalendar = new CycleBusinessCalendar()
                Date cycleDueDate = cycleBusinessCalendar.resolveDuedate(timerInfo.value)

                if (customCurrentTime){
                    resetCurrentTime()
                }

                return [(activityId) : cycleDueDate]
            case {it['type'] == 'duration'}:
                DurationBusinessCalendar durationCalendar = new DurationBusinessCalendar()
                Date durationDueDate = durationCalendar.resolveDuedate(timerInfo.value)

                if (customCurrentTime){
                    resetCurrentTime()
                }

                return [(activityId) : durationDueDate]
            default:
                throw new IOException('Invalid Timer mapping found: must be of type: date or cycle or duration')
        }
    }

    Map<String, String> getTimerValue(TimerEventDefinition timer) {
        if (timer.getTimeDate() != null) {
            return [('type'):'date',
                    ('value'): timer.getTimeDate().getRawTextContent()]
        } else if (timer.getTimeCycle() != null) {
            return [('type'):'cycle',
                    ('value'): timer.getTimeCycle().getRawTextContent()]
        } else if (timer.getTimeDuration() != null) {
            return [('type'):'duration',
                    ('value'): timer.getTimeDuration().getRawTextContent()]
        } else {
            throw new IOException('Timer definition missing; Timer definition is required on all timers')
        }
    }
}