#!/usr/bin/env ruby

require "set"
require 'yaml'
require 'json'
require 'fileutils'


class ModelConf

  class << self
    def from(model_cfg)
      cfg = ModelConf.new(model_cfg['name'], model_cfg['type'])
      if model_cfg.has_key? 'fields'
        model_cfg['fields'].each do |field_cfg|
          if field_cfg['type'].index(' const').nil?
            cfg.append_field(FieldConf.from(field_cfg))
          else
            cfg.append_const(ConstConf.from(field_cfg))
          end
        end
        field_names = Set.new
        cfg.fields.each do |field|
          if %w(it field value).member?(field.name)
            raise "field name must not be `#{field.name}` on kotlin"
          elsif field_names.member?(field.name)
            raise "duplicated field name `#{field.name}` on model `#{cfg.name}`"
          end
          field_names << field.name
        end
        cfg.fields.each do |field|
          field.sources.each do |source|
            source_field = cfg.fields.find { |f| f.name == source }
            unless source_field.nil?
              source_field.associate(field)
            end
          end
        end
      end
      cfg.fill_imports
      cfg
    end
  end

  attr_reader :name,
              :type,
              :consts,
              :fields,
              :imports_javas,
              :imports_others

  def initialize(name, type)
    @name = name
    @type = type
    @fields = []
    @consts = []
    @imports_javas = Set.new
    @imports_others = Set.new
  end

  def append_const(const)
    @consts << const
  end

  def append_field(field)
    @fields << field.bind(self, @fields.size)
  end
  
  def fill_imports
    @imports_javas.clear
    @imports_others.clear
    if @fields.any? { |const| %w(uuid uuid-legacy).member?(const.type) }
      @imports_javas << 'java.util.*'
    end
    @imports_others += ['com.fasterxml.jackson.databind.JsonNode',
                        'com.fasterxml.jackson.databind.node.JsonNodeFactory',
                        'com.github.fmjsjx.bson.model2.core.*',
                        'org.bson.*',
                        'org.bson.conversions.Bson']
    unless @fields.empty?
      @imports_others << 'com.github.fmjsjx.bson.model.core.BsonUtil'
      unless reality_fields.empty?
        @imports_others << 'com.mongodb.client.model.Updates'
      end
    end
    if @consts.any? { |const| const.type == 'datetime' }
      @imports_javas << 'java.time.LocalDateTime'
    end
    if @fields.any? { |field| field.type == 'datetime' }
      @imports_others << 'com.github.fmjsjx.libcommon.util.DateTimeUtil'
      @imports_javas << 'java.time.LocalDateTime'
    end
  end

  def reality_fields
    @fields.select { |field| field.reality? }
  end

  def generic_super_type
    case @type
    when 'root'
      "RootModel<#@name>"
    when 'object'
      "ObjectModel<#@name>"
    else
      raise "unsupported model type `#@type`"
    end
  end

  def generate_imports_code(package)
    code = "package #{package}\n\n"
    @imports_others.sort.each { |import| code << "import #{import}\n" }
    @imports_javas.sort.each { |import| code << "import #{import}\n" }
    code << "\n"
  end

  def generate_class_code(package = nil)
    code = "\n"
    unless package.nil?
      code << generate_imports_code(package)
    end
    code << "class #@name : #{generic_super_type} {\n\n"
    code << generate_consts_code
    code << generate_fields_code
    code << "}\n"
  end

  def generate_consts_code
    code = "    companion object {\n"
    unless @consts.empty?
      @consts.each do |const|
        code << const.generate_declare_code
      end
      code << "\n"
    end
    code << @fields.map do |field|
      field.generate_const_code
    end.select do |c|
      not c.nil?
    end.join
    code << "    }\n"
    code << "\n"
  end

  def generate_fields_code
    @fields.map do |field|
      field.generate_declare_code
    end.join
  end

end


class ConstConf

  class << self
    def from(field_cfg)
      name = field_cfg['name']
      if name.start_with?('BNAME_')
        raise "const field must not be started with 'BNAME_'"
      end
      type = field_cfg['type'].split(' ')[0]
      value = field_cfg['value']
      ConstConf.new(name, type, value)
    end
  end

  attr_reader :name, :type, :value

  def initialize(name, type, value)
    @name = name
    @type = type
    @value = value
  end

  def generate_declare_code
    case @type
    when 'int', 'long', 'double', 'boolean', 'string'
      "        const val #@name = \"#@value\"\n"
    when 'datetime'
      "        val #@name: LocalDateTime = #@value\n"
    else
      raise "unsupported const type #@type"
    end
  end

end


class FieldConf

  class << self
    def from(field_cfg)
      name, bname = field_cfg['name'].split(' ')
      bname = if bname.nil? then name else bname end
      type = field_cfg['type'].split(' ')[0]
      cfg = case type
      when 'int'
        IntFieldConf.new(name, bname)
      when 'long'
        LongFieldConf.new(name, bname)
      when 'double'
        DoubleFieldConf.new(name, bname)
      when 'boolean'
        BooleanFieldConf.new(name, bname)
      when 'string'
        StringFieldConf.new(name, bname)
      when 'datetime'
        DateTimeFieldConf.new(name, bname)
      when 'object-id'
        ObjectIdFieldConf.new(name, bname)
      when 'uuid'
        UUIDFieldConf.new(name, bname, false)
      when 'uuid-legacy'
        UUIDFieldConf.new(name, bname, true)
      when 'int-array'
        IntArrayFieldConf.new(name, bname)
      when 'long-array'
        LongArrayFieldConf.new(name, bname)
      when 'double-array'
        DoubleArrayFieldConf.new(name, bname)
      when 'std-list'
        StdListFieldConf.new(name, bname)
      when 'object'
        ObjectFieldConf.new(name, bname)
      when 'map'
        MapFieldConf.new(name, bname)
      when 'list'
        ListFieldConf.new(name, bname)
      else
        raise "unsupported field type `#{type}`"
      end
      field_cfg['type'].split(' ')[1..].each do |modifier|
        case modifier
        when 'required'
          cfg.required
        when 'virtual'
          cfg.virtual
        when 'increment-1'
          cfg.increment_1
        when 'increment-n'
          cfg.increment_n
        when 'loadonly'
          cfg.loadonly
        when 'transient'
          cfg.transient
        when 'hidden'
          cfg.hidden
        end
      end
      if field_cfg.has_key? 'default'
        cfg.default = field_cfg['default']
      end
      if field_cfg.has_key? 'model'
        cfg.model = field_cfg['model']
      end
      if field_cfg.has_key? 'key'
        cfg.key = field_cfg['key']
      end
      if field_cfg.has_key? 'value'
        cfg.value = field_cfg['value']
      end
      if field_cfg.has_key? 'sources'
        cfg.sources = field_cfg['sources'].uniq
      end
      if field_cfg.has_key? 'lambda'
        cfg.lambda_expression = field_cfg['lambda']
      end
      if field_cfg.has_key? 'lambda-kotlin'
        dfg.lambda_expression = field_cfg['lambda-kotlin']
      end
      cfg
    end
  end

  attr_reader :name,
              :bname,
              :type,
              :kotlin_type,
              :parent_model,
              :index,
              :associates

  attr_accessor :default,
                :model,
                :key,
                :value,
                :sources,
                :lambda_expression

  def initialize(name, bname, type, kotlin_type = nil)
    @name = name
    @bname = bname
    @type = type
    @kotlin_type = kotlin_type
    @required = false
    @virtual = false
    @loadonly = false
    @transient = false
    @hidden = false
    @increment_1 = false
    @increment_n = false
    @sources = []
    @associates = []
  end

  def required(required = true)
    @required = required
    self
  end

  def required?
    @required
  end

  def generic_type
    if required?
      @kotlin_type
    else
      "#@kotlin_type?"
    end
  end

  def has_default?
    not @default.nil?
  end

  def virtual(virtual = true)
    @virtual = virtual
    self
  end

  def virtual?
    @virtual
  end

  def loadonly(loadonly = true)
    @loadonly = loadonly
    self
  end

  def loadonly?
    @loadonly
  end

  def transient(transient = true)
    @transient = transient
    self
  end

  def transient?
    @transient
  end

  def hidden?
    @hidden
  end

  def hidden(hidden = true)
    @hidden = hidden
    self
  end

  def increment_1(increment_1 = true)
    @increment_1 = increment_1
    self
  end

  def increment_1?
    @increment_1
  end

  def increment_n(increment_n = true)
    @increment_n = increment_n
    self
  end

  def increment_n?
    @increment_n
  end

  def bind(parent_model, index)
    @parent_model = parent_model
    @index = index
    self
  end

  def associate(field)
    @associates << field
  end

  def single_value?
    true
  end

  def reality?
    not virtual? and not loadonly? and not transient?
  end

  def key_type
    unless @key.nil?
      case @key
      when 'int'
        'Int'
      when 'long'
        'Long'
      when 'string'
        'String'
      else
        raise "unsupported key type `#@key`"
      end
    end
  end

  def value_type
    unless @value.nil?
      case @value
      when 'int'
        'Int'
      when 'long'
        'Long'
      when 'double'
        'Double'
      when 'string'
        'String'
      else
        raise "unsupported value type `#@value`"
      end
    end
  end

  def bname_const_field_name
    "BNAME_#{upper_word_name}"
  end

  def upper_word_name
    @name.gsub(/[A-Z]/) { |match| "_#{match}" }.upcase
  end

  def camcel_name
    @name[0].upcase << @name[1..]
  end

  def default_value_code
    unless required?
      return nil
    end
    required_default_value_code
  end

  def required_default_value_code
    raise "default value is unsupported for `#@type`"
  end

  def variable_name(suffix)
    if @parent_model.nil?
      @name + suffix
    else
      @parent_model.variable_name(@name + suffix)
    end
  end

  def generate_const_code
    if virtual? or transient?
      return nil
    end
    "        const val #{bname_const_field_name} = \"#@bname\"\n"
  end

  def generate_declare_code
    code = ""
    if virtual?
      code << "    val #@name: #{generic_type}\n"
      code << "        get() = #@lambda_expression\n"
    elsif transient?
      code << generate_transient_declare_code
    elsif loadonly?
      code << generate_loadonly_declare_code
    else
      code << generate_reality_declare_code
    end
    code << "\n"
  end

  def generate_transient_declare_code
    if required?
      if has_default?
        "    var #@name: #{generic_type} = #{default_value_code}\n\n"
      else
        "    lateinit var #@name: #{generic_type}\n\n"
      end
    else
      "    var #@name: #{generic_type} = null\n\n"
    end
  end

  def generate_loadonly_declare_code
    generate_transient_declare_code
  end

  def generate_reality_declare_code
    code = generate_field_declare_line_code
    # TODO
  end

  def generate_field_declare_line_code
    if required?
      unless has_default?
        raise "#{type} required field must has default value"
      end
      "    var #@name: #{generic_type} = #{default_value_code}\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

  # TODO

end


class BuildInFieldConf < FieldConf
  

  def initialize(name, bname, type, kotlin_type, primitive = false)
    super(name, bname, type, kotlin_type)
    @primitive = primitive
  end
  
  def primitive?
    @primitive
  end

  def generate_transient_declare_code
    if required?
      if primitive? or has_default?
        "    var #@name: #{generic_type} = #{default_value_code}\n\n"
      else
        "    lateinit var #@name: #{generic_type}\n\n"
      end
    else
      "    var #@name: #{generic_type} = null\n\n"
    end
  end

  def generate_field_declare_line_code
    if required?
      "    var #@name: #{generic_type} = #{default_value_code}\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class IntFieldConf < BuildInFieldConf

  def initialize(name, bname)
    super(name, bname, 'int', 'Int', true)
  end

  def required_default_value_code
    if @default.nil?
      '0'
    else
      case @default.to_s.downcase
      when 'min'
        'Int.MIN_VALUE'
      when 'max'
        'Int.MAX_VALUE'
      else
        @default.to_s
      end
    end
  end

end

class LongFieldConf < BuildInFieldConf

  def initialize(name, bname)
    super(name, bname, 'long', 'Long', true)
  end

  def required_default_value_code
    if @default.nil?
      '0L'
    else
      case @default.to_s.downcase
      when 'min'
        'Long.MIN_VALUE'
      when 'max'
        'Long.MAX_VALUE'
      else
        @default.to_s
      end
    end
  end

end

class DoubleFieldConf < BuildInFieldConf

  def initialize(name, bname)
    super(name, bname, 'double', 'Double', true)
  end

  def required_default_value_code
    if @default.nil?
      '0.0'
    else
      case @default.to_s.downcase
      when 'nan'
        'Double.NaN'
      when '+inf'
        'Double.POSITIVE_INFINITY'
      when '-inf'
        'Double.NEGATIVE_INFINITY'
      when 'min'
        'Double.MIN_VALUE'
      when 'max'
        'Double.MAX_VALUE'
      else
        @default.to_s
      end
    end
  end

end

class BooleanFieldConf < BuildInFieldConf

  def initialize(name, bname)
    super(name, bname, 'boolean', 'Boolean', true)
  end

  def required_default_value_code
    if @default.nil?
      'false'
    else
      case @default.to_s.downcase
      when 'true', '1'
        'true'
      else
        'false'
      end
    end
  end

end

class StringFieldConf < BuildInFieldConf

  def initialize(name, bname)
    super(name, bname, 'string', 'String')
  end

  def required_default_value_code
    if @default.nil?
      '""'
    else
      @default.to_json
    end
  end

end

class DateTimeFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'datetime', 'LocalDateTime')
  end

  def required_default_value_code
    case @default.downcase
    when 'min'
      'LocalDateTime.MIN'
    when 'max'
      'LocalDateTime.MAX'
    when 'now'
      'LocalDateTime.now()'
    else
      "LocalDateTime.parse(#{@default.to_json})"
    end
  end

  def generate_field_declare_line_code
    if required?
      if has_default?
        "    var #@name: #{generic_type} = #{default_value_code}\n"
      else
        "    var #@name: #{generic_type} = LocalDateTime.MIN\n"
      end
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class ObjectIdFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'object-id', 'ObjectId')
  end

end

class UUIDFieldConf < FieldConf

  attr_accessor :legacy

  def initialize(name, bname, legacy)
    super(name, bname, legacy ? 'uuid-legacy' : 'uuid', 'UUID')
  end

end

class PrimitiveArrayFieldConf < FieldConf

  attr_reader :primitive_value_type

  def initialize(name, bname, primitive_value_type, kotlin_type)
    super(name, bname, "#{primitive_value_type}-array", kotlin_type)
    @primitive_value_type = primitive_value_type
  end

  def required_default_value_code
    "#{primitive_value_type}ArrayOf(#{JSON.parse("[#@default]").join(", ")})"
  end

  def generate_field_declare_line_code
    if required?
      if has_default?
        "    var #@name: #{generic_type} = #{default_value_code}\n"
      else
        "    var #@name: #{generic_type} = emptyList()\n"
      end
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class IntArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'int', 'IntArray')
  end

end

class LongArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'long', 'LongArray')
  end

end

class DoubleArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'double', 'DoubleArray')
  end

end

class StdListFieldConf < FieldConf

  def initialize(name, bname)
    super(name, bname, 'std-list', 'List')
    @value_nullable = false
  end
  
  def value_nullable(value_nullable = true)
    @value_nullable = value_nullable
    self
  end

  def value_nullable?
    @value_nullable
  end
  
  def parameterized_type
    if value_nullable?
      if @value == 'object'
        "#@model?"
      else
        "#{value_type}?"
      end
    else
      if @value == 'object'
        @model
      else
        value_type
      end
    end
  end

  def generic_type
    if required?
      "#@kotlin_type<#{parameterized_type}>"
    else
      "#@kotlin_type<#{parameterized_type}>?"
    end
  end

  def generate_transient_declare_code
    if required?
      "    var #@name: #{generic_type} = emptyList()\n\n"
    else
      "    var #@name: #{generic_type} = null\n\n"
    end
  end

  def generate_field_declare_line_code
    if required?
      "    val #@name: #{generic_type} = emptyList()\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class ModelFieldConf < FieldConf
  
  def initialize(name, bname, type)
    super(name, bname, type)
  end

  def single_value?
    false
  end

end

class ObjectFieldConf < ModelFieldConf

  def initialize(name, bname)
    super(name, bname, 'object')
  end

  def generic_type
    if required?
      @model
    else
      "#@model?"
    end
  end

  def generate_transient_declare_code
    if required?
      "    val #@name: #{generic_type} = #{generic_type}()\n\n"
    else
      "    var #@name: #{generic_type} = null\n\n"
    end
  end

  def generate_field_declare_line_code
    if required?
      "    val #@name: #{generic_type} = #{generic_type}().parent(this).key(#{bname_const_field_name}).index(#@index)\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class MapFieldConf < ModelFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'map')
  end

  def generic_type
    if @value == 'object'
      if required?
        "DefaultMapModel<#{key_type}, #@model>"
      else
        "DefaultMapModel<#{key_type}, #@model>?"
      end
    else
      if required?
        "SingleValueMapModel<#{key_type}, #{value_type}>"
      else
        "SingleValueMapModel<#{key_type}, #{value_type}>?"
      end
    end
  end

  def generate_transient_declare_code
    if required?
      "    val #@name: #{generic_type} = #{map_init_code}\n\n"
    else
      "    var #@name: #{generic_type} = null\n\n"
    end
  end

  def map_init_code
    if @value == 'object'
      case @key
      when 'int'
        "DefaultMapModel.integerKeysMap(::#@model)"
      when 'long'
        "DefaultMapModel.longKeysMap(::#@model)"
      when 'string'
        "DefaultMapModel.stringKeysMap(::#@model)"
      else
        raise "unsupported key type `#@key`"
      end
    else
      case @key
      when 'int'
        "SingleValueMapModel.integerKeysMap(#{single_value_type})"
      when 'long'
        "SingleValueMapModel.longKeysMap(#{single_value_type})"
      when 'string'
        "SingleValueMapModel.stringKeysMap(#{single_value_type})"
      else
        raise "unsupported key type `#@key`"
      end
    end
  end

  def single_value_type
    case @value
    when 'int'
      'SingleValueTypes.INTEGER'
    when 'long'
      'SingleValueTypes.LONG'
    when 'double'
      'SingleValueTypes.DOUBLE'
    when 'string'
      'SingleValueTypes.STRING'
    else
      raise "unsupported value type `#@value`"
    end
  end

  def generate_field_declare_line_code
    if required?
      "    val #@name: #{generic_type} = #{map_init_code}.parent(this).key(#{bname_const_field_name}).index(#@index)\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

class ListFieldConf < ModelFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'list')
  end

  def generic_type
    if @value == 'object'
      if required?
        "DefaultListModel<#@model>"
      else
        "DefaultListModel<#@model>?"
      end
    else
      raise "unsupported value type `#@value` for list"
    end
  end

  def generate_transient_declare_code
    raise "list field can't be neither transient nor loadonly"
  end

  def generate_field_declare_line_code
    if required?
      "    var #@name: #{generic_type} = DefaultListModel(::#@model).parent(this).key(#{bname_const_field_name}).index(#@index)\n"
    else
      "    var #@name: #{generic_type} = null\n"
    end
  end

end

cfg = YAML.load_file(ARGV[0])

if cfg.has_key? 'java-package'
  cfg['package'] = cfg['java-package']
end

if cfg.has_key? 'kotlin-package'
  cfg['package'] = cfg['kotlin-package']
end

model_names = Set.new
cfg['models'].map do |model_cfg|
  model = ModelConf.from(model_cfg)
  if model_names.member?(model.name)
    raise "duplicated model name `#{model.name}`"
  end
  model_names << model.name
  model
end.each do |model|
  puts '----------------------------------------------------------'
  #package_dir = File.join(ARGV[1], File.join(cfg['package'].split('.')))
  filename = "#{model.name}.java"
  #puts "Generating #{filename} ... (on path: #{package_dir})"
  code = model.generate_class_code(cfg['package'])
  puts code
  puts "OK"
  puts '=========================================================='
end
