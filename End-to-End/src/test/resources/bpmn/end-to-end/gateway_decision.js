function evalGateway(){
  var number = execution.getVariable('number')

  if (number >= 5 && number <= 10){
    return true
  } else {
    return false
  }
}

evalGateway()