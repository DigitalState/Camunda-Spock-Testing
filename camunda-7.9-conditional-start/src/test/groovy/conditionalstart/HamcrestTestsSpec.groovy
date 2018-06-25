package conditionalstart

import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.hamcrest.Matchers
import org.hamcrest.collection.IsMapContaining
import org.hamcrest.core.Every
import org.hamcrest.core.IsNot
import org.hamcrest.core.StringContains
import org.junit.Assert
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import io.digitalstate.camunda.coverage.bpmnSequenceFlows
import io.digitalstate.camunda.coverage.bpmnMessageEvents

import java.util.stream.Collectors

import static io.digitalstate.camunda.coverage.BpmnCoverageBuilder.*
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*

@Ignore
class HamcrestTestsSpec extends Specification{

  @Shared BpmnModelInstance model

  def setupSpec(){
    model = prepModelForCoverage('bpmn/conditionalstart/HamcrestTests.bpmn').withTraits(bpmnSequenceFlows, bpmnMessageEvents)
  }

  def 'Redundant Sequence Flows'() {
    when: 'Given a Model Definition'
    then: 'Ensure there  are no redundant sequence flows'
//      ArrayList sequenceFlows = model.getSequenceFlowsSourceTargetList()
//      ArrayList sequenceFlows = model.getSequenceFlowsSourceTargetList(["StartEvent_1ugw7xf":"Task_1qb2k3a"])
//     assertThat(sequenceFlows).doesNotHaveDuplicates()
      model.assertNoDuplicateSequenceFlows(["StartEvent_1ugw7xf":"Task_1qb2k3a"])
  }

  def "Message Events"(){
    when: 'Given a Model Definition'
    then: 'Ensure all Message Events have proper configurations'
      List<Map<String, String>> catchEvents = model.getintermediateCatchEvents()
//      assertThat (catchEvents).extracting()
//      model.assertIntermediateCatchEventsAreConfigured()
      assertThat(catchEvents).allSatisfy {elm ->
        assertThat(elm).hasNoNullFieldsOrProperties()
      }

  }
}

