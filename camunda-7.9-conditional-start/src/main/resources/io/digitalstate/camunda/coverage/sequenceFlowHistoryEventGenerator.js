var Date = Java.type("java.util.Date")

// https://docs.camunda.org/javadoc/camunda-bpm-platform/7.9/org/camunda/bpm/engine/impl/history/event/HistoricActivityInstanceEventEntity.html
var HistoricActivityInstanceEventEntity = Java.type('org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity')

var historicActivityInstance = new HistoricActivityInstanceEventEntity()

historicActivityInstance.setActivityId(execution.getCurrentTransitionId())
historicActivityInstance.setExecutionId(execution.getId())
historicActivityInstance.setActivityType('sequenceFlow')
historicActivityInstance.setStartTime(new Date())
historicActivityInstance.setEndTime(new Date())
historicActivityInstance.setDurationInMillis(0)
historicActivityInstance.setProcessDefinitionId(execution.getProcessDefinitionId())
historicActivityInstance.setProcessInstanceId(execution.getProcessInstanceId())

var historyHandler = execution.getProcessEngineServices().getProcessEngineConfiguration().getHistoryEventHandler()
historyHandler.handleEvent(historicActivityInstance)