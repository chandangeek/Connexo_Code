#!/usr/bin/ruby

branch=ARGV[1]
if branch != "master" then
        exit(0)
end


require 'fileutils'

regexes = [
		/Uni\.I18n\.translate\('(\S*)',\s*'(.{3})',\s*'(.+?[^\\])'.*?\)/m,
		/Uni\.I18n\.translate\('(\S*)',\s*'(.{3})',\s*\"(.+?[^\\])\".*?\)/m,
		/Uni\.I18n\.translatePlural\('(\S*)',.?.*.?,\s?'(.{3})',\s?'(.+?[^\\])'.*?\)/m,
		/Uni\.I18n\.translatePlural\('(\S*)',.?.*.?,\s?'(.{3})',\s?\"(.+?[^\\])\".*?\)/m
	]

duplicates = Hash.new
duplicatesBlob = "Translation duplicates\n"
translations = Hash.new
translationsBlob = "// Translations\n\n"
duplicatesFile = "I18nDuplicates.txt"
propertiesFile = "i18n.properties.tmp"
current_component = ""

folder = Dir.pwd

Dir.glob(folder + "/src/main/java/**/*UiInstaller.java", File::FNM_CASEFOLD) do |file|
	contents = File.read(file)
	current_component = contents.scan(/String COMPONENT_NAME = "(.{3})";/)[0][0]
	print "Current component: " + current_component + "\n"
end

Dir.glob(folder + "/src/**/*.js") do |file|
	contents = File.read(file)

	for regex in regexes 
		contents.scan(regex) {|key, component, value|

			if component != current_component
				abort("Incorrect component: " + component + " (while " + current_component + " expected)")
			end
			
			if translations[component].nil? then
				translations[component] = Hash.new
			end

			if duplicates[value].nil? then
				duplicates[value] = Hash.new
			end
					
			currentValue = translations[component][key]
			if currentValue.nil? && currentValue == value then
				if conflicts[component].nil? then
					conflicts[component] = Hash.new
				end 

				conflicts[component][key] = value
			else
				translations[component][key] = value

				if duplicates[value][component].nil? then
					duplicates[value][component] = Hash.new
				end

				if duplicates[value][component][key].nil? then
					duplicates[value][component][key] = 0
				end

				duplicates[value][component][key] = duplicates[value][component][key] + 1
			end
		}
	end
end

translations.each do |component, keys|
	componentBlob = "# " + component + " component translations (" + keys.length.to_s + " keys)\n"
	componentBlob += "# Created " + Time.new.strftime("%Y-%m-%d %T") + "\n"
	emptyKeys = Hash.new
	keys = keys.sort_by{|key,value|key}
	keys.each do |key, value|
		if key == "" || value == "" then
			emptyKeys[key] = value
		end

		componentBlob += "\n" + key + "=" + value
	end

	if emptyKeys.length > 0 then
		componentBlob += "\n\n// SEVERE ISSUES: Empty keys or values\n"

		emptyKeys.each do |key, value|
			componentBlob += "\n" + key + "=" + value	
		end
	end
	
	translationsBlob += componentBlob + "\n\n"
	print componentBlob
	File.open(folder + "/src/main/resources/" + propertiesFile, 'w') { |file| file.write(componentBlob) }
end

system("tail -n +3 src/main/resources/i18n.properties > src/main/resources/i18n.properties.bak")
system("tail -n +3 src/main/resources/i18n.properties.tmp > src/main/resources/i18n.properties.tmp.bak")
difference=`diff src/main/resources/i18n.properties.bak src/main/resources/i18n.properties.tmp.bak`
FileUtils.rm('src/main/resources/i18n.properties.bak')
FileUtils.rm('src/main/resources/i18n.properties.tmp.bak')
if difference.to_s != '' then
	FileUtils.mv('src/main/resources/i18n.properties.tmp','src/main/resources/i18n.properties')
		print "\ni18n.properties changed, push to git\n"
        remote=ARGV[0]
        value=`git remote add origin #{remote}`
        value=`git commit -m "New version of i18n.properties (bamboo build)" src/main/resources/i18n.properties`
        value=`git push origin master`
else
        FileUtils.rm('src/main/resources/i18n.properties.tmp')
end
