import spock.lang.Specification

class LabelPrinter {
  def _(def message) {
    println message
    true
  }
}

Specification.mixin LabelPrinter

report {
    enabled false
    logFileDir './build-reports'
    logFileName 'spock-report.json'
    logFileSuffix new Date().format('yyyy-MM-dd_HH_mm_ss')
}