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