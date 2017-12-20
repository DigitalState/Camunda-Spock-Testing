# Basic Usage Example

# How to use

1. Write spock specifications in `src/test/groovy/...` (whatever folder path).
    1. Spec file names and class names must end with `Spec.groovy`.  Example: `CamundaHelloWorld1Spec.groovy`. 
1. Add your BPMN files to `src/test/resources/bpmn/...` (whatever sub-folder paths you link as long as you configure them in your `@Deployment` annotation)
1. In Terminal, navigate to the root of the folder/project and run `mvn clean test`.
1. Output will be created in the Terminal, and you can look at `./build-reports` folder for JSON reports on your tests.


# Known Issues

1. `@Deployment` can only be applied to `def` methods inside of the class.  As a result you cannot use Spock's `@Stepwise` feature to chain multiple `def` steps together for a single test/deployment.


# Business-centric Features:

1. `@Narative`
1. `@Issue`
1. `@See`
1. `@Title`
1. `_` method after each block that outputs the text in the block to the terminal and build-reports


# Build Reports

Build Reports can be configured in the `./src/test/resources/SpockConfig.groovy` file in the `report` object.

Additional reports and details can be seen in the `target` folder after a attempt of `mvn clean test`.

The `./build-reports` folder has been inlcuded in this folder for example purposes only.  The folder can be deleted, as when `mvn clean test` is run, the folder will be created when required.