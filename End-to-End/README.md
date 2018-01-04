# End to End Test

This is a end to end test that looks to demonstrate a production level implementation of Unit Testing using Spock Framework.

Overall goals will be to test:

1. BPMN process 
1. Scripts inside of the BPMN. Each script should be a external file.


Notes:

1. Camunda 7.8
1. Java 8
1. Uses Spock Framework / Groovy for tests
1. All Scripts being executed in Camunda are exteral resource scripts and use Javascript/Nashorn as script language.
1. BPMN and Scripts are located in `./src/test/resources/bpmn/end-to-end`
1. Unit Test Specs are located in: `./src/test/groovy/end-to-end`

# How to run

Execute: `mvn clean test` in terminal when in root of project folder.


# BPMN Image

![bpmn image](./src/test/resources/bpmn/end-to-end/end-to-end.png)

----

# Notes:

Task Assignments:
1. Do Something: Task_1bvdtdv: Assginee: `chris`
2. Do Something Else: Task_1xjtauh: Assginee: `john`