package conditionalstart

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance

trait BpmnFluentBuilder {

    BpmnModelInstance model1(){
        BpmnModelInstance model = Bpmn.createExecutableProcess('model1')
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

}