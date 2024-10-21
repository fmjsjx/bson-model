#!/usr/bin/env ruby

require "set"
require 'yaml'
require 'json'
require 'fileutils'


class ModelConf

  class << self
    def from(model_cfg)
      cfg = ModelConf.new(model_cfg['name'], model_cfg['type'])
      if model_cfg.has_key? 'imports'
        model_cfg['imports'].each do |import_cfg|
          cfg.append_import(import_cfg)
        end
      end
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
              :imports,
              :consts,
              :fields

  def initialize(name, type)
    @name = name
    @type = type
    @imports = []
    @consts = []
    @fields = []
    @imports_javas = Set.new
    @imports_others = Set.new
  end

  def append_import(import)
    @imports << import
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
    @imports_others += ['com.alibaba.fastjson2.JSONObject',
                        'com.fasterxml.jackson.databind.JsonNode',
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
    if @consts.any? { |const| const.type == 'date' }
      @imports_javas << 'java.time.LocalDate'
    end
    if @fields.any? { |field| field.type == 'datetime' or (field.type == 'std-list' and field.value == 'datetime') }
      @imports_others << 'com.github.fmjsjx.libcommon.util.DateTimeUtil'
      @imports_javas << 'java.time.LocalDateTime'
    end
    if @fields.any? { |field| field.type == 'date' or (field.type == 'std-list' and field.value == 'date') }
      @imports_others << 'com.github.fmjsjx.libcommon.util.DateTimeUtil'
      @imports_javas << 'java.time.LocalDate'
    end
    #  Fix issue "Missing import part for ObjectId"
    #  see: https://github.com/fmjsjx/bson-model/issues/72
    if @fields.any? { |field| field.type == 'object-id' }
      @imports_others << 'org.bson.types.ObjectId'
    end
    # Check if should import com.alibaba.fastjson2.JSONArray
    if @fields.any? { |field| ['int-array', 'long-array', 'double-array', 'std-list'].member?(field.type) }
      @imports_others << 'com.alibaba.fastjson2.JSONArray'
    end
    @imports.each do |import|
      if import.start_with?('java.')
        @imports_javas << import
      else
        @imports_others << import
      end
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
    code << generate_to_fastjson2_node_code
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
    code << generate_load_json_object_code
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
      code << "        var #{bson_var} = new BsonDocument();\n"
      fields.map do |field|
        field.generate_append_to_bson_code(bson_var)
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
      code << "        return #{bson_var};\n"
    end
    code << "    }\n\n"
  end

  def generate_load_code
    src_var = variable_name('src')
    code = "    @Override\n"
    code << "    public #@name load(BsonDocument #{src_var}) {\n"
    code << "        resetStates();\n"
    @fields.map do |field|
      field.generate_load_code(src_var)
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

  def generate_to_fastjson2_node_code
    node_var = variable_name('jsonObject')
    code = "    @Override\n"
    code << "    public JSONObject toFastjson2Node() {\n"
    code << "        var #{node_var} = new JSONObject();\n"
    @fields.map do |field|
      field.generate_append_to_fastjson2_node_code(node_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "        return #{node_var};\n"
    code << "    }\n\n"
  end

  def generate_to_data_code
    data_var = variable_name('data')
    code = "    @Override\n"
    code << "    public Map<Object, Object> toData() {\n"
    code << "        var #{data_var} = new LinkedHashMap<>();\n"
    @fields.map do |field|
      field.generate_put_to_data_code(data_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "        return #{data_var};\n"
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
    copy_var = variable_name('copy')
    code = "    @Override\n"
    code << "    public #@name deepCopy() {\n"
    code << "        var #{copy_var} = new #@name();\n"
    code << "        deepCopyTo(#{copy_var}, false);\n"
    code << "        return #{copy_var};\n"
    code << "    }\n\n"
  end

  def generate_deep_copy_from_code
    src_var = variable_name('src')
    code = "    @Override\n"
    code << "    public void deepCopyFrom(#@name #{src_var}) {\n"
    fields = @fields.select { |field| not field.virtual? }
    unless fields.empty?
      fields.map do |field|
        field.generate_deep_copy_from_code(src_var)
      end.select do |c|
        not c.nil?
      end.each do |c|
        code << c
      end
    end
    code << "    }\n\n"
  end

  def generate_append_field_updates_code
    updates_var = variable_name('updates')
    code = "    @Override\n"
    code << "    protected void appendFieldUpdates(List<Bson> #{updates_var}) {\n"
    fields = reality_fields
    unless fields.empty?
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return;\n"
      code << "        }\n"
      fields.each do |field|
        code << field.generate_append_updates_code(updates_var)
      end
    end
    code << "    }\n\n"
  end

  def generate_load_object_node_code
    src_var = variable_name('src')
    code = "    @Override\n"
    code << "    protected void loadObjectNode(JsonNode #{src_var}) {\n"
    code << "        resetStates();\n"
    @fields.map do |field|
      field.generate_load_object_node_code(src_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "    }\n\n"
  end

  def generate_load_json_object_code
    src_var = variable_name('src')
    code = "    @Override\n"
    code << "    protected void loadJSONObject(JSONObject #{src_var}) {\n"
    code << "        resetStates();\n"
    @fields.map do |field|
      field.generate_load_json_object_code(src_var)
    end.select do |c|
      not c.nil?
    end.each do |c|
      code << c
    end
    code << "    }\n\n"
  end

  def generate_append_update_data_code
    data_var = variable_name('data')
    code = "    @Override\n"
    code << "    protected void appendUpdateData(Map<Object, Object> #{data_var}) {\n"
    fields = @fields.select { |field| not field.hidden? and not field.loadonly? and not field.transient? }
    unless fields.empty?
      code << "        var changedFields = this.changedFields;\n"
      code << "        if (changedFields.isEmpty()) {\n"
      code << "            return;\n"
      code << "        }\n"
      fields.map do |field|
        field.generate_append_update_data_code(data_var)
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
    code << "    public Map<Object, Object> toDeletedData() {\n"
    code << "        return null;\n"
    code << "    }\n\n"
  end

  def generate_append_deleted_data_code
    data_var = variable_name('data')
    code = "    @Override\n"
    code << "    protected void appendDeletedData(Map<Object, Object> #{data_var}) {\n"
    fields = @fields.select { |field| not field.hidden? and not field.loadonly? and not field.transient? }
    if fields.any? { |field| not field.required? or not field.single_value? }
      code << "        var changedFields = this.changedFields;\n"
      fields.map do |field|
        field.generate_append_deleted_data_code(data_var)
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
      code << "        return \"#@name(\" + \"#{fields[0].name}=\" + #{fields[0].generate_to_string_code} +\n"
      fields[1..].each do |field|
        code << "                \", #{field.name}=\" + #{field.generate_to_string_code} +\n"
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
      type = field_cfg['type'].split(' ').select { |x| x != 'const' }.join(' ')
      value = field_cfg['value']
      ConstConf.new(name, type, value)
    end
  end

  attr_reader :name, :type, :type_1, :type_2, :value

  def initialize(name, type, value)
    @name = name
    @type, @type_1, @type_2 = type.split(' ')
    @value = value
  end

  def generate_declare_code
    case @type
    when 'int', 'long', 'double', 'boolean'
      "    public static final #@type #@name = #@value;\n"
    when 'string'
      "    public static final String #@name = \"#@value\";\n"
    when 'date'
      "    public static final LocalDate #@name = #@value;\n"
    when 'datetime'
      "    public static final LocalDateTime #@name = #@value;\n"
    when 'std-list'
      if @type_1.nil?
        raise "value type must not be nil when type is std-list"
      end
      case @type_1
      when 'int'
        "    public static final List<Integer> #@name = #@value;\n"
      when 'long'
        "    public static final List<Long> #@name = #@value;\n"
      when 'double'
        "    public static final List<Double> #@name = #@value;\n"
      when 'string'
        "    public static final List<String> #@name = #@value;\n"
      else
        raise "unsupported value type #@type_1 for const type std-list"
      end
    when 'std-set'
      if @type_1.nil?
        raise "value type must not be nil when type is std-set"
      end
      case @type_1
      when 'int'
        "    public static final Set<Integer> #@name = #@value;\n"
      when 'long'
        "    public static final Set<Long> #@name = #@value;\n"
      when 'double'
        "    public static final Set<Double> #@name = #@value;\n"
      when 'string'
        "    public static final Set<String> #@name = #@value;\n"
      else
        raise "unsupported value type #@type_1 for const type std-set"
      end
    else
      raise "unsupported const type #@type"
    end
  end

end

class FieldConf

  class << self
    def from(field_cfg)
      name, bname, dname = field_cfg['name'].split(' ')
      if bname.nil?
        bname = name
      end
      if dname.nil?
        dname = name
      end
      type = field_cfg['type'].split(' ')[0]
      cfg = case type
      when 'int'
        IntFieldConf.new(name, bname, dname)
      when 'long'
        LongFieldConf.new(name, bname, dname)
      when 'double'
        DoubleFieldConf.new(name, bname, dname)
      when 'boolean'
        BooleanFieldConf.new(name, bname, dname)
      when 'string'
        StringFieldConf.new(name, bname, dname)
      when 'date'
        DateFieldConf.new(name, bname, dname)
      when 'datetime'
        DateTimeFieldConf.new(name, bname, dname)
      when 'object-id'
        ObjectIdFieldConf.new(name, bname, dname)
      when 'uuid'
        UUIDFieldConf.new(name, bname, dname)
      when 'uuid-legacy'
        UUIDFieldConf.new(name, bname, dname, true)
      when 'int-array'
        IntArrayFieldConf.new(name, bname, dname)
      when 'long-array'
        LongArrayFieldConf.new(name, bname, dname)
      when 'double-array'
        DoubleArrayFieldConf.new(name, bname, dname)
      when 'std-list'
        StdListFieldConf.new(name, bname, dname)
      when 'object'
        ObjectFieldConf.new(name, bname, dname)
      when 'map'
        MapFieldConf.new(name, bname, dname)
      when 'list'
        ListFieldConf.new(name, bname, dname)
      when 'bson-document'
        BsonDocumentFieldConf.new(name, bname, dname)
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
              :dname,
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

  def initialize(name, bname, dname, type)
    @name = name
    @bname = bname
    @dname = dname
    @type = type
    @value = nil
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

  def variable_name_global(var_name)
    @parent_model.variable_name(var_name)
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
    if transient?
      generate_transient_declare_code
    elsif loadonly?
      generate_loadonly_declare_code
    else
      generate_reality_declare_code
    end
  end

  def generate_transient_declare_code
    generate_reality_declare_code
  end

  def generate_loadonly_declare_code
    generate_transient_declare_code
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

  def generate_append_to_bson_code(bsovar_n)
    raise "unsupported type `#@type`"
  end

  def generate_append_value_to_bson_code(bsovar_n, bson_value_factory)
    if required?
      "        #{bsovar_n}.append(#{bname_const_field_name}, #{bson_value_factory});\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{bsovar_n}.append(#{bname_const_field_name}, #{bson_value_factory});\n"
      code << "        }\n"
    end
  end

  def generate_load_code(src_var)
    if virtual? or transient?
      return nil
    end
    generate_reality_load_code(src_var)
  end

  def generate_reality_load_code(src_var)
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

  def generate_append_to_fastjson2_node_code(node_var)
    if virtual? or transient?
      return nil
    end
    generate_reality_append_to_fastjson2_node_code(node_var)
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    raise "unsupported type `#@type`"
  end

  def generate_put_value_to_fastjson2_node_code(node_var, value_factory)
    if required?
      "        #{node_var}.put(#{bname_const_field_name}, #{value_factory});\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{node_var}.put(#{bname_const_field_name}, #{value_factory});\n"
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
      generate_visible_put_to_data_code(data_var)
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name);\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name);\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name);\n"
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

  def generate_deep_copy_from_code(src_var)
    "        #@name = #{src_var}.#@name;\n"
  end

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), #@name))")
  end

  def generate_reality_append_updates_code(updates_var, append_code)
    code = "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            #{append_code};\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name == null) {\n"
      code << "                #{updates_var}.add(Updates.unset(path().resolve(#{bname_const_field_name}).value()));\n"
      code << "            } else {\n"
      code << "                #{append_code};\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

  def generate_load_object_node_code(src_var)
    if virtual? or transient?
      return nil
    end
    generate_reality_load_object_node_code(src_var)
  end

  def generate_reality_load_object_node_code(src_var)
    generate_reality_load_code(src_var)
  end

  def generate_load_json_object_code(src_var)
    if virtual? or transient?
      return nil
    end
    generate_reality_load_json_object_code(src_var)
  end

  def generate_reality_load_json_object_code(src_var)
    generate_reality_load_code(src_var)
  end

  def generate_append_update_data_code(data_var)
    code = "        if (changedFields.get(#@index)) {\n"
    if virtual?
      code << generate_virtual_append_value_to_update_data_code(data_var)
    else
      code << generate_reality_append_value_to_update_data_code(data_var)
    end
    code << "        }\n"
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name);\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name);\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name);\n"
      code << "            }\n"
    end
  end

  def generate_append_deleted_data_code(data_var)
    if required? and single_value?
      return nil
    end
    if virtual?
      generate_virtual_append_deleted_data_code(data_var)
    else
      generate_reality_append_deleted_data_code(data_var)
    end
  end

  def generate_virtual_append_deleted_data_code(data_var)
    code = ''
    code << "        if (changedFields.get(#@index) && #{getter_name}() == null) {\n"
    code << "            #{data_var}.put(\"#@dname\", 1);\n"
    code << "        }\n"
  end

  def generate_reality_append_deleted_data_code(data_var)
    code = ''
    code << "        if (changedFields.get(#@index) && #@name == null) {\n"
    code << "            #{data_var}.put(\"#@dname\", 1);\n"
    code << "        }\n"
  end

  def generate_to_string_code
    @name
  end

end

class PrimitiveFieldConf < FieldConf

  attr_reader :boxed_type

  def initialize(name, bname, dname, type, boxed_type)
    super(name, bname, dname, type)
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
  
  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, @name)
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

  def initialize(name, bname, dname)
    super(name, bname, dname, 'int', 'Integer')
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonInt32(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.intValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.intValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.integerValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

end

class LongFieldConf < PrimitiveFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'long', 'Long')
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonInt64(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.longValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.longValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedLongValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

end

class DoubleFieldConf < PrimitiveFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'double', 'Double')
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonDouble(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.doubleValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.doubleValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedDoubleValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
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

  def initialize(name, bname, dname)
    super(name, bname, dname, 'boolean', 'Boolean')
  end

  def getter_name
    if required?
      "is#{camcel_name}"
    else
      "get#{camcel_name}"
    end
  end

  def required_default_value_code
    case @default.to_s.downcase
    when 'true', '1'
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonBoolean(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.boxedBooleanValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.boxedBooleanValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.boxedBooleanValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
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

  def initialize(name, bname, dname)
    super(name, bname, dname, 'string')
  end

  def generic_type
    'String'
  end

  def required_default_value_code
    @default.to_json
  end

  def generate_reality_declare_code
    if required?
      if has_default?
        "    private #{generic_type} #@name = #{default_value_code};\n"
      else
        "    private #{generic_type} #@name = \"\";\n"
      end
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonString(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, @name)
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, @name)
  end

  def generate_clean_code
    if required?
      if has_default?
        "        #@name = #{default_value_code};\n"
      else
        "        #@name = \"\";\n"
      end
    else
      "        #@name = null;\n"
    end
  end

end

class DateFieldConf < FieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'date')
  end

  def generic_type
    'LocalDate'
  end

  def required_default_value_code
    case @default.downcase
    when 'min'
      'LocalDate.MIN'
    when 'max'
      'LocalDate.MAX'
    when 'now'
      'LocalDate.now()'
    else
      if @parent_model.consts.any? { |const| const.name == @default }
        @default
      else
        "LocalDate.parse(#{@default.to_json})"
      end
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
    generate_append_value_to_bson_code(bson_var, "new BsonInt32(DateTimeUtil.toNumber(#@name))")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        or_else_code = case @default.downcase
        when 'min'
          'orElse(LocalDate.MIN)'
        when 'max'
          'orElse(LocalDate.MAX)'
        when 'now'
          'orElseGet(LocalDate::now)'
        else
          if @parent_model.consts.any? { |const| const.name == @default }
            "orElse(#@default)"
          else
            "orElseGet(() -> LocalDate.parse(#{@default.to_json}))"
          end
        end
        "        #@name = BsonUtil.intValue(#{src_var}, #{bname_const_field_name}).stream().mapToObj(DateTimeUtil::toDate).findFirst().#{or_else_code};\n"
      else
        "        #@name = BsonUtil.intValue(#{src_var}, #{bname_const_field_name}).stream().mapToObj(DateTimeUtil::toDate).findFirst().orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.intValue(#{src_var}, #{bname_const_field_name}).stream().mapToObj(DateTimeUtil::toDate).findFirst().orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "DateTimeUtil.toNumber(#@name)")
  end
  
  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, "DateTimeUtil.toNumber(#@name)")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
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

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), DateTimeUtil.toNumber(#@name)))")
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

end

class DateTimeFieldConf < FieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'datetime')
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
      if @parent_model.consts.any? { |const| const.name == @default }
        @default
      else
        "LocalDateTime.parse(#{@default.to_json})"
      end
    end
  end

  def generate_reality_declare_code
    if required? and has_default?
      "    private #{generic_type} #@name = #{default_value_code};\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "BsonUtil.toBsonDateTime(#@name)")
  end

  def generate_reality_load_code(src_var)
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
          if @parent_model.consts.any? { |const| const.name == @default }
            "orElse(#@default)"
          else
            "orElseGet(() -> LocalDateTime.parse(#{@default.to_json}))"
          end
        end
        "        #@name = BsonUtil.dateTimeValue(#{src_var}, #{bname_const_field_name}).#{or_else_code};\n"
      else
        "        #@name = BsonUtil.dateTimeValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.dateTimeValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "DateTimeUtil.toEpochMilli(#@name)")
  end
  
  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, "DateTimeUtil.toEpochMilli(#@name)")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
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

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonDateTime(#@name)))")
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

end

class ObjectIdFieldConf < FieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'object-id')
  end

  def generic_type
    'ObjectId'
  end

  def generate_reality_declare_code
    "    private #{generic_type} #@name;\n"
  end

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "new BsonObjectId(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      "        #@name = BsonUtil.objectIdValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
    else
      "        #@name = BsonUtil.objectIdValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "#@name.toHexString()")
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, "#@name.toHexString()")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}().toHexString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    "        #@name = null;\n"
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}().toHexString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toHexString());\n"
      code << "            }\n"
    end
  end

end

class UUIDFieldConf < FieldConf

  attr_reader :legacy

  def initialize(name, bname, dname, legacy = false)
    super(name, bname, dname, legacy ? 'uuid-legacy' : 'uuid')
    @legacy = legacy
  end

  def generic_type
    'UUID'
  end

  def generate_reality_declare_code
    "    private #{generic_type} #@name;\n"
  end

  def generate_append_to_bson_code(bsovar_n)
    if @legacy
      generate_append_value_to_bson_code(bsovar_n, "BsonUtil.toBsonBinaryUuidLegacy(#@name)")
    else
      generate_append_value_to_bson_code(bsovar_n, "BsonUtil.toBsonBinary(#@name)")
    end
  end

  def generate_reality_load_code(src_var)
    if @legacy
      if required?
        "        #@name = BsonUtil.uuidLegacyValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      else
        "        #@name = BsonUtil.uuidLegacyValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
      end
    else
      if required?
        "        #@name = BsonUtil.uuidValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      else
        "        #@name = BsonUtil.uuidValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
      end
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    generate_put_value_to_json_node_code(json_node_var, "#@name.toString()")
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    generate_put_value_to_fastjson2_node_code(node_var, "#@name.toString()")
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    "        #@name = null;\n"
  end

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonBinary(#@name)))")
  end

  def generate_reality_load_object_node_code(src_var)
    if required?
      "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).map(UUID::fromString).orElseThrow();\n"
    else
      "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).map(UUID::fromString).orElse(null);\n"
    end
  end

  def generate_reality_load_json_object_code(src_var)
    if required?
      "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).map(UUID::fromString).orElseThrow();\n"
    else
      "        #@name = BsonUtil.stringValue(#{src_var}, #{bname_const_field_name}).map(UUID::fromString).orElse(null);\n"
    end
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}().toString());\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name.toString());\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name.toString());\n"
      code << "            }\n"
    end
  end

end

class PrimitiveArrayFieldConf < FieldConf

  attr_reader :primitive_value_type

  def initialize(name, bname, dname, primitive_value_type)
    super(name, bname, dname, "#{primitive_value_type}-array")
    @primitive_value_type = primitive_value_type
  end

  def generic_type
    "#@primitive_value_type[]"
  end

  def required_default_value_code
    "new #@primitive_value_type[] { #{JSON.parse("[#@default]").join(", ")} }"
  end

  def generate_reality_declare_code
    if required?
      if has_default?
        "    private #{generic_type} #@name = #{default_value_code};\n"
      else
        "    private #{generic_type} #@name = new #@primitive_value_type[] {};\n"
      end
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "BsonUtil.toBsonArray(#@name)")
  end

  def generate_reality_load_code(src_var)
    if required?
      if has_default?
        "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(#{src_var}, #{bname_const_field_name}).orElse(#{default_value_code});\n"
      else
        "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
      end
    else
      "        #@name = BsonUtil.#{@primitive_value_type}ArrayValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    var_array_node = variable_name("ArrayNode")
    v_var = variable_name_global('v')
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{var_array_node} = jsonNode.arrayNode(#@name.length);\n"
      code << "        for (var #{v_var} : #@name) {\n"
      code << "            #{var_array_node}.add(#{v_var});\n"
      code << "        }\n"
      code << "        #{json_node_var}.set(#{bname_const_field_name}, #{var_array_node});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{var_array_node} = jsonNode.arrayNode(#@name.length);\n"
      code << "            for (var #{v_var} : #@name) {\n"
      code << "                #{var_array_node}.add(#{v_var});\n"
      code << "            }\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, #{var_array_node});\n"
      code << "        }\n"
    end
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    var_json_array = variable_name("JsonArray")
    v_var = variable_name_global('v')
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{var_json_array} = new JSONArray(#@name.length);\n"
      code << "        for (var #{v_var} : #@name) {\n"
      code << "            #{var_json_array}.add(#{v_var});\n"
      code << "        }\n"
      code << "        #{node_var}.put(#{bname_const_field_name}, #{var_json_array});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{var_json_array} = new JSONArray(#@name.length);\n"
      code << "            for (var #{v_var} : #@name) {\n"
      code << "                #{var_json_array}.add(#{v_var});\n"
      code << "            }\n"
      code << "            #{node_var}.put(#{bname_const_field_name}, #{var_json_array});\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    if required?
      if has_default?
        "        #@name = #{default_value_code};\n"
      else
        "        #@name = new #@primitive_value_type[]{};\n"
      end
    else
      "        #@name = null;\n"
    end
  end

  def generate_deep_copy_from_code(src_var)
    code = ''
    code << "        var #@name = #{src_var}.#@name;\n"
    code << "        if (#@name == null) {\n"
    code << "            this.#@name = null;\n"
    code << "        } else {\n"
    code << "            this.#@name = Arrays.copyOf(#@name, #@name.length);\n"
    code << "        }\n"
  end

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonArray(#@name)))")
  end
  
  def generate_to_string_code
    "Arrays.toString(#@name)"
  end

end

class IntArrayFieldConf < PrimitiveArrayFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'int')
  end

end

class LongArrayFieldConf < PrimitiveArrayFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'long')
  end

end

class DoubleArrayFieldConf < PrimitiveArrayFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'double')
  end

end

class StdListFieldConf < FieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'std-list')
  end

  def value_type
    case @value
    when 'int'
      'Integer'
    when 'long'
      'Long'
    when 'double'
      'Double'
    when 'string'
      'String'
    when 'uuid', 'uuid-legacy'
      'UUID'
    when 'date'
      'LocalDate'
    when 'datetime'
      'LocalDateTime'
    when 'object-id'
      'ObjectId'
    else
      raise "unsupported value type `#@value`"
    end
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
    when 'date'
      v_var = variable_name_global('v')
      "#{v_var} -> new BsonInt32(DateTimeUtil.toNumber(#{v_var}))"
    when 'datetime'
      'BsonUtil::toBsonDateTime'
    when 'object-id'
      'BsonObjectId::new'
    when 'uuid'
      'BsonUtil::toBsonBinary'
    when 'object'
      "#@model::toBson"
    else
      raise "unsupported value type `#@value` for std-list"
    end
  end

  def generate_reality_load_code(src_var)
    v_var = variable_name_global('v')
    case @value
    when 'int'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::intValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::intValue).orElse(null);\n"
      end
    when 'long'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::longValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::longValue).orElse(null);\n"
      end
    when 'double'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::doubleValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonNumber::doubleValue).orElse(null);\n"
      end
    when 'boolean'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonBoolean::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonBoolean::getValue).orElse(null);\n"
      end
    when 'string'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonString::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonString::getValue).orElse(null);\n"
      end
    when 'date'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonNumber #{v_var}) -> DateTimeUtil.toDate(#{v_var}.intValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonNumber #{v_var}) -> DateTimeUtil.toDate(#{v_var}.intValue())).orElse(null);\n"
      end
    when 'datetime'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonUtil::toLocalDateTime).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonUtil::toLocalDateTime).orElse(null);\n"
      end
    when 'object-id'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonObjectId::getValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, BsonObjectId::getValue).orElse(null);\n"
      end
    when 'uuid'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonBinary #{v_var}) -> #{v_var}.asUuid(UuidRepresentation.STANDARD)).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonBinary #{v_var}) -> #{v_var}.asUuid(UuidRepresentation.STANDARD)).orElse(null);\n"
      end
    when 'uuid-legacy'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonBinary #{v_var}) -> #{v_var}.asUuid(UuidRepresentation.UUID_LEGACY)).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonBinary #{v_var}) -> #{v_var}.asUuid(UuidRepresentation.UUID_LEGACY)).orElse(null);\n"
      end
    when 'object'
      if required?
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonDocument #{v_var}) -> new #@model().load(#{v_var})).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.arrayValue(#{src_var}, #{bname_const_field_name}, (BsonDocument #{v_var}) -> new #@model().load(#{v_var})).orElse(null);\n"
      end
    else
      raise "unsupported value type `#@value` for `std-list`"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    var_array_node = variable_name("ArrayNode")
    add_code = case @value
    when 'int', 'long', 'double', 'boolean', 'string'
      "#@name.forEach(#{var_array_node}::add);"
    when 'date'
      "#@name.stream().map(DateTimeUtil::toNumber).forEach(#{var_array_node}::add);"
    when 'datetime'
      "#@name.stream().map(DateTimeUtil::toEpochMilli).forEach(#{var_array_node}::add);"
    when 'object-id'
      "#@name.stream().map(ObjectId::toHexString).forEach(#{var_array_node}::add);"
    when 'uuid', 'uuid-legacy'
      "#@name.stream().map(UUID::toString).forEach(#{var_array_node}::add);"
    when 'object'
      "#@name.stream().map(#@model::toJsonNode).forEach(#{var_array_node}::add);"
    else
      raise "unsupported value type `#@value` for std-list"
    end
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{var_array_node} = jsonNode.arrayNode(#@name.size());\n"
      code << "        #{add_code}\n"
      code << "        #{json_node_var}.set(#{bname_const_field_name}, #{var_array_node});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{var_array_node} = jsonNode.arrayNode(#@name.size());\n"
      code << "            #{add_code}\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, #{var_array_node});\n"
      code << "        }\n"
    end
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    var_json_array = variable_name("JsonArray")
    add_code = case @value
    when 'int', 'long', 'double', 'boolean', 'string'
      "#{var_json_array}.addAll(#@name);"
    when 'date'
      "#@name.stream().map(DateTimeUtil::toNumber).forEach(#{var_json_array}::add);"
    when 'datetime'
      "#@name.stream().map(DateTimeUtil::toEpochMilli).forEach(#{var_json_array}::add);"
    when 'object-id'
      "#@name.stream().map(ObjectId::toHexString).forEach(#{var_json_array}::add);"
    when 'uuid', 'uuid-legacy'
      "#@name.stream().map(UUID::toString).forEach(#{var_json_array}::add);"
    when 'object'
      "#@name.stream().map(#@model::toFastjson2Node).forEach(#{var_json_array}::add);"
    else
      raise "unsupported value type `#@value` for std-list"
    end
    code = "        var #@name = this.#@name;\n"
    if required?
      code << "        var #{var_json_array} = new JSONArray(#@name.size());\n"
      code << "        #{add_code}\n"
      code << "        #{node_var}.put(#{bname_const_field_name}, #{var_json_array});\n"
    else
      code << "        if (#@name != null) {\n"
      code << "            var #{var_json_array} = new JSONArray(#@name.size());\n"
      code << "            #{add_code}\n"
      code << "            #{node_var}.put(#{bname_const_field_name}, #{var_json_array});\n"
      code << "        }\n"
    end
  end

  def data_value_convert_code
    case @value
    when 'object'
      ".stream().map(#@model::toData).toList()"
    when 'int', 'long', 'double', 'boolean', 'string'
      ''
    when 'date'
      '.stream().map(LocalDate::toString).toList()'
    when 'datetime'
      '.stream().map(LocalDateTime::toString).toList()'
    when 'object-id'
      '.stream().map(ObjectId::toHexString).toList()'
    when 'uuid', 'uuid-legacy'
      '.stream().map(UUID::toString).toList()'
    else
      raise "unsupport value type `@value` for std-list"
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}()#{data_value_convert_code});\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    if required?
      "        #@name = List.of();\n"
    else
      "        #@name = null;\n"
    end
  end

  def generate_deep_copy_from_code(src_var)
    code = ''
    case @value
    when 'int', 'long', 'double', 'boolean', 'string', 'date', 'datetime', 'object-id', 'uuid', 'uuid-legacy'
      code << "        var #@name = #{src_var}.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            this.#@name = new ArrayList<>(#{src_var}.#@name);\n"
      code << "        } else {\n"
      code << "            this.#@name = null;\n"
      code << "        }\n"
    when 'object'
      var_copy = variable_name('Copy')
      var_copy_value = variable_name('CopyValue')
      code << "        var #@name = #{src_var}.#@name;\n"
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
      code << "        } else {\n"
      code << "            this.#@name = null;\n"
      code << "        }\n"
    else
      raise "unsupport value type `@value` for std-list"
    end
  end

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#{updates_var}.add(Updates.set(path().resolve(#{bname_const_field_name}).value(), BsonUtil.toBsonArray(#@name, #{to_array_mapper_code})))")
  end

  def generate_reality_load_object_node_code(src_var)
    v_var = variable_name_global('v')
    case @value
    when 'int'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::intValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::intValue).orElse(null);\n"
      end
    when 'long'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::longValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::longValue).orElse(null);\n"
      end
    when 'double'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::doubleValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::doubleValue).orElse(null);\n"
      end
    when 'boolean'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::booleanValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::booleanValue).orElse(null);\n"
      end
    when 'string'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::textValue).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, JsonNode::textValue).orElse(null);\n"
      end
    when 'date'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.toDate(Math.max(#{v_var}.intValue(), 101))).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.toDate(Math.max(#{v_var}.intValue(), 101))).orElse(null);\n"
      end
    when 'datetime'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.ofEpochMilli(#{v_var}.longValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.ofEpochMilli(#{v_var}.longValue())).orElse(null);\n"
      end
    when 'object-id'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new ObjectId(#{v_var}.textValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new ObjectId(#{v_var}.textValue())).orElse(null);\n"
      end
    when 'uuid', 'uuid-legacy'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> UUID.fromString(#{v_var}.textValue())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> UUID.fromString(#{v_var}.textValue())).orElse(null);\n"
      end
    when 'object'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new #@model().load(#{v_var})).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new #@model().load(#{v_var})).orElse(null);\n"
      end
    else
      raise "unsupported value type `#@value` for `std-list`"
    end
  end

  def generate_reality_load_json_object_code(src_var)
    v_var = variable_name_global('v')
    case @value
    when 'int'
      n_var = variable_name_global('n')
      parse_int_code = "#{v_var} -> #{v_var} instanceof Number #{n_var} ? #{n_var}.intValue() : 0"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_int_code}).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_int_code}).orElse(null);\n"
      end
    when 'long'
      n_var = variable_name_global('n')
      parse_long_code = "#{v_var} -> #{v_var} instanceof Number #{n_var} ? #{n_var}.longValue() : 0L"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_long_code}).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_long_code}).orElse(null);\n"
      end
    when 'double'
      n_var = variable_name_global('n')
      parse_double_code = "#{v_var} -> #{v_var} instanceof Number #{n_var} ? #{n_var}.doubleValue() : 0.0"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_double_code}).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_double_code}).orElse(null);\n"
      end
    when 'boolean'
      b_var = variable_name_global('b')
      parse_boolean_code = "#{v_var} -> #{v_var} instanceof Boolean #{b_var} ? #{b_var} : Boolean.parseBoolean(#{v_var}.toString())"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_boolean_code}).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{parse_boolean_code}).orElse(null);\n"
      end
    when 'string'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, Object::toString).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, Object::toString).orElse(null);\n"
      end
    when 'date'
      n_var = variable_name_global('n')
      parse_int_code = "#{v_var} instanceof Number #{n_var} ? #{n_var}.intValue() : 101"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.toDate(#{parse_int_code})).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.toDate(#{parse_int_code})).orElse(null);\n"
      end
    when 'datetime'
      n_var = variable_name_global('n')
      parse_long_code = "#{v_var} instanceof Number #{n_var} ? #{n_var}.longValue() : 0L"
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.ofEpochMilli(#{parse_long_code})).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> DateTimeUtil.ofEpochMilli(#{parse_long_code})).orElse(null);\n"
      end
    when 'object-id'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new ObjectId(#{v_var}.toString())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new ObjectId(#{v_var}.toString())).orElse(null);\n"
      end
    when 'uuid', 'uuid-legacy'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> UUID.fromString(#{v_var}.toString())).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> UUID.fromString(#{v_var}.toString())).orElse(null);\n"
      end
    when 'object'
      if required?
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new #@model().loadFastjson2Node(#{v_var})).orElseGet(List::of);\n"
      else
        "        #@name = BsonUtil.listValue(#{src_var}, #{bname_const_field_name}, #{v_var} -> new #@model().loadFastjson2Node(#{v_var})).orElse(null);\n"
      end
    else
      raise "unsupported value type `#@value` for `std-list`"
    end
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #{getter_name}()#{data_value_convert_code});\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #@name#{data_value_convert_code});\n"
      code << "            }\n"
    end
  end

end

class ModelFieldConf < FieldConf

  def initialize(name, bname, dname, type)
    super(name, bname, dname, type)
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

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, "#@name.toBson()")
  end

  def generate_load_model_code(src_var, factor)
    if required?
      "        BsonUtil.documentValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(#@name::load, #@name::clean);\n"
    else
      v_var = variable_name_global('v')
      code = "        BsonUtil.documentValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(\n"
      code << "                #{v_var} -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                    }\n"
      code << "                    this.#@name = #{factor}.load(#{v_var}).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
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

  def generate_reality_append_to_fastjson2_node_code(node_var)
    if required?
      "        #{node_var}.put(#{bname_const_field_name}, #@name.toFastjson2Node());\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{node_var}.put(#{bname_const_field_name}, #@name.toFastjson2Node());\n"
      code << "        }\n"
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #{getter_name}().toData());\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toData());\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", #@name.toData());\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", #@name.toData());\n"
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

  def generate_deep_copy_from_code(src_var)
    if required?
      "        #{src_var}.#@name.deepCopyTo(#@name, false);\n"
    else
      code = "        var #@name = #{src_var}.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            this.#@name = #@name.deepCopy().parent(this).key(#{bname_const_field_name}).index(#@index);\n"
      code << "        } else {\n"
      code << "            #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #@name.unbind();\n"
      code << "                this.#@name = null;\n"
      code << "            }\n"
      code << "        }\n"
    end
  end

  def generate_append_updates_code(updates_var)
    generate_reality_append_updates_code(updates_var, "#@name.appendUpdates(#{updates_var})")
  end

  def generate_load_model_object_node_code(src_var, factor)
    if required?
      "        BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(#@name::load, #@name::clean);\n"
    else
      v_var = variable_name_global('v')
      code = "        BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(\n"
      code << "                #{v_var} -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                    }\n"
      code << "                    this.#@name = #{factor}.load(#{v_var}).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
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

  def generate_load_model_json_object_code(src_var, factor)
    if required?
      "        BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(#@name::loadFastjson2Node, #@name::clean);\n"
    else
      v_var = variable_name_global('v')
      code = "        BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).ifPresentOrElse(\n"
      code << "                #{v_var} -> {\n"
      code << "                    var #@name = this.#@name;\n"
      code << "                    if (#@name != null) {\n"
      code << "                        #@name.unbind();\n"
      code << "                    }\n"
      code << "                    this.#@name = #{factor}.loadFastjson2Node(#{v_var}).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
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

  def generate_virtual_append_value_to_update_data_code(data_var)
    code = ''
    var_update_data = variable_name("UpdateData")
    if required?
      code << "            var #{var_update_data} = #{getter_name}().toUpdateData();\n"
      code << "            if (#{var_update_data} != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #{var_update_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                var #{var_update_data} = #@name.toUpdateData();\n"
      code << "                if (#{var_update_data} != null) {\n"
      code << "                    #{data_var}.put(\"#@dname\", #{var_update_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    code = ''
    var_update_data = variable_name("UpdateData")
    if required?
      code << "            var #{var_update_data} = #@name.toUpdateData();\n"
      code << "            if (#{var_update_data} != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #{var_update_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                var #{var_update_data} = #@name.toUpdateData();\n"
      code << "                if (#{var_update_data} != null) {\n"
      code << "                    #{data_var}.put(\"#@dname\", #{var_update_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
  end

  def generate_virtual_append_deleted_data_code(data_var)
    var_deleted_data = variable_name('DeletedData')
    code = ''
    code << "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            var #{var_deleted_data} = #{getter_name}().toDeletedData();\n"
      code << "            if (#{var_deleted_data} != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #{var_deleted_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name == null) {\n"
      code << "                #{data_var}.put(\"#@dname\", 1);\n"
      code << "            } else {\n"
      code << "                var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "                if (#{var_deleted_data} != null) {\n"
      code << "                    #{data_var}.put(\"#@dname\", #{var_deleted_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

  def generate_reality_append_deleted_data_code(data_var)
    var_deleted_data = variable_name('DeletedData')
    code = ''
    code << "        if (changedFields.get(#@index)) {\n"
    if required?
      code << "            var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "            if (#{var_deleted_data} != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", #{var_deleted_data});\n"
      code << "            }\n"
    else
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name == null) {\n"
      code << "                #{data_var}.put(\"#@dname\", 1);\n"
      code << "            } else {\n"
      code << "                var #{var_deleted_data} = #@name.toDeletedData();\n"
      code << "                if (#{var_deleted_data} != null) {\n"
      code << "                    #{data_var}.put(\"#@dname\", #{var_deleted_data});\n"
      code << "                }\n"
      code << "            }\n"
    end
    code << "        }\n"
  end

end

class ObjectFieldConf < ModelFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'object')
  end

  def generic_type
    @model
  end

  def generate_transient_declare_code
    if required?
      "    private final #{generic_type} #@name = new #{generic_type}();\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_declare_code
    if required?
      "    private final #{generic_type} #@name = new #{generic_type}().parent(this).key(#{bname_const_field_name}).index(#@index);\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_load_code(src_var)
    generate_load_model_code(src_var, "new #{generic_type}()")
  end

  def generate_reality_load_object_node_code(src_var)
    generate_load_model_object_node_code(src_var, "new #{generic_type}()")
  end

  def generate_reality_load_json_object_code(src_var)
    generate_load_model_json_object_code(src_var, "new #{generic_type}()")
  end

end

class MapFieldConf < ModelFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'map')
  end

  def generic_type
    if @value == 'object'
      "DefaultMapModel<#{key_type}, #@model>"
    else
      "SingleValueMapModel<#{key_type}, #{value_type}>"
    end
  end

  def generate_transient_declare_code
    if required?
      "    private final #{generic_type} #@name = #{map_init_code};\n"
    else
      "    private #{generic_type} #@name;\n"
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

  def generate_reality_load_code(src_var)
    generate_load_model_code(src_var, map_init_code)
  end

  def generate_reality_load_object_node_code(src_var)
    generate_load_model_object_node_code(src_var, map_init_code)
  end

  def generate_reality_load_json_object_code(src_var)
    generate_load_model_json_object_code(src_var, map_init_code)
  end

end

class ListFieldConf < ModelFieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'list')
  end

  def generic_type
    if @value == 'object'
      "DefaultListModel<#@model>"
    else
      raise "unsupported value type `#@value` for list"
    end
  end

  def generate_transient_declare_code
    raise "list field can't be neither transient nor loadonly"
  end

  def generate_reality_declare_code
    if required?
      "    private final #{generic_type} #@name = new #{generic_type}(#@model::new).parent(this).key(#{bname_const_field_name}).index(#@index);\n"
    else
      "    private #{generic_type} #@name;\n"
    end
  end

  def generate_reality_load_code(src_var)
    generate_load_model_code(src_var, "new #{generic_type}(#@model::new)")
  end

  def generate_reality_load_object_node_code(src_var)
    generate_load_model_object_node_code(src_var, "new #{generic_type}(#@model::new)")
  end

  def generate_reality_load_json_object_code(src_var)
    generate_load_model_json_object_code(src_var, "new #{generic_type}(#@model::new)")
  end

end

class BsonDocumentFieldConf < FieldConf

  def initialize(name, bname, dname)
    super(name, bname, dname, 'bson-document')
  end

  def generic_type
    'BsonDocument'
  end

  def generate_reality_declare_code
    "    private #{generic_type} #@name;\n"
  end

  def generate_append_to_bson_code(bsovar_n)
    generate_append_value_to_bson_code(bsovar_n, @name)
  end

  def generate_reality_load_code(src_var)
    if required?
      "        #@name = BsonUtil.documentValue(#{src_var}, #{bname_const_field_name}).orElseThrow();\n"
    else
      "        #@name = BsonUtil.documentValue(#{src_var}, #{bname_const_field_name}).orElse(null);\n"
    end
  end

  def generate_reality_load_object_node_code(src_var)
    if required?
      "        #@name = BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).map(BsonUtil::toBsonDocument).orElseThrow();\n"
    else
      "        #@name = BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).map(BsonUtil::toBsonDocument).orElse(null);\n"
    end
  end

  def generate_reality_load_json_object_code(src_var)
    if required?
      "        #@name = BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).map(BsonUtil::toBsonDocument).orElseThrow();\n"
    else
      "        #@name = BsonUtil.objectValue(#{src_var}, #{bname_const_field_name}).map(BsonUtil::toBsonDocument).orElse(null);\n"
    end
  end

  def generate_reality_append_to_json_node_code(json_node_var)
    if required?
      "        #{json_node_var}.set(#{bname_const_field_name}, BsonUtil.toObjectNode(#@name));\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{json_node_var}.set(#{bname_const_field_name}, BsonUtil.toObjectNode(#@name));\n"
      code << "        }\n"
    end
  end

  def generate_reality_append_to_fastjson2_node_code(node_var)
    if required?
      "        #{node_var}.put(#{bname_const_field_name}, BsonUtil.toJSONObject(#@name));\n"
    else
      code = "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{node_var}.put(#{bname_const_field_name}, BsonUtil.toJSONObject(#@name));\n"
      code << "        }\n"
    end
  end

  def generate_virtual_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", BsonUtil.toMap(#{getter_name}()));\n"
    else
      code = ''
      code << "        var #@name = #{getter_name}();\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
      code << "        }\n"
    end
  end

  def generate_visible_put_to_data_code(data_var)
    if required?
      "        #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
    else
      code = ''
      code << "        var #@name = this.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
      code << "        }\n"
    end
  end

  def generate_clean_code
    "        #@name = null;\n"
  end

  def generate_deep_copy_from_code(src_var)
    if required?
      "        #@name = #{src_var}.#@name.clone();\n"
    else
      code = "        var #@name = #{src_var}.#@name;\n"
      code << "        if (#@name != null) {\n"
      code << "            this.#@name = #@name.clone();\n"
      code << "        } else {\n"
      code << "            this.#@name = null;\n"
      code << "        }\n"
    end
  end

  def generate_virtual_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", BsonUtil.toMap(#{getter_name}()));\n"
    else
      code = ''
      code << "            var #@name = #{getter_name}();\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
      code << "            }\n"
    end
  end

  def generate_reality_append_value_to_update_data_code(data_var)
    if required?
      "            #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
    else
      code = ''
      code << "            var #@name = this.#@name;\n"
      code << "            if (#@name != null) {\n"
      code << "                #{data_var}.put(\"#@dname\", BsonUtil.toMap(#@name));\n"
      code << "            }\n"
    end
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
