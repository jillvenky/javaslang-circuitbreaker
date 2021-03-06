/*
 *
 *  Copyright 2016 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.circuitbreaker;


import io.github.robwin.circuitbreaker.internal.InMemoryCircuitBreakerRegistry;
import javaslang.collection.Seq;

import java.util.function.Supplier;

/**
 * The {@link CircuitBreakerRegistry} is a factory to create CircuitBreaker instances which stores all CircuitBreaker instances in a registry.
 */
public interface CircuitBreakerRegistry {

    /**
     * Returns all managed {@link CircuitBreaker} instances.
     *
     * @return all managed {@link CircuitBreaker} instances.
     */
    Seq<CircuitBreaker> getAllCircuitBreakers();

    /**
     * Returns a managed {@link CircuitBreaker} or creates a new one with the default CircuitBreaker configuration.
     *
     * @param name the name of the CircuitBreaker
     * @return The {@link CircuitBreaker}
     */
    CircuitBreaker circuitBreaker(String name);

    /**
     * Returns a managed {@link CircuitBreaker} or creates a new one with a custom CircuitBreaker configuration.
     *
     * @param name      the name of the CircuitBreaker
     * @param circuitBreakerConfig  a custom CircuitBreaker configuration
     * @return The {@link CircuitBreaker}
     */
    CircuitBreaker circuitBreaker(String name, CircuitBreakerConfig circuitBreakerConfig);

    /**
     * Returns a managed {@link CircuitBreaker} or creates a new one with a custom CircuitBreaker configuration.
     *
     * @param name      the name of the CircuitBreaker
     * @param circuitBreakerConfigSupplier a supplier of a custom CircuitBreaker configuration
     * @return The {@link CircuitBreaker}
     */
    CircuitBreaker circuitBreaker(String name, Supplier<CircuitBreakerConfig> circuitBreakerConfigSupplier);

    /**
     * Creates a CircuitBreakerRegistry with a custom CircuitBreaker configuration.
     *
     * @param circuitBreakerConfig a custom CircuitBreaker configuration
     * @return a CircuitBreakerRegistry with a custom CircuitBreaker configuration.
     */
    static CircuitBreakerRegistry of(CircuitBreakerConfig circuitBreakerConfig){
        return new InMemoryCircuitBreakerRegistry(circuitBreakerConfig);
    }

    /**
     * Creates a CircuitBreakerRegistry with a default CircuitBreaker configuration.
     *
     * @return a CircuitBreakerRegistry with a default CircuitBreaker configuration.
     */
    static CircuitBreakerRegistry ofDefaults(){
        return new InMemoryCircuitBreakerRegistry();
    }
}
