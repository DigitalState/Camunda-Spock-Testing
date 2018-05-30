# Interactive Example

This is a example of how to create a interactive style console output allowing to quickly test the camunda environment and the bpmn files being uploaded.


# How to Use:

Root into folder and run: `./mvnw clean test`

Test will run.

You can update the BPMN file in src/test/resources/bpmn/interactive/interactive.bpmn



# Console output:

```console
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running InteractiveSpec
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
May 29, 2018 9:41:33 PM org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
INFO: Loading XML bean definitions from class path resource [camunda_config/camunda.cfg.xml]
Deployment ID: '1' has been created
Starting the process instance
Process is Active and waiting for user task completion
Process Variables are:
['someVar':'Some String Value', 'json':{"customer":"Kermit"}]
Deployment ID: '1' has been deleted
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.233 sec - in InteractiveSpec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 22.290 s
[INFO] Finished at: 2018-05-29T21:41:47-04:00
[INFO] Final Memory: 22M/179M
[INFO] ------------------------------------------------------------------------
```
