/**
 * Load configuration file as a SPIN JSON variable in-memory and optionally as a process variable.
 *
 * @param string fileName The name of the configuration file in the deployment.
 * @param string key The top level JSON key in the configuration file that will be saved, and other keys/objects are omitted.
 * @param boolean persist Whether to save the configuration as a process variable.
 * @return SPIN JSON Object
 */
function loadConfig(fileName, key, persist)
{
  'use strict';

  if (typeof(persist) == 'undefined') {
    persist = false;
  }

  if (typeof(key) == 'undefined') {
    key = null;
  }

  var processDefinitionId = execution.getProcessDefinitionId();
  var deploymentId = execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(processDefinitionId).getDeploymentId();
  var resource = execution.getProcessEngineServices().getRepositoryService().getResourceAsStream(deploymentId, fileName);

  var Scanner = Java.type('java.util.Scanner');

  var scannerResource = new Scanner(resource, 'UTF-8');

  var configText = scannerResource.useDelimiter('\\Z').next();
  scannerResource.close();

  var configAsJson = S(configText);

  if (key === null) {
    var config = configAsJson;
  } else {
    if (!configAsJson.hasProp(key)) {
      throw 'Key "' + key + '" does not exist.';
    }
    var config = configAsJson.prop(key);
  }

  if (persist) {
    execution.setVariable('_config', config);
  }

  return config;
}

loadConfig('config.json', 'script_paths', true)