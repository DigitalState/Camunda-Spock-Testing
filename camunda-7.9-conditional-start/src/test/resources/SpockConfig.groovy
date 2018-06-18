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


spockReports {
    set 'com.athaydes.spockframework.report.template.TemplateReportCreator.enabled': true
    set 'com.athaydes.spockframework.report.testSourceRoots': "src/test/groovy"
    set 'com.athaydes.spockframework.report.showCodeBlocks': true
    set 'com.athaydes.spockframework.report.hideEmptyBlocks': false

    set 'com.athaydes.spockframework.report.outputDir': 'target/spock-reports'

    set 'com.athaydes.spockframework.report.IReportCreator': 'com.athaydes.spockframework.report.template.TemplateReportCreator'

    set 'com.athaydes.spockframework.report.template.TemplateReportCreator.specTemplateFile': '/templateReportCreator/spec-template.template'
    set 'com.athaydes.spockframework.report.template.TemplateReportCreator.reportFileExtension': 'html'
    set 'com.athaydes.spockframework.report.template.TemplateReportCreator.summaryTemplateFile': '/templateReportCreator/summary-template.template'
    set 'com.athaydes.spockframework.report.template.TemplateReportCreator.summaryFileName': 'summary.html'
}

