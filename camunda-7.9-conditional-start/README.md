# Conditional Start Event using Camunda 7.9

This is a example showing how to test against Conditional Start Events and using scripting to eval the variables submitted as part of the Conditional Start Event evaluation.

# How to Use:

Root into folder and run: `./mvnw clean test`

Test will run.

You can update the BPMN file in src/test/resources/bpmn/conditionalstart/conditionalStart.bpmn



# Console output:

```console
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running ConditionalStartSpec
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
Jun 01, 2018 2:53:04 PM org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
INFO: Loading XML bean definitions from class path resource [camunda_config/camunda.cfg.xml]
Deployment ID: '1' has been created
Starting the process instance
{
  temp => Value '24' of type 'PrimitiveValueType[integer]', isTransient=false
  json => Value '{"customer":"Kermit"}' of type 'json', isTransient=false
}
Process is Active and waiting for user task completion
Process Variables are:
['temp':24, 'someVar':'Some String Value', 'json':{"customer":"Kermit"}]
Deployment ID: '1' has been deleted
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 16.521 sec - in ConditionalStartSpec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 24.339 s
[INFO] Finished at: 2018-06-01T14:53:20-04:00
[INFO] Final Memory: 21M/182M
[INFO] ------------------------------------------------------------------------
```
