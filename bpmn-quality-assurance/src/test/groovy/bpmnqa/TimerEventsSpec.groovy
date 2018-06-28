package bpmnqa

import bpmnqa.bpmnTimers
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition
import spock.lang.Shared
import spock.lang.Specification

import java.text.SimpleDateFormat

class TimerEventsSpec extends Specification implements bpmnTimers{

  @Shared BpmnModelInstance model
  @Shared SimpleDateFormat dateF = new SimpleDateFormat("yyyy MM dd - HH:mm")
  @Shared SimpleDateFormat dateBiz = new SimpleDateFormat('E MMM FF HH:mm:ss zzz yyyy')

  def setupSpec(){
    String path = 'bpmn/qa-test.bpmn'
    InputStream bpmnFile = this.class.getResource(path.toString()).newInputStream()
    model = Bpmn.readModelFromStream(bpmnFile).withTraits(bpmnTimers)
  }

  def 'Start Event Cycle Cron Test'(Date customStartTime, String expectedResultTime){
    when:'Given a Timer Start Event that we eval with a custom start date of #customStartTime'
      String activityId = 'StartEvent_0ii048j'
      TimerEventDefinition timerEvent = model.getTimerById(activityId)
      Map<String,Date> result = evaluateTimer(timerEvent, customStartTime)

    then:'The timer should have a due date set to #execptedResultTime'
      assert result.get(activityId).toString() == expectedResultTime

    where:
    customStartTime                   || expectedResultTime
    dateF.parse('2011 01 01 - 00:00') || 'Sat Jan 01 01:00:00 EST 2011'
    dateF.parse('2011 01 01 - 04:49') || 'Sat Jan 01 05:00:00 EST 2011'
    dateF.parse('2011 01 01 - 05:01') || 'Sat Jan 01 10:00:00 EST 2011'
    dateF.parse('2011 01 01 - 09:59') || 'Sat Jan 01 10:00:00 EST 2011'
    dateF.parse('2011 01 01 - 14:30') || 'Sat Jan 01 15:00:00 EST 2011'
    dateF.parse('2011 01 01 - 19:30') || 'Sat Jan 01 20:00:00 EST 2011'
    dateF.parse('2011 01 01 - 21:00') || 'Tue Feb 01 01:00:00 EST 2011'
  }

  def 'Start Event Cycle Cron Test - Timer Occurrences'(){
    when:'Given a Timer Start Event that we get the 10 sequential occurrences'
      String activityId = 'StartEvent_0ii048j'
      TimerEventDefinition timerEvent = model.getTimerById(activityId)
//    Alternate Usage using occurrencesByCount():
//    timerEvent = timerEvent.withTraits(bpmnTimers)
//    timerEvent.occurrencesByCount(10)
      Date customStartTime = dateF.parse('2011 01 01 - 00:00')
      List<Date> timerOccurrences = getTimerOccurrencesByCount(timerEvent, 10, customStartTime)

    and: 'a expected set of dates are established'
      List<Date> expectedTimerOccurrences = ['Sat Jan 01 01:00:00 EST 2011',
                                            'Sat Jan 01 05:00:00 EST 2011',
                                            'Sat Jan 01 10:00:00 EST 2011',
                                            'Sat Jan 01 15:00:00 EST 2011',
                                            'Sat Jan 01 20:00:00 EST 2011',
                                            'Tue Feb 01 01:00:00 EST 2011',
                                            'Tue Feb 01 05:00:00 EST 2011',
                                            'Tue Feb 01 10:00:00 EST 2011',
                                            'Tue Feb 01 15:00:00 EST 2011',
                                            'Tue Feb 01 20:00:00 EST 2011'].collect {dateBiz.parse(it)}

    then:'The timer should generate timer events for the expected set of timer dates'
//      assert timerOccurrences == expectedTimerOccurrences

        // Use string comparison to show more detailed error to more easily
        // identify where the error was in the list
      assert timerOccurrences.toString() == expectedTimerOccurrences.toString()
  }

}