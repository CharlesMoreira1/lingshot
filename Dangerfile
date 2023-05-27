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

file_path = '**/build/reports/dependency-analysis/build-health-report.txt'

if File.exist?(file_path)
  file_content = File.read(file_path).strip
  markdown("Detect unused and misused dependencies: `#{file_content}`")
else
  markdown("O arquivo #{file_path} não foi encontrado.")
end