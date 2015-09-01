#!/usr/bin/ruby

branch=ARGV[1]
if branch != "master" then
        exit(0)
end


require 'fileutils'

regexesPluralOneMissingPara = [
        /Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*'([^,]*)'\s*,\s*'([^,]*)'\s*\)/m,
		/Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*"([^,]*)"\s*,\s*"([^,]*)"\s*\)/m,
        /Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*'([^,]*)'\s*,\s*"([^,]*)"\s*\)/m,
		/Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*"([^,]*)"\s*,\s*'([^,]*)'\s*\)/m
	]

regexesPluralTwoMissingPara = [
        /Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*'([^,]*)'\s*\)/m,
		/Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*"([^,]*)"\s*\)/m
	]

regexesPlural = [
        /Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*'([^,]*)'\s*,\s*'([^,]*)'\s*,\s*'([^,^;]*)'\s*\)/m,
		/Uni\.I18n\.translatePlural\(\s*'([^,]*)'\s*,\s*([^,]*)\s*,\s*'(.{3})'\s*,\s*"([^,]*)"\s*,\s*"([^,]*)"\s*,\s*"([^,^;]*)"\s*\)/m
    ]

regexes = [
		/Uni\.I18n\.translate\(\s*'(\S*)'\s*,\s*'(.{3})'\s*,\s*'(.+?[^\\])'.*?\)/m,
		/Uni\.I18n\.translate\(\s*'(\S*)'\s*,\s*'(.{3})'\s*,\s*"(.+?[^\\])".*?\)/m
	]

duplicates = Hash.new
duplicatesBlob = "Translation duplicates\n"
differentTranslations = Hash.new
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

	doAbort = false
	for regex in regexesPluralOneMissingPara
		contents.scan(regex) {|key, counter, component, valueZero, valueOne|
			print "The call of translatePlural() for key '" + key + "' is missing a parameter\n"
			doAbort = doAbort || true
		}
	end
	if doAbort
		print " ======= ERROR ======= \n"
		abort("Aborted due to the above errors in the file:\n" + file);
	end

	doAbort = false
	for regex in regexesPluralTwoMissingPara
		contents.scan(regex) {|key, counter, component, valueZero|
			print "The call of translatePlural() for key '" + key + "' is missing two parameters\n"
			doAbort = doAbort || true;
		}
	end
	if doAbort
		print " ======= ERROR ======= \n"
		abort("Aborted due to the above errors in the file:\n" + file);
	end

	for regex in regexes 
		contents.scan(regex) {|key, component, value|

			if component != current_component
				abort("Incorrect component: " + component + " (while " + current_component + " expected)")
			end
			
			# Check for same keys with different translations
			if differentTranslations.fetch(key, nil) == nil then
				differentTranslations[key] = value
			elsif differentTranslations.fetch(key, nil) != value then
				abort("Found different translations for the same key '" + key + "': '" + value + "' vs. '" + differentTranslations.fetch(key, nil) + "'")
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

	for regex in regexesPlural
		contents.scan(regex) {|key, counter, component, valueZero, valueOne, valueMany|

			if component != current_component
				abort("Incorrect component: " + component + " (while " + current_component + " expected)")
			end

			if translations[component].nil? then
				translations[component] = Hash.new
			end

			if duplicates[valueZero].nil? then
				duplicates[valueZero] = Hash.new
			end
			if duplicates[valueOne].nil? then
				duplicates[valueOne] = Hash.new
			end
			if duplicates[valueMany].nil? then
				duplicates[valueMany] = Hash.new
			end

			keysToProcess = [ key + '[0]', key + '[1]', key + '[many]' ]
			valuesToProcess = [ valueZero, valueOne, valueMany ]

			(0..2).each do |i|
			    value = valuesToProcess[i]
				currentKey = keysToProcess[i]
				# Check for same keys with different translations
				if differentTranslations.fetch(currentKey, nil) == nil then
					differentTranslations[currentKey] = value
				elsif differentTranslations.fetch(currentKey, nil) != value then
					abort("Found different translations for the same key '" + currentKey + "': '" + value + "' vs. '" + differentTranslations.fetch(currentKey, nil) + "'")
				end
				currentValue = translations[component][currentKey]
				if currentValue.nil? && currentValue == value then
					if conflicts[component].nil? then
						conflicts[component] = Hash.new
					end

					conflicts[component][currentKey] = value
				else
					translations[component][currentKey] = value

					if duplicates[value][component].nil? then
						duplicates[value][component] = Hash.new
					end

					if duplicates[value][component][currentKey].nil? then
						duplicates[value][component][currentKey] = 0
					end

					duplicates[value][component][currentKey] = duplicates[value][component][currentKey] + 1
				end
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
