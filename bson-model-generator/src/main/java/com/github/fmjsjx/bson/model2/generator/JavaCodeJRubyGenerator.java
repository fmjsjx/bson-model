package com.github.fmjsjx.bson.model2.generator;

import com.github.fmjsjx.bson.model.generator.JRubyCodeGenerator;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The main class of v2 java code generator using JRuby.
 *
 * @author MJ Fang
 * @since 2.0
 */
public class JavaCodeJRubyGenerator {

    /**
     * Main method.
     *
     * @param args main args
     * @throws IOException if any I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        var container = new ScriptingContainer();
        container.setArgv(args);
        try (var in = JRubyCodeGenerator.class.getResourceAsStream("/v2_java_code_generator.rb")) {
            assert in != null;
            var script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            container.runScriptlet(script);
        }
    }

}
