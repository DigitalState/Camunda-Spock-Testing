package bpmnqa

import org.assertj.core.api.SoftAssertions
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import bpmnqa.bpmnEvents


class HamcrestTestsSpec extends Specification implements bpmnEvents, SomeAssertions{

  @Shared BpmnModelInstance model

  def setupSpec(){
    String path = 'bpmn/qa-test.bpmn'
    InputStream bpmnFile = this.class.getResource(path.toString()).newInputStream()
    model = Bpmn.readModelFromStream(bpmnFile).withTraits(bpmnEvents)
  }

  @Unroll
  def "Message Events Check: External Implementation"(){
//    expect:
//      externalEventIsConfigured(event)
//    verifyAll {
//      externalEventIsConfigured(event)
//    }
//      verifyAll {
//        that(event['implementation']['topic'], notNullValue())
//        that(event['eventId'], notNullValue())
//        that(event['eventName'], notNullValue())
//      }
//    println 'event--->'
//    println event['implementation']['topic']
    //      softly.assertThat(event['implementation']['topic']).isNotNull()
//      softly.assertThat(event['eventId']).isNotNull()
//      softly.assertThat(event['eventName']).isNotNull()
//    SoftAssertions.assertSoftly { softly ->
//      softly.assertThat(dog).withFailMessage("Dog cannot be null").isNotNull()
//      softly.assertThat(cat).withFailMessage("cat cannot be null").isNotNull()
//    }

    expect:
        evalSomeAssertions()


//   where:
//   dog | cat
//      "1" | "1"
//    null | null

//   event << model.getMessageEvents().findAll { it['implementation']['camundaType'] == 'external'}
//   event << model.getMessageEvents('endEvent')
  }

}

trait SomeAssertions{
  def evalSomeAssertions(){
    verifyAll {
      1 == 2
      2 == 3
      4 == 5
    }

  }
}