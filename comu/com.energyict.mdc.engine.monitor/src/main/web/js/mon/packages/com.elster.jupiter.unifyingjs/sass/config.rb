cur_dir = File.dirname(__FILE__)

# Get the directory that this configuration file exists in
dir = File.dirname(__FILE__)

# Compass configurations
sass_path = dir
css_path = File.join(dir, '..', 'resources', 'css')
fonts_path = File.join(dir, '..', 'resources', 'fonts')
images_dir = File.join(dir, '..', 'resources', 'images')

# Require any additional compass plugins here.
output_style = :compressed
environment = :production

module Sass::Script::Functions
  def get_resource_dir()
    dir = ENV.fetch("resource_dir", '../../')
    Sass::Script::String.new(dir)
  end
end


