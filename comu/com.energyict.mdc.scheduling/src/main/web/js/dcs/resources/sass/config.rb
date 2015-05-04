# Get the directory that this configuration file exists in
dir = File.dirname(__FILE__)

# Load ExtJS themes
load File.join(dir, '..', '..', 'ext', 'packages', 'ext-theme-neptune')

# Compass configurations
sass_path = dir
css_path = File.join(dir, "..", "css")
fonts_path = File.join(dir, "..", "fonts")
images_dir = File.join(dir, "..", "images")

# Require any additional compass plugins here.
output_style = :compressed
environment = :production
