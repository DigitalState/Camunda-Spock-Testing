package conditionalstart

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance

trait BpmnFluentBuilder {

    BpmnModelInstance model1(){
        BpmnModelInstance model = Bpmn.createExecutableProcess('model')
        .startEvent()
        .scriptTask()
            .name('Some Simple Script')
            .scriptFormat('javascript')
            .camundaResource('deployment://fluentModelScript1.js')
        .userTask()
            .name('placeholder')
        .endEvent()
        .done()

        return model
    }

    BpmnModelInstance model2(){
        BpmnModelInstance model = Bpmn.createExecutableProcess('model')
                .name("Reminder Demo")
                .startEvent()
                .userTask('readEmail')
                    .boundaryEvent()
                        .timerWithDuration("PT1H")
                        .cancelActivity(false)
                        .manualTask()
                            .name('do something')
                        .endEvent()
                        .moveToActivity('readEmail')
                .boundaryEvent()
                    .timerWithCycle("R3/PT10M")
                    .manualTask()
                        .name('do something else')
                    .endEvent()
                    .moveToActivity('readEmail')
                .endEvent()
                .done()
        return model
    }

}