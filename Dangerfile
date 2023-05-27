# Ktlint
checkstyle_format.base_path = Dir.pwd
Dir["**/reports/ktlint/ktlintMainSourceSetCheck/**.xml"].each do |file_name|
  checkstyle_format.report file_name
end

# Detekt
Dir["**/reports/detekt/detekt.xml"].each do |file_name|
  kotlin_detekt.severity = "warning"
  kotlin_detekt.gradle_task = "detekt"
  kotlin_detekt.report_file = file_name
  kotlin_detekt.detekt(inline_mode: true)
end

txt_file_path = 'build/reports/dependency-analysis/build-health-report.txt'
txt_file_content = File.read(txt_file_path)

message "Conteúdo do arquivo baixado:"
markdown "```\n#{txt_file_content}\n```"