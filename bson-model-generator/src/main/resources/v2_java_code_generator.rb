#!/usr/bin/env ruby

require "set"
require 'yaml'
require 'json'


class ModelConf

  attr_reader :name,
              :type,
              :fields

  def initialize(name, type)
    @name = name
    @type = type
    @fields = []
  end

  def append_field(field)
    @fields << field.index(@fields.size)
  end

  def all_fields_single_value_required?
    @fields.e
  end

end

class FieldConf

  attr_reader :name,
              :bname,
              :type

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
    @increment_1 = false
    @increment_n = false
    @sources = []
  end

  def required(required = true)
    @required = required
    self
  end

  def required?
    @required
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

  def increment_1(increment_1 = true)
    @increment_1 = increment_1
    self
  end

  def increment_1?
    @increment_1
  end

  def increment_1(increment_n = true)
    @increment_n = increment_n
    self
  end

  def increment_n?
    @increment_n
  end

  def index(index = nil)
    if index.nil?
      @index
    else
      @index = index
      self
    end
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
      'String'
    when 'date'
      'LocalDate'
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
        raise "unsupported value type `#@value`"
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

  def const_field_name
    @name.gsub(/A-Z/) do |match| "_#{match}" end.upcase
  end

  def getter_name
    "get#{camcel_name}"
  end

  def setter_name
    "set#{camcel_name}"
  end
    
  private
  def camcel_name
    @name[0].upcase << @name[1..-1]
  end

end


