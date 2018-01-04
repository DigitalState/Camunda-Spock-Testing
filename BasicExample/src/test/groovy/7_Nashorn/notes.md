Notes

# Reference Materials:

Process Engine Tests:
> http://camunda.github.io/camunda-bpm-assert/apidocs/org/camunda/bpm/engine/test/assertions/ProcessEngineTests.html

AssertJ methods that are available through the Camunda Unit Test Imports:
>http://joel-costigliola.github.io/assertj/core/api/index.html

Spock and Nashorn Inspiration:
>https://objectpartners.com/2014/05/29/unit-test-your-server-side-javascript-with-spock/

>http://eshepelyuk.github.io/2014/11/26/-testing-jvm-javascript-jasmine-spock-nashorn.html

Javax Script Engine Manager:
>https://docs.oracle.com/javase/7/docs/api/javax/script/ScriptEngineManager.html

Java Script Engine:
>https://docs.oracle.com/javase/7/docs/api/javax/script/ScriptEngine.html

General Nashorn Example docs:
>http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html

Nashorn Script Engine:
>https://docs.oracle.com/javase/8/docs/jdk/api/nashorn/jdk/nashorn/api/scripting/NashornScriptEngine.html

Camunda Delegate Execution Interface (execution variable in scripts):
>https://docs.camunda.org/javadoc/camunda-bpm-platform/7.8/org/camunda/bpm/engine/delegate/DelegateExecution.html

Spock Docs on Interaction Testing:
>http://spockframework.org/spock/docs/1.0/interaction_based_testing.html


Variables to Mock and their required classes/interfaces:
>https://docs.camunda.org/manual/7.8/user-guide/process-engine/scripting/#variables-available-during-script-execution

# General notes

1. Quote from Spck Docs:
   > Stubbing is the act of making collaborators respond to method calls in a certain way. When stubbing a method, you don’t care if and how many times the method is going to be called; you just want it to return some value, or perform some side effect, whenever it gets called.
   
    - The "You dont care the number of times its getting called" is SUPER important difference.  Not following this will cause weird behavior.