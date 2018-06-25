package bpmnqa

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance

import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static bpmnqa.bpmnEvents.externalEventIsConfigured
import static spock.util.matcher.HamcrestSupport.that
import static spock.util.matcher.HamcrestSupport.expect
import static org.hamcrest.CoreMatchers.*
import bpmnqa.bpmnEvents
import static bpmnqa.bpmnEvents.*


class HamcrestTestsSpec extends Specification implements bpmnEvents{

  @Shared BpmnModelInstance model

  def setupSpec(){
    String path = 'bpmn/qa-test.bpmn'
    InputStream bpmnFile = this.class.getResource(path.toString()).newInputStream()
    model = Bpmn.readModelFromStream(bpmnFile).withTraits(bpmnEvents)
  }

  @Unroll
  def "Message Events Check: External Implementation"(){
    expect:
//      externalEventIsConfigured(event)
    verifyAll {
      externalEventIsConfigured(event)
    }
//      verifyAll {
//        that(event['implementation']['topic'], notNullValue())
//        that(event['eventId'], notNullValue())
//        that(event['eventName'], notNullValue())
//      }
   where:
   event << model.getMessageEvents().findAll { it['implementation']['camundaType'] == 'external'}
//   event << model.getMessageEvents('endEvent')
  }

}

