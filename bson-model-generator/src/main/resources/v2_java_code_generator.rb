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
          if field_names.member?(field.name)
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
              :fields

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
    @imports_javas << 'java.util.*'
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

  def generate_class_code(package)
    code = "package #{package};\n\n"
    @imports_others.sort.each { |import| code << "import #{import};\n" }
    code << "\n"
    @imports_javas.sort.each { |import| code << "import #{import};\n" }
    code << "\n"
    code << "public class #@name extends #{generic_super_type} {\n\n"
    code << generate_consts_code
    code << generate_fields_code
    code << generate_fields_accessors_code
    code << generate_fields_changed_code
    code << generate_to_bson_code
    code << generate_load_code
    code << generate_to_json_node_code
    code << generate_to_data_code
    code << generate_any_updated_code
    code << generate_reset_children_code
    code << generate_deleted_size_code
    code << generate_any_deleted_code
    code << generate_clean_code
    code << generate_deep_copy_code
    code << generate_deep_copy_from_code
    code << generate_append_field_updates_code
    code << generate_load_object_node_code
    code << generate_append_update_data_code
    unless @fields.select { |field| not field.hidden? and not field.loadonly? and not field.transient? }
                  .any? { |field| not field.required? or not field.single_value? }
      code << generate_to_deleted_data_return_null_code
    end
    code << generate_append_deleted_data_code
    code << generate_to_string_code
    code << "}\n"
  end

  def generate_consts_code
    code = ''
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
    code << "\n"
  end

  def generate_fields_code
    @fields.map do |field|
      field.generate_declare_code
    end.select do |c|
      not c.nil?
    end.join << "\n"
  end

  def generate_fields_accessors_code
    @fields.map do |field|
      field.generate_accessors_code
    end.join
  end

  def generate_fields_changed_code
    @fields.map do |field|
      field.generate_changed_code
    end.select do |c|
      not c.nil?
    end.join
  end

  def generate_to_bson_code
    bson_var = variable_name('bson')
    code = "    @Override\n"
    code << "    public BsonDocument toBson() {\n"
    fields = reality_fields
    if fields.empty?
      code << "        return new BsonDocument();\n"
    else
      code << "        var bson = new BsonDocument();\n"
      fields.map do |field|
        field.generate_append_to_bson_code(bson_var)
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
      code << "        return bson;\n"
    end
    code << "    }\n\n"
  end

  def generate_load_code
    code = "    @Override\n"
    code << "    public #@name load(BsonDocument src) {\n"
    code << "        resetStates();\n"
    @fields.map do |field|
      field.generate_load_code
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "        return this;\n"
    code << "    }\n\n"
  end

  def generate_to_json_node_code
    json_node_var = variable_name('jsonNode')
    code = "    @Override\n"
    code << "    public JsonNode toJsonNode() {\n"
    code << "        var #{json_node_var} = JsonNodeFactory.instance.objectNode();\n"
    @fields.map do |field|
      field.generate_append_to_json_node_code(json_node_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "        return jsonNode;\n"
    code << "    }\n\n"
  end

  def variable_name(var_name)
    variable_name = var_name
    i = 0
    while @fields.any? { |f| f.name == variable_name } do
      variable_name = "#{var_name}#{i += 1}"
    end
    variable_name
  end

  def generate_to_data_code
    data_var = variable_name('data')
    code = "    @Override\n"
    code << "    public Object toData() {\n"
    code << "        var #{data_var} = new LinkedHashMap<>();\n"
    @fields.map do |field|
      field.generate_put_to_data_code(data_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "        return data;\n"
    code << "    }\n\n"
  end

  def generate_any_updated_code
    code = "    @Override\n"
    code << "    public boolean anyUpdated() {\n"
    fields = reality_fields
    unless fields.empty?
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return false;\n"
      code << "        }\n"
      fields.map do |field|
        field.generate_any_updated_code
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
    end
    code << "        return false;\n"
    code << "    }\n\n"
  end

  def generate_reset_children_code
    code = "    @Override\n"
    code << "    protected void resetChildren() {\n"
    @fields.select do |field|
      not field.single_value?
    end.each do |field|
      if field.reality?
        if field.required?
          code << "        #{field.name}.reset();\n"
        else
          code << "        var #{field.name} = this.#{field.name};\n"
          code << "        if (#{field.name} != null) {\n"
          code << "            #{field.name}.reset();\n"
          code << "        }\n"
        end
      end
    end
    code << "    }\n\n"
  end

  def generate_deleted_size_code
    code = "    @Override\n"
    code << "    protected int deletedSize() {\n"
    fields = reality_fields
    unless fields.any? { |field| not field.required? or not field.single_value? }
      code << "        return 0;\n"
    else
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return 0;\n"
      code << "        }\n"
      n_var = variable_name('n')
      code << "        var #{n_var} = 0;\n"
      fields.each do |field|
        if field.single_value?
          unless field.required?
            code << "        if (changedFields.get(#{field.index}) && #{field.name} == null) {\n"
            code << "            #{n_var}++;\n"
            code << "        }\n"
          end
        else
          if field.required?
            code << "        if (changedFields.get(#{field.index}) && #{field.name}.anyDeleted()) {\n"
            code << "            #{n_var}++;\n"
            code << "        }\n"
          else
            code << "        if (changedFields.get(#{field.index})) {\n"
            code << "            var #{field.name} = this.#{field.name};\n"
            code << "            if (#{field.name} == null || #{field.name}.anyDeleted()) {\n"
            code << "                #{n_var}++;\n"
            code << "            }\n"
            code << "        }\n"
          end
        end
      end
      code << "        return #{n_var};\n"
    end
    code << "    }\n\n"
  end

  def generate_any_deleted_code
    code = "    @Override\n"
    code << "    public boolean anyDeleted() {\n"
    fields = reality_fields
    if fields.any? { |field| not field.required? or not field.single_value? }
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return false;\n"
      code << "        }\n"
      fields.each do |field|
        if field.single_value?
          unless field.required?
            code << "        if (changedFields.get(#{field.index}) && #{field.name} == null) {\n"
            code << "            return true;\n"
            code << "        }\n"
          end
        else
          if field.required?
            code << "        if (changedFields.get(#{field.index}) && #{field.name}.anyDeleted()) {\n"
            code << "            return true;\n"
            code << "        }\n"
          else
            code << "        if (changedFields.get(#{field.index})) {\n"
            code << "            var #{field.name} = this.#{field.name};\n"
            code << "            if (#{field.name} == null || #{field.name}.anyDeleted()) {\n"
            code << "                return true;\n"
            code << "            }\n"
            code << "        }\n"
          end
        end
      end
    end
    code << "        return false;\n"
    code << "    }\n\n"
  end

  def generate_clean_code
    code = "    @Override\n"
    code << "    public #@name clean() {\n"
    fields = @fields.select { |field| not field.virtual? }
    unless fields.empty?
      fields.map do |field|
        field.generate_clean_code
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
      code << "        resetStates();\n"
    end
    code << "        return this;\n"
    code << "    }\n\n"
  end

  def generate_deep_copy_code
    var_copy = variable_name('copy')
    code = "    @Override\n"
    code << "    public #@name deepCopy() {\n"
    code << "        var #{var_copy} = new #@name();\n"
    code << "        deepCopyTo(#{var_copy}, false);\n"
    code << "        return #{var_copy};\n"
    code << "    }\n\n"
  end

  def generate_deep_copy_from_code
    code = "    @Override\n"
    code << "    public void deepCopyFrom(#@name src) {\n"
    fields = @fields.select { |field| not field.virtual? }
    unless fields.empty?
      fields.map do |field|
        field.generate_deep_copy_from_code
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
    end
    code << "    }\n\n"
  end

  def generate_append_field_updates_code
    code = "    @Override\n"
    code << "    protected void appendFieldUpdates(List<Bson> updates) {\n"
    fields = reality_fields
    unless fields.empty?
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return;\n"
      code << "        }\n"
      fields.each do |field|
        code << field.generate_append_updates_code
      end
    end
    code << "    }\n\n"
  end

  def generate_load_object_node_code
    code = "    @Override\n"
    code << "    protected void loadObjectNode(JsonNode src) {\n"
    code << "        resetStates();\n"
    @fields.map do |field|
      field.generate_load_object_node_code
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "    }\n\n"
  end

  def generate_append_update_data_code
    code = "    @Override\n"
    code << "    protected void appendUpdateData(Map<Object, Object> data) {\n" 
    fields = @fields.select { |field| not field.hidden? and not field.loadonly? and not field.transient? }
    unless fields.empty?
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return;\n"
      code << "        }\n"
      fields.map do |field|
        field.generate_append_update_data_code
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
    end
    code << "    }\n\n"
  end

  def generate_to_deleted_data_return_null_code
    code = "    @Override\n"
    code << "    public Object toDeletedData() {\n"
    code << "        return null;\n"
    code << "    }\n\n"
  end

  def generate_append_deleted_data_code
    code = "    @Override\n"
    code << "    protected void appendDeletedData(Map<Object, Object> data) {\n"
    fields = @fields.select { |field| not field.hidden? and not field.loadonly? and not field.transient? }
    if fields.any? { |field| not field.required? or not field.single_value? }
      code << "        var changedFields = this.changedFields;\n"
      fields.map do |field|
        field.generate_append_deleted_data_code
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
    end
    code << "    }\n\n"
  end

  def generate_to_string_code
    code = "    @Override\n"
    code << "    public String toString() {\n"
    fields = @fields.select { |field| not field.virtual? }
    case fields.size
    when 0
      code << "        return \"#@name()\";\n"
    else
      code << "        return \"#@name(\" + \"#{fields[0].name}=\" + #{fields[0].name} +\n"
      fields[1..].each do |field|
        code << "                \", #{field.name}=\" + #{field.name} +\n"
      end
      code << "                \")\";\n"
    end
    code << "    }\n\n"
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
    when 'int', 'long', 'double', 'boolean'
      "    public static final #@type #@name = #@value;\n"
    when 'string'
      "    public static final String #@name = \"#@value\";\n"
    when 'datetime'
      "    public static final LocalDateTime #@name = #@value;\n"
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
    "BNAME_#{upper_word_name}"
  end

  def upper_word_name
    @name.gsub(/[A-Z]/) { |match| "_#{match}" }.upcase
  end

  def getter_name
    "get#{camcel_name}"
  end

  def setter_name
    "set#{camcel_name}"
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
    "    public static final String #{bname_const_field_name} = \"#@bname\";\n"
  end

  def generate_declare_code
    if virtual?
      return nil
    end
    generate_reality_declare_code
  end

  def generate_reality_declare_code
    raise "unsupported type `#@type`"
  end

  def generate_accessors_code
    code = generate_getter_code
    code << "\n"
    setter_code = generate_setter_code
    unless setter_code.nil?
      code << setter_code
      code << "\n"
    end
    if required? and %w(int long).member?(@type)
      if increment_1?
        code << "    public #{generic_type} increase#{camcel_name}() {\n"
        code << "        #{generate_field_changed_code}\n"
        code << "        return ++#@name;\n"
        code << "    }\n\n"
      end
      if increment_n?
        code << "    public #{generic_type} add#{camcel_name}(#{generic_type} #@name) {\n"
        code << "        #@name = this.#@name += #@name;\n"
        code << "        #{generate_field_changed_code}\n"
        code << "        return #@name;\n"
        code << "    }\n\n"
      end
    end
    code
  end

  def generate_getter_code
    code = "    public #{generic_type} #{getter_name}() {\n"
    if virtual?
      code << "        return #@lambda_expression;\n"
    else
      code << "        return #@name;\n"
    end
    code << "    }\n"
  end

  def generate_setter_code
    if virtual?
      return nil
    elsif required? and %w(object map list).member?(@type)
      return nil
    end
    code = "    public void set#{camcel_name}(#{generic_type} #@name) {\n"
    if loadonly? or transient?
      code << "        this.#@name = #@name;\n"
    else
      code << generate_reality_setter_code
    end 
    code << "    }\n"
  end

  def generate_reality_setter_code
    code = ''
    if required?
      code << "        Objects.requireNonNull(#@name, \"#@name must not be null\");\n"
      code << "        if (!#@name.equals(this.#@name)) {\n"
    else
      code << "        if (!Objects.equals(#@name, this.#@name)) {\n"
    end
    code << "            this.#@name = #@name;\n"
    code << "            #{generate_field_changed_code}\n"
    code << "        }\n"
  end

  def generate_field_changed_code
    if @associates.empty?
      "fieldChanged(#@index);"
    else
      "fieldsChanged(#@index, #{@associates.map { |e| e.index }.join(', ')});"
    end
  end

  def generate_changed_code
    if loadonly? or transient?
      return nil
    end
    code = "    public boolean #{@name}Changed() {\n"
    code << "        return changedFields.get(#@index);\n"
    code << "    }\n\n"
  end

  def generate_append_to_bson_code(bson_var)
    raise "unsupported type `#@type`"
  end

  def generate_append_value_to_bson_code(bson_var, bson_value_factory)
    if required?
      "        #{bson_var}.append(#{bname_const_field_name}, #{bson_value_factory});\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{bson_var}.append(#{bname_const_field_name}, #{bson_value_factory});\n"
      code << "        }\n"
    end
  end

  def generate_load_code
    if virtual? or transient?
      return nil
    end
    generate_reality_load_code
  end

  def generate_reality_load_code
    raise "unsupported type `#@type`"
  end

  def generate_append_to_json_node_code(json_node_var)
    if virtual? or transient?
      return nil
    end
    generate_reality_append_to_json_node_code(json_node_var)
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    raise "unsupported type `#@type`"
  end

  def generate_put_value_to_json_node_code(json_node_var, value_factory)
    if required?
      "        #{json_node_var}.put(#{bname_const_field_name}, #{value_factory});\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{json_node_var}.put(#{bname_const_field_name}, #{value_factory});\n"
      code << "        }\n"
    end
  end

  def generate_put_to_data_code(data_var)
    if hidden?
      return nil
    end
    if virtual?
      generate_virtual_put_to_data_code(data_var)
    else
      generate_visiable_put_to_data_code(data_var)
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #{getter_name}());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name);\n"
      code << "        }\n"
    end
  end

  def generate_visiable_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #@name);\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name);\n"
      code << "        }\n"
    end
  end

  def generate_any_updated_code
    if loadonly? or transient?
      return nil
    end
    generate_reality_any_updated_code
  end

  def generate_reality_any_updated_code
    if required?
      code = "        if (changedFields.get(#@index)) {\n"
    else
      code = "        if (changedFields.get(#@index) && #@name != null) {\n"
    end
    code << "            return true;\n"
    code << "        }\n"
  end

  def generate_clean_code
    nil
  end

  def generate_deep_copy_from_code
    "        #@name = src.#@name;\n"
  end

  def generate_append_updates_code
    generate_reality_append_updates_code("updates.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), #@name))")
  end
  
  def generate_reality_append_updates_code(append_code)
    code = "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            #{append_code};\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name == null) {\n"
      code << "                updates.add(Updates.unset(path().resolve(#{bname_const_field_name}).value()));\n"
      code << "            } else {\n"
      code << "                #{append_code};\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

  def generate_load_object_node_code
    if virtual?
      return nil
    end
    generate_reality_load_object_node_code
  end

  def generate_reality_load_object_node_code
    generate_reality_load_code
  end
  
  def generate_append_update_data_code
    code = "        if (changedFields.get(#@index)) {\n"
    if virtual?
      code << generate_virtual_append_value_to_update_data_code
    else
      code << generate_reality_append_value_to_update_data_code
    end
    code << "        }\n"
  end

  def generate_virtual_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #{getter_name}());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name);\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #@name);\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name);\n"
      code << "            }\n"
    end
  end

  def generate_append_deleted_data_code
    if required? and single_value?
      return nil
    end
    if virtual?
      generate_virtual_append_deleted_data_code
    else
      generate_reality_append_deleted_data_code
    end
  end
  
  def generate_virtual_append_deleted_data_code
    code = ''
    code << "        if (changedFields.get(#@index) && #{getter_name}() == null) {\n"
    code << "            data.put(\"#@name\", 1);\n"
    code << "        }\n"
  end

  def generate_reality_append_deleted_data_code
    code = ''
    code << "        if (changedFields.get(#@index) && #@name == null) {\n"
    code << "            data.put(\"#@name\", 1);\n"
    code << "        }\n"
  end

end

class PrimitiveFieldConf < FieldConf

  attr_reader :boxed_type

  def initialize(name, bname, type, boxed_type)
    super(name, bname, type)
    @boxed_type = boxed_type
  end

  def generic_type
    if required?
      @type
    else
      @boxed_type
    end
  end

  def generate_reality_declare_code
    if required?
      if has_default? and default_value_code != '0'
        "    private #@type #@name = #{default_value_code};\n"
      else
        "    private #@type #@name;\n"
      end
    else
      "    private #@boxed_type #@name;\n"
    end
  end

  def generate_reality_setter_code
    code = ''
    if required?
      code << "        if (#@name != this.#@name) {\n"
    else
      code << "        if (!Objects.equals(#@name, this.#@name)) {\n"
    end
    code << "            this.#@name = #@name;\n"
    code << "            #{generate_field_changed_code}\n"
    code << "        }\n"
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, @name)
  end

  def generate_clean_code
    if required?
      if has_default?
        "        #@name = #{default_value_code};\n"
      else
        "        #@name = 0;\n"
      end
    else
      "        #@name = null;\n"
    end
  end

end

class IntFieldConf < PrimitiveFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'int', 'Integer')
  end

  def required_default_value_code
    case @default.to_s.downcase
    when 'min'
      'Integer.MIN_VALUE'
    when 'max'
      'Integer.MAX_VALUE'
    else
      @default.to_s
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonInt32(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.intValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.intValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.IntegerValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

end

class LongFieldConf < PrimitiveFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'long', 'Long')
  end

  def required_default_value_code
    case @default.to_s.downcase
    when 'min'
      'Long.MIN_VALUE'
    when 'max'
      'Long.MAX_VALUE'
    else
      @default.to_s
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonInt64(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.longValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.longValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedLongValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

end

class DoubleFieldConf < PrimitiveFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'double', 'Double')
  end

  def required_default_value_code
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

  def generate_reality_declare_code
    if required?
      if has_default?
        "    private #@type #@name = #{default_value_code};\n"
      else
        "    private #@type #@name = Double.NaN;\n"
      end
    else
      "    private #@boxed_type #@name;\n"
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonDouble(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.doubleValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.doubleValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedDoubleValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_clean_code
    if required?
      if has_default?
        "        #@name = #{default_value_code};\n"
      else
        "        #@name = Double.NaN;\n"
      end
    else
      "        #@name = null;\n"
    end
  end

end

class BooleanFieldConf < PrimitiveFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'boolean', 'Boolean')
  end

  def getter_name
    if required?
      "is#{camcel_name}"
    else
      super.getter_name
    end
  end

  def required_default_value_code
    case @default.to_s.downcase
    when 'true', 1
      'true'
    else
      'false'
    end
  end

  def generate_reality_declare_code
    if required?
      if has_default?
        "    private #@type #@name = #{default_value_code};\n"
      else
        "    private #@type #@name;\n"
      end
    else
      "    private #@boxed_type #@name;\n"
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonBoolean(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.booleanValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.booleanValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedBooleanValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_clean_code
    if required?
      if has_default?
        "        #@name = #{default_value_code};\n"
      else
        "        #@name = false;\n"
      end
    else
      "        #@name = null;\n"
    end
  end

end

class StringFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'string')
  end

  def generic_type
    'String'
  end

  def required_default_value_code
    @default.to_json
  end

  def generate_reality_declare_code
    if required? and has_default?
      "    private #{generic_type} #@name = #{default_value_code};\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonString(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.stringValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.stringValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.stringValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, @name)
  end

  def generate_clean_code
    if required? and has_default?
      "        #@name = #{default_value_code};\n"
    else
      "        #@name = null;\n"
    end
  end

end

class DateTimeFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'datetime')
  end

  def generic_type
    'LocalDateTime'
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

  def generate_reality_declare_code
    if required? and has_default?
      "    private #{generic_type} #@name = #{default_value_code};\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "BsonUtil.toBsonDateTime(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        or_else_code = case @default.downcase
        when 'min'
          'orElse(LocalDateTime.MIN)'
        when 'max'
          'orElse(LocalDateTime.MAX)'
        when 'now'
          'orElseGet(LocalDateTime::now)'
        else
          "orElseGet(() -> LocalDateTime.parse(#{@default.to_json}))"
        end
        "        #@name = BsonUtil.dateTimeValue(src, #{bname_const_field_name}).#{or_else_code};\n"
      else
        "        #@name = BsonUtil.dateTimeValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.dateTimeValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "DateTimeUtil.toEpochMilli(#@name)")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_visiable_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #@name.toString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    if required? and has_default?
      "        #@name = #{default_value_code};\n"
    else
      "        #@name = null;\n"
    end
  end

  def generate_append_updates_code
    generate_reality_append_updates_code("updates.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonDateTime(#@name)))")
  end

  def generate_virtual_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name.toString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #@name.toString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name.toString());\n"
      code << "            }\n"
    end
  end

end

class ObjectIdFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'object-id')
  end

  def generic_type
    'ObjectId'
  end

  def generate_reality_declare_code
    "    private #{generic_type} #@name;\n"
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "new BsonObjectId(#@name)")
  end

  def generate_reality_load_code
    if required?
      "        #@name = BsonUtil.objectIdValue(src, #{bname_const_field_name}).orElseThrow();\n"
    else
      "        #@name = BsonUtil.objectIdValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "#@name.toHexString()")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #{getter_name}().toHexString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toHexString());\n"
      code << "        }\n"
    end
  end

  def generate_visiable_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #@name.toHexString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toHexString());\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    "        #@name = null;\n"
  end

  def generate_virtual_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #{getter_name}().toHexString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name.toHexString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #@name.toHexString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name.toHexString());\n"
      code << "            }\n"
    end
  end

end

class PrimitiveArrayFieldConf < FieldConf

  attr_reader :primitive_value_type

  def initialize(name, bname, primitive_value_type)
    super(name, bname, "#{primitive_value_type}-array")
    @primitive_value_type = primitive_value_type
  end

  def generic_type
    "#@primitive_value_type[]"
  end

  def required_default_value_code
    "new #@primitive_value_type[] { #{JSON.parse("[#@default]").join(", ")} }"
  end

  def generate_reality_declare_code
    if required? and has_default?
      "    private #{generic_type} #@name = #{default_value_code};\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_setter_code
    code = ''
    if required?
      code << "        Objects.requireNonNull(#@name, \"#@name must not be null\");\n"
    end
    code << "        if (!Arrays.equals(#@name, this.#@name)) {\n"
    code << "            this.#@name = #@name;\n"
    code << "            #{generate_field_changed_code}\n"
    code << "        }\n"
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "BsonUtil.toBsonArray(#@name)")
  end

  def generate_reality_load_code
    if required?
      if has_default?
        "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(src, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(src, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(src, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    array_node_var = variable_name("ArrayNode")
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{array_node_var} = jsonNode.arrayNode(#@name.length);\n"
      code << "        for (var i = 0; i < #@name.length; i++) {\n"
      code << "            #{array_node_var}.add(#@name[i]);\n"
      code << "        }\n"
      code << "        #{json_node_var}.set(#{bname_const_field_name}, #{array_node_var});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{array_node_var} = jsonNode.arrayNode(#@name.length);\n"
      code << "            for (var i = 0; i < #@name.length; i++) {\n"
      code << "                #{array_node_var}.add(#@name[i]);\n"
      code << "            }\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, #{array_node_var});\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    if required? and has_default?
      "        #@name = #{default_value_code};\n"
    else
      "        #@name = null;\n"
    end
  end

  def generate_deep_copy_from_code
    code = ''
    code << "        var #@name = src.#@name;\n"
    code << "        if (#@name == null) {\n"
    code << "            this.#@name = null;\n"
    code << "        } else {\n"
    code << "            this.#@name = Arrays.copyOf(#@name, #@name.length);\n"
    code << "        }\n"
  end

  def generate_append_updates_code
    generate_reality_append_updates_code("updates.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonArray(#@name)))")
  end

end

class IntArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'int')
  end

end

class LongArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'long')
  end

end

class DoubleArrayFieldConf < PrimitiveArrayFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'double')
  end

end

class StdListFieldConf < FieldConf
  
  def initialize(name, bname)
    super(name, bname, 'std-list')
  end

  def generic_type
    if @value == 'object'
      "List<#@model>"
    else
      "List<#{value_type}>"
    end
  end

  def generate_reality_declare_code
    if required?
      "    private #{generic_type} #@name = List.of();\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "BsonUtil.toBsonArray(#@name, #{to_array_mapper_code})")
  end

  def to_array_mapper_code
    case @value
    when 'int'
      'BsonInt32::new'
    when 'long'
      'BsonInt64::new'
    when 'double'
      'BsonDouble::new'
    when 'boolean'
      'BsonBoolean::new'
    when 'string'
      'BsonString::new'
    when 'datetime'
      'BsonUtil::toBsonDateTime'
    when 'object'
      raise 'std-list must be either `loadonly` or `transient` when has object values'
    else
      raise "unsupported value type `#@value` for std-list"
    end
  end

  def generate_reality_load_code
    case @value
    when 'int'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::intValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::intValue).orElse(null);\n"
      end
    when 'long'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::longValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::longValue).orElse(null);\n"
      end
    when 'double'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::doubleValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonNumber::doubleValue).orElse(null);\n"
      end
    when 'boolean'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonBoolean::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonBoolean::getValue).orElse(null);\n"
      end
    when 'string'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonString::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonString::getValue).orElse(null);\n"
      end
    when 'datetime'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonUtil::toLocalDateTime).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonUtil::toLocalDateTime).orElse(null);\n"
      end
    when 'object-id'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonObjectId::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, BsonObjectId::getValue).orElse(null);\n"
      end
    when 'object'
      if required?
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, (BsonDocument v) -> new #@model().load(v)).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(src, #{bname_const_field_name}, (BsonDocument v) -> new #@model().load(v)).orElse(null);\n"
      end
    else
      raise "unsupported value type `#@value` for `std-list`"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    array_node_var = variable_name("ArrayNode")
    add_code = case @value
    when 'int', 'long', 'boolean', 'string'
      "#@name.forEach(#{array_node_var}::add);"
    when 'datetime'
      "#@name.stream().mapToInt(DateTimeUtil::toEpochMilli).forEach(#{array_node_var}::add);"
    when 'object-id'
      "#@name.stream().map(ObjectId::toHexString).forEach(#{array_node_var}::add);"
    when 'object'
      "#@name.stream().map(#@model::toJsonNode).forEach(#{array_node_var}::add);"
    else
      raise "unsupported value type `#@value` for std-list"
    end
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{array_node_var} = jsonNode.arrayNode(#@name.size());\n"
      code << "        #{add_code}\n"
      code << "        #{json_node_var}.set(#{bname_const_field_name}, #{array_node_var});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{array_node_var} = jsonNode.arrayNode(#@name.size());\n"
      code << "            #{add_code}\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, #{array_node_var});\n"
      code << "        }\n"
    end
  end

  def data_value_convert_code
    case @value
    when 'object'
      ".stream().map(#@model::toData).toList()"
    when 'int', 'long', 'double', 'boolean', 'string'
      ''
    when 'datetime'
      '.stream().map(LocalDateTime::toString).toList()'
    when 'object-id'
      '.stream().map(ObjectId::toHexString).toList()'
    else
      raise "unsupport value type `@value` for std-list"
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #{getter_name}()#{data_value_convert_code});\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name#{data_value_convert_code});\n"
      code << "        }\n"
    end
  end

  def generate_visiable_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #@name#{data_value_convert_code});\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name#{data_value_convert_code});\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    if required? and has_default?
      "        #@name = List.of();\n"
    else
      "        #@name = null;\n"
    end
  end

  def generate_deep_copy_from_code
    code = ''
    case @value
    when 'int', 'long', 'double', 'boolean', 'string', 'datetime', 'object-id'
      code << "        var #@name = src.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            this.#@name = new ArrayList<>(src.#@name);\n"
      code << "        }\n"
    when 'object'
      var_copy = variable_name('Copy')
      var_copy_value = variable_name('CopyValue')
      code << "        var #@name = src.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            var #{var_copy} = new ArrayList<#@model>(#@name.size());\n"
      code << "            for (var #{var_copy_value} : #@name) {\n"
      code << "                if (#{var_copy_value} == null) {\n"
      code << "                    #{var_copy}.add(null);\n"
      code << "                } else {\n"
      code << "                    #{var_copy}.add(#{var_copy_value}.deepCopy());\n"
      code << "                }\n"
      code << "            }\n"
      code << "            this.#@name = #{var_copy};\n"
      code << "        }\n"
    else
      raise "unsupport value type `@value` for std-list"
    end
  end

  def generate_append_updates_code
    generate_reality_append_updates_code("updates.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonArray(#@name, #{to_array_mapper_code})))")
  end

  def generate_reality_load_object_node_code
    case @value
    when 'int'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::intValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::intValue).orElse(null);\n"
      end
    when 'long'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::longValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::longValue).orElse(null);\n"
      end
    when 'double'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::doubleValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::doubleValue).orElse(null);\n"
      end
    when 'boolean'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::booleanValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::booleanValue).orElse(null);\n"
      end
    when 'string'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::textValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, JsonNode::textValue).orElse(null);\n"
      end
    when 'datetime'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> DateTimeUtil.ofEpochMilli(value.longValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> DateTimeUtil.ofEpochMilli(value.longValue())).orElse(null);\n"
      end
    when 'object-id'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> new ObjectId(value.textValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> new ObjectId(value.textValue())).orElse(null);\n"
      end
    when 'object'
      if required?
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> new #@model().load(v)).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(src, #{bname_const_field_name}, v -> new #@model().load(v)).orElse(null);\n"
      end
    else
      raise "unsupported value type `#@value` for `std-list`"
    end
  end

  def generate_virtual_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #{getter_name}()#{data_value_convert_code});\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name#{data_value_convert_code});\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code
    if required?
      "            data.put(\"#@name\", #@name#{data_value_convert_code});\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                data.put(\"#@name\", #@name#{data_value_convert_code});\n"
      code << "            }\n"
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

  def generate_reality_setter_code
    code = ''
    code << "        if (#@name != null) {\n"
    code << "            #@name.mustUnbound();\n"
    code << "            this.#@name = #@name.parent(this).key(#{bname_const_field_name}).index(#@index).fullyUpdate(true);\n"
    code << "            #{generate_field_changed_code}\n"
    code << "        } else {\n"
    code << "            #@name = this.#@name;\n"
    code << "            if (#@name != null) {\n"
    code << "                #@name.unbind();\n"
    code << "                this.#@name = null;\n"
    code << "                #{generate_field_changed_code}\n"
    code << "            }\n"
    code << "        }\n"
  end

  def generate_append_to_bson_code(bson_var)
    generate_append_value_to_bson_code(bson_var, "#@name.toBson()")
  end

  def generate_load_model_code(factor)
    if required?
      "        BsonUtil.documentValue(src, #{bname_const_field_name}).ifPresentOrElse(#@name::load, #@name::clean);\n"
    else
      code = "        BsonUtil.documentValue(src, #{bname_const_field_name}).ifPresentOrElse(\n"
      code << "                v -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                    }\n"
      code << "                    this.#@name = #{factor}.load(v).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      code << "                },\n"
      code << "                () -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                        this.#@name = null;\n"
      code << "                    }\n"
      code << "                }\n"
      code << "        );\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    if required?
      "        #{json_node_var}.set(#{bname_const_field_name}, #@name.toJsonNode());\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, #@name.toJsonNode());\n"
      code << "        }\n"
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #{getter_name}().toData());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toData());\n"
      code << "        }\n"
    end
  end

  def generate_visiable_put_to_data_code(data_var)
    if required?
      "        data.put(\"#@name\", #@name.toData());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            data.put(\"#@name\", #@name.toData());\n"
      code << "        }\n"
    end
  end
  
  def generate_reality_any_updated_code
    if required?
      code = "        if (changedFields.get(#@index) && #@name.anyUpdated()) {\n"
      code << "            return true;\n"
    else
      code = "        if (changedFields.get(#@index)) {\n"
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null && #@name.anyUpdated()) {\n"
      code << "                return true;\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

  def generate_clean_code
    if required?
      "        #@name.clean();\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #@name.clean().unbind();\n"
      code << "            this.#@name = null;\n"
      code << "        }\n"
    end
  end

  def generate_deep_copy_from_code
    if required?
      "        src.#@name.deepCopyTo(#@name, false);\n"
    else
      code = "        var #@name = src.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            this.#@name = #@name.deepCopy().parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      code << "        }\n"
    end
  end

  def generate_append_updates_code
    generate_reality_append_updates_code("#@name.appendUpdates(updates)")
  end
  
  def generate_load_model_object_node_code(factor)
    if required?
      "        BsonUtil.objectValue(src, #{bname_const_field_name}).ifPresentOrElse(#@name::load, #@name::clean);\n"
    else
      code = "        BsonUtil.objectValue(src, #{bname_const_field_name}).ifPresentOrElse(\n"
      code << "                v -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                    }\n"
      code << "                    this.#@name = #{factor}.load(v).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      code << "                },\n"
      code << "                () -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                        this.#@name = null;\n"
      code << "                    }\n"
      code << "                }\n"
      code << "        );\n"
    end
  end

  def generate_virtual_append_value_to_update_data_code
    code = ''
    var_update_data = variable_name("UpdateData")
    if required?
      code << "            var #{var_update_data} = #{getter_name}().toUpdateData();\n"
      code << "            if (#{var_update_data} != null) {\n"
      code << "                data.put(\"#@name\", #{var_update_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                var #{var_update_data} = #@name.toUpdateData();\n"
      code << "                if (#{var_update_data} != null) {\n"
      code << "                    data.put(\"#@name\", #{var_update_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code
    code = ''
    var_update_data = variable_name("UpdateData")
    if required?
      code << "            var #{var_update_data} = #@name.toUpdateData();\n"
      code << "            if (#{var_update_data} != null) {\n"
      code << "                data.put(\"#@name\", #{var_update_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                var #{var_update_data} = #@name.toUpdateData();\n"
      code << "                if (#{var_update_data} != null) {\n"
      code << "                    data.put(\"#@name\", #{var_update_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
  end
  
  def generate_virtual_append_deleted_data_code
    var_deleted_data = variable_name('DeletedData')
    code = ''
    code << "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            var #{var_deleted_data} = #{getter_name}().toDeletedData();\n"
      code << "            if (#{var_deleted_data} != null) {\n"
      code << "                data.put(\"#@name\", 1);\n"
      code << "            }\n"
    else
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name == null) {\n"
      code << "                data.put(\"#@name\", 1);\n"
      code << "            } else {\n"
      code << "                var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "                if (#{var_deleted_data} != null) {\n"
      code << "                    data.put(\"#@name\", 1);\n"
      code << "                }\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

  def generate_reality_append_deleted_data_code
    var_deleted_data = variable_name('DeletedData')
    code = ''
    code << "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "            if (#{var_deleted_data} != null) {\n"
      code << "                data.put(\"#@name\", 1);\n"
      code << "            }\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name == null) {\n"
      code << "                data.put(\"#@name\", 1);\n"
      code << "            } else {\n"
      code << "                var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "                if (#{var_deleted_data} != null) {\n"
      code << "                    data.put(\"#@name\", 1);\n"
      code << "                }\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

end

class ObjectFieldConf < ModelFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'object')
  end

  def generic_type
    @model
  end

  def generate_reality_declare_code
    if required?
      "    private final #{generic_type} #@name = new #{generic_type}().parent(this).key(#{bname_const_field_name}).index(#@index);\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_load_code
    generate_load_model_code("new #{generic_type}()")
  end

  def generate_reality_load_object_node_code
    generate_load_model_object_node_code("new #{generic_type}()")
  end

end

class MapFieldConf < ModelFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'map')
  end

  def generic_type
    if @value == 'object'
      "DefaultMapModel<#{key_type}, #@model>"
    else
      "SingleValueMapModel<#{key_type}, #{value_type}>"
    end
  end

  def generate_reality_declare_code
    if required?
      "    private final #{generic_type} #@name = #{map_init_code}.parent(this).key(#{bname_const_field_name}).index(#@index);\n"
    else
      "    private #{generic_type} #@name;\n"
    end
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
      single_value_type = case @value
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

  def generate_reality_load_code
    generate_load_model_code(map_init_code)
  end

  def generate_reality_load_object_node_code
    generate_load_model_object_node_code(map_init_code)
  end

end

class ListFieldConf < ModelFieldConf
  
  def initialize(name, bname)
    super(name, bname, 'list')
  end

  def generic_type
    if @value == 'object'
      "DefaultListModel<#@model>"
    else
      raise "unsupported value type `#@value` for list"
    end
  end

  def generate_reality_declare_code
    if required?
      "    private final #{generic_type} #@name = new #{generic_type}(#@model::new).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_load_code
    generate_load_model_code("new #{generic_type}(#@model::new)")
  end

  def generate_reality_load_object_node_code
    generate_load_model_object_node_code("new #{generic_type}(#@model::new)")
  end

end

cfg = YAML.load_file(ARGV[0])

if cfg.has_key? 'java-package'
  cfg['package'] = cfg['java-package']
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
  package_dir = File.join(ARGV[1], File.join(cfg['package'].split('.')))
  unless File.directory?(package_dir)
    FileUtils.mkdir_p(package_dir)
  end
  filename = "#{model.name}.java"
  puts "Generating #{filename} ... (on path: #{package_dir})"
  code = model.generate_class_code cfg['package']
  File.open(File.join(package_dir, filename), 'w') do |io|
    io.syswrite(code)
  end
  puts "OK"
end

puts "Done."
