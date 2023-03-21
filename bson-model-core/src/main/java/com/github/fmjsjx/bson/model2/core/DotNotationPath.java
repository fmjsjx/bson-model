package com.github.fmjsjx.bson.model2.core;

import com.github.fmjsjx.libcommon.util.StringUtil;

import java.util.Objects;

/**
 * BSON dot notation path.
 *
 * @author MJ Fang
 * @since 2.0
 */
public final class DotNotationPath {


    /**
     * Constructs a new {@link DotNotationPath} with {@code null} value.
     *
     * @return a {@code DotNotationPath}
     */
    public static final DotNotationPath root() {
        return RootInstanceHolder.instance;
    }

    private static final class RootInstanceHolder {
        private static final DotNotationPath instance = new DotNotationPath(null);
    }

    /**
     * Constructs a new {@link DotNotationPath} by the given parameters.
     *
     * @param keys the array of the keys
     * @return a {@code DotNotation}
     */
    public static final DotNotationPath of(Object... keys) {
        if (keys.length == 0) {
            return root();
        }
        var b = new StringBuilder();
        for (var key : keys) {
            b.append('.');
            if (key instanceof Number) {
                b.append(((Number) key).intValue());
            } else {
                b.append(key.toString());
            }
        }
        return new DotNotationPath(b.toString());
    }


    private final String value;

    DotNotationPath(String value) {
        this.value = StringUtil.isEmpty(value) ? "" : value;
    }


    /**
     * Returns the string value of this dot notation path.
     *
     * @return the string value of this dot notation path
     */
    public String value() {
        return value;
    }

    /**
     * Returns if this dot notation path is root path or not.
     *
     * @return {@code true} if this dot notation path is root path, {@code false} otherwise
     */
    public boolean isRoot() {
        return value.isEmpty();
    }

    /**
     * Resolves the given key against this dot notation path.
     *
     * @param key the key to be resolved
     * @return a new {@code DotNotationPath}
     */
    public DotNotationPath resolve(Object key) {
        Objects.requireNonNull(key, "key must not be null");
        return new DotNotationPath(isRoot() ? key.toString() : value + "." + key);
    }

    /**
     * Resolves the given name against this dot notation path.
     *
     * @param name the field name to be resolved
     * @return a new {@code DotNotationPath}
     */
    public DotNotationPath resolve(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return new DotNotationPath(isRoot() ? name : value + "." + name);
    }

    /**
     * Resolves the given name against this dot notation path.
     *
     * @param index the index to be resolved
     * @return a new {@code DotNotationPath}
     */
    public DotNotationPath resolve(int index) {
        return new DotNotationPath(isRoot() ? Integer.toString(index) : value + "." + index);
    }

    @Override
    public String toString() {
        return value();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DotNotationPath path) {
            return this == path || value().equals(path.value());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value().hashCode();
    }

}
