cur_dir = File.dirname(__FILE__)
output_style = :compact

module Sass::Script::Functions
  def get_resource_dir()
    dir = ENV.fetch("resource_dir", '../../')
    Sass::Script::String.new(dir)
  end
end
