function transform(person) {
    return {
      firstName: person.name.first, 
      lastName: person.name.last
      }
}

var dogName = execution.getVariable('dog')
dogName   // Variable needs to be returned at the end or else groovy gets 'null'
