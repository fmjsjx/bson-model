package com.github.fmjsjx.bson.model.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jruby.embed.ScriptingContainer;

/**
 * The main class of code generator using JRuby.
 */
public class JRubyCodeGenerator {

    public static void main(String[] args) throws IOException {
        var container = new ScriptingContainer();
        container.setArgv(args);
        try (var in = JRubyCodeGenerator.class.getResourceAsStream("/code_generator.rb")) {
            var script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            container.runScriptlet(script);
        }
    }

}
