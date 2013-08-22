package com.elster.jupiter.util;

/**
 * Helper interface for dependency injection. Instead of injecting an instance, you can inject a Provider for a type.
 * This way a provider can return the same instance each time or create a new one per request.
 */
public interface Provider<T> {

    T get();
}
