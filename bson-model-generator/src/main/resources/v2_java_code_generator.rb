#!/usr/bin/env ruby

require "set"
require 'yaml'
require 'json'


class ModelConf

  class << self
    def from(model_cfg)
      cfg = ModelConf.new(model_cfg['name'], model_cfg['type'])
      if model_cfg.has_key? 'fields'
        model_cfg['fields'].each do |field_cfg|
          cfg.append_field(FieldConf.from(field_cfg))
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
              :fields

  def initialize(name, type)
    @name = name
    @type = type
    @fields = []
    @imports_javas = Set.new
    @imports_others = Set.new
  end

  def append_field(field)
    @fields << field.bind(self, @fields.size)
  end

  def all_fields_single_value_required?
    @fields.e
  end

  def fill_imports
    @imports_javas.clear
    @imports_others.clear
    @imports_javas += ['java.util.*']
    @imports_others += ['com.fasterxml.jackson.databind.JsonNode',
                        'com.fasterxml.jackson.databind.node.JsonNodeFactory',
                        'com.github.fmjsjx.bson.model2.core.*',
                        'org.bson.*',
                        'org.bson.conversions.Bson']
    unless @fields.empty?
      @imports_others += ['com.github.fmjsjx.bson.model.core.BsonUtil',
                          'com.mongodb.client.model.Updates']
    end
    if @fields.any? { |field| field.type == 'datetime' }
      @imports_others += ['com.github.fmjsjx.libcommon.util.DateTimeUtil']
      @imports_javas << 'java.time.LocalDateTime'
    end
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

  def generate_class_code(package)
    code = "package #{package};\n\n"
    @imports_others.sort.each { |import| code << "import #{import};\n" }
    @imports_javas.sort.each { |import| code <<"import #{import};\n" }
    code << "\n"
    code << "public class #@name extends #{generic_super_type} {\n\n"
    code << generate_consts_code
    code << "\n"
    code << generate_fields_code
    code << "\n"
    code << generate_fields_accessors_code
    code << "\n"
    code << "}\n"
  end

  def generate_consts_code
    @fields.map do |field|
      field.generate_const_code
    end.select do |c|
      not c.nil?
    end.join
  end

  def generate_fields_code
    @fields.map do |field|
      field.generate_declare_code
    end.select do |c|
      not c.nil?
    end.join
  end

  def generate_fields_accessors_code
    @fields.map do |field|
      field.generate_accessors_code
    end.join
  end

end

class FieldConf

  class << self
    def from(field_cfg)
      name, bname = field_cfg['name'].split(' ')
      bname = if bname.nil? then name else bname end
      type = field_cfg['type'].split(' ')[0]
      cfg = FieldConf.new(name, bname, type)
      field_cfg['type'].split(' ')[1..-1].each do |modifier|
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
      cfg
    end
  end

  attr_reader :name,
              :bname,
              :type,
              :parent_model,
              :index,
              :associates

  attr_accessor :default,
                :model,
                :key,
                :value,
                :sources,
                :lambda_expression

  def initialize(name, bname, type)
    @name = name
    @bname = bname
    @type = type
    @required = false
    @virtual = false
    @loadonly = false
    @transient = false
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
    %w(int long double boolean string date datetime object-id
       int-array long-array double-array std-list).member? @type
  end

  def generic_type
    case @type
    when 'int'
      if required? then 'int' else 'Integer' end
    when 'long'
      if required? then 'long' else 'Long' end
    when 'double'
      if required? then 'double' else 'Double' end
    when 'boolean'
      if required? then 'boolean' else 'Boolean' end
      when 'string'
        if required? then 'boolean' else 'Boolean' end
    when 'datetime'
      'LocalDateTime'
    when 'object-id'
      'ObjectId'
    when 'int-array'
      'int[]'
    when 'long-array'
      'long[]'
    when 'double-array'
      'double[]'
    when 'std-list'
      if @value == 'object'
        "List<#@model>"
      else
        "List<#{value_type}>"
      end
    when 'object'
      @model
    when 'map'
      if @value == 'object'
        "DefaultMapModel<#{key_type}, #@model>"
      else
        "SingleValueMapModel<#{key_type}, #{value_type}>"
      end
    when 'list'
      if @value == 'object'
        "DefaultListModel<#@model>"
      else
        raise "unsupported value type `#@value` for list"
      end
    else
      raise "unsupported type `#@type`"
    end
  end

  def key_type
    unless @key.nil?
      case @key
      when 'int'
        'Integer'
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
        'Integer'
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
    "BNAME_#{@name.gsub(/[A-Z]/) { |match| "_#{match}" }.upcase}"
  end

  def getter_name
    "get#{camcel_name}"
  end

  def setter_name
    "set#{camcel_name}"
  end

  def generate_const_code
    if virtual? or transient?
      return nil
    end
    "    public static final String #{bname_const_field_name} = \"#@bname\";\n"
  end

  def generate_declare_code
    if virtual?
      return nil
    end
    case @type
    when 'int', 'long'
      if required?
        if has_default? and @default != 0
          "    private #{generic_type} #@name = #{default_value};\n"
        else
          "    private #{generic_type} #@name;\n"
        end
      else
        "    private #{generic_type} #@name;\n"
      end
    when 'double', 'boolean', 'string', 'datetime', 'int-array', 'long-array', 'double-array'
      if required? and has_default?
        "    private #{generic_type} #@name = #{default_value};\n"
      else
        "    private #{generic_type} #@name;\n"
      end
    when 'object-id'
      "    private #{generic_type} #@name;\n"
    when 'std-list'
      unless loadonly? or transient?
        raise 'std-list can only be `loadonly` or `transient`'
      end
      if required?
        "    private #{generic_type} #@name = List.of();\n"
      else
        "    private #{generic_type} #@name;\n"
      end
    when 'object'
      if required?
        "    private final #{generic_type} #@name = new #{generic_type}().parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      else
        "    private #{generic_type} #@name;\n"
      end
    when 'map'
      if required?
        "    private final #{generic_type} #@name = #{map_init_code}.parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      else
        "    private #{generic_type} #@name;\n"
      end
    when 'list'
      if required?
        "    private final #{generic_type} #@name = new #{generic_type}(#@model::new).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      else
        "    private #{generic_type} #@name;\n"
      end
    else
      raise "unsupported type `#@type`"
    end
  end

  def generate_accessors_code
    code = generate_getter_code
    code << "\n"
  end

  def generate_getter_code
    code = ""
    if @type == 'boolean' and required?
      code = "    public boolean is#{camcel_name}() {\n"
    else
      code = "    public #{generic_type} get#{camcel_name}() {\n"
    end
    if virtual?
      code << "        return #@lambda_expression;\n"
    else
      code << "        return #@name;\n"
    end
    code << "    }\n"
  end

  def camcel_name
    @name[0].upcase << @name[1..-1]
  end

  def default_value
    if not required?
      return nil
    end
    case @type
    when 'int'
      int_default_value
    when 'long'
      long_default_value
    when 'double'
      double_default_value
    when 'boolean'
      boolean_default_value
    when 'string'
      string_default_value
    when 'datetime'
      datetime_default_value
    when 'int-array'
      primitive_array_default_value('int')
    when 'long-array'
      primitive_array_default_value('long')
    when 'double-array'
      primitive_array_default_value('double')
    else
      raise "default value is unsupported for `#@type`"
    end
  end

  def int_default_value
    case @default.to_s.downcase
    when 'min'
      'Integer.MIN_VALUE'
    when 'max'
      'Integer.MAX_VALUE'
    else
      @default.to_s
    end
  end

  def long_default_value
    case @default.to_s.downcase
    when 'min'
      'Long.MIN_VALUE'
    when 'max'
      'Long.MAX_VALUE'
    else
      @default.to_s
    end
  end

  def double_default_value
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

  def boolean_default_value
    case @default.to_s.downcase
    when 'true', '1'
      'true'
    else
      'false'
    end
  end

  def string_default_value
    @default.to_json
  end

  def datetime_default_value
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

  def primitive_array_default_value(type)
    "new #{type}[] { #{JSON.parse("[#@default]").join(", ")} }"
  end

  def map_init_code
    if @value == 'object'
      case @key
      when 'int'
        "DefaultMapModel.integerKeysMap(#@model::new)"
      when 'long'
        "DefaultMapModel.longKeysMap(#@model::new)"
      when 'string'
        "DefaultMapModel.stringKeysMap(#@model::new)"
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

end


cfg = YAML.load_file(ARGV[0])

if cfg.has_key? 'java-package'
  cfg['package'] = cfg['java-package']
end

cfg['models'].each do |model_cfg|
  model = ModelConf.from(model_cfg)
  puts model.generate_class_code cfg['package']
  puts "========================="
end
