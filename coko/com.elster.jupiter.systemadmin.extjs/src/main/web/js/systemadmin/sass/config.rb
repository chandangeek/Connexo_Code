module Sass::Script::Functions
  def get_resource_dir()
    dir = ENV.fetch("resource.dir", '../../')
    Sass::Script::String.new(dir)
  end
end