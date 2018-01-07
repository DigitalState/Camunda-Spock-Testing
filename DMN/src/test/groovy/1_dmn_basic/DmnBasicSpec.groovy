import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.entry

// Commented out imports are shown for reference purposes
// import org.camunda.bpm.dmn.engine.DmnDecision
// import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult
// import org.camunda.bpm.dmn.engine.DmnDecisionTableResult
// import org.camunda.bpm.dmn.engine.DmnEngine
import org.camunda.bpm.dmn.engine.test.DmnEngineRule

import org.junit.Rule
// import org.junit.ClassRule

import spock.lang.*

@See([
  "https://github.com/DigitalState/Camunda-Spock-Testing",
  "https://docs.camunda.org/manual/7.8/user-guide/dmn-engine/testing/",
  "https://github.com/camunda/camunda-engine-dmn-unittest",
  "http://spockframework.org/spock/docs/1.0/data_driven_testing.html#data-tables",
  "https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/dmn/engine/test/package-summary.html"
  ])

@Narrative("""
Test a DMN with Datatables
""")

@Title("DMN Unit Testing with Datatable as variable inputs")

class DmnBasicSpec extends Specification {
  // Use ClassRule if you want the engine to only initialize once
  // ClassRule would typically be used when the @Stepwise annotation for Spock is being used.
  // If ClassRule is used then dmnEngineRule must have @Shared
  // Example:
  // @ClassRule
  // @Shared public DmnEngineRule dmnEngineRule = new DmnEngineRule();
  //
  @Rule
  public DmnEngineRule dmnEngineRule = new DmnEngineRule();

  def setupSpec(){
    println 'Starting Spec'
  }

  def 'DMN Unit Test 1'(String systemName, String personResponsible) {
    when:_ "Evaluating: '${systemName}' and expecting: '${personResponsible}'"
      def dmnEngine = dmnEngineRule.getDmnEngine()

      // Parse decision
      def dmnFile = this.class.getResource("./dmn/basic_input_output.dmn").newInputStream()
      def decision = dmnEngine.parseDecision("SystemResponsibility", dmnFile)

      def variables = [
        'systemName': systemName
      ]

      def results = dmnEngine.evaluateDecisionTable(decision, variables)

    then:_ 'DMN has only 1 result'
      assertThat(results).hasSize(1)
    
    and:_ "Output of DMN: 'personResponsible: ${personResponsible}'"
      def result = results.getSingleResult()
      assertThat(result)
        .containsOnly(
          entry("personResponsible", personResponsible)
        )

    where:
              systemName | personResponsible
            'System 123' | 'Chris'
              'System 1' | 'Chris'
              'System 2' | 'Chris'
              'System 3' | 'Chris'
          'System Alpha' | 'Frank'
      'Core System Beta' | 'Alison'
  }

  // Cleanup the deployment after the Spec has completed.
  def cleanupSpec() {
    println 'Finished Spec'
  }
}