package com.hostelhub.prototype;

/**
 * Prototype Pattern: Prototype interface
 * Declares the cloning contract for producing object copies with independent mutable state.
 *
 * @param <T> The type of object being cloned.
 */
public interface Prototype<T> {
    /**
     * Creates and returns a copy of this object with independent mutable state.
     *
     * @return A fresh, independent clone of type T.
     */
    T clone();
}
