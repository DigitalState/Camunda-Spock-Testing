function spinify(body) {
  // var parsed = JSON.parse(body)
  // var stringified = JSON.stringify(parsed)
  var stringified = JSON.stringify(body)
  var spin = S(stringified)
  return spin
}

execution.setVariable('someVar', 'Some String Value')

var myJson = {
  "someKey1": "some Value 1",
  "someKey2": "some Value 2",
  "someKey3": {
      "someSubKey1": "some Sub Value 1",
      "someSubKey2": [
        {
          "someArrayKey1": "Some Array Value 1"
        },
        {
          "someArrayKey2": "Some Array Value 2",
          "someArrayKey3": "Some Array Value 3"
        }
      ]
  }
}

var myJsonSpin = spinify(myJson)
myJsonSpin

