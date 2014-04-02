require '..\..\ext-theme-base\sass\utils.rb'

# sass_path: the directory your Sass files are in. THIS file should also be in the Sass folder
sass_path = File.dirname(__FILE__)
cur_dir = File.dirname(__FILE__)
css_path = File.join(sass_path, "..", "build", "resources")

$ext_path = File.join("..", "..", "..")

# We need to load in the Ext4 themes folder, which includes all it's default styling, images, variables and mixins
#load File.join(File.dirname(__FILE__), $ext_path, 'resources', 'themes')
#load File.join(File.dirname(__FILE__), $ext_path, 'resources', 'ext-theme-neptune')

#Compass config variable
relative_assets = true

output_style = :nested