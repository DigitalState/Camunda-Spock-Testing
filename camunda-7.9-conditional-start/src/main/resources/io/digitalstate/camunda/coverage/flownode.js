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

// https://docs.camunda.org/javadoc/camunda-bpm-platform/7.9/org/camunda/bpm/engine/impl/history/handler/DbHistoryEventHandler.html
var historyHandler = Java.type('org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler');
(new historyHandler).handleEvent(historicActivityInstance)