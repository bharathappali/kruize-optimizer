/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.kruize.optimizer.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class to load mock JSON responses from test resources
 */
public class MockResponseLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MOCK_RESPONSES_PATH = "/mock-responses/";

    /**
     * Load a JSON file from mock-responses directory and parse it to the specified class
     *
     * @param filename The name of the JSON file (without path)
     * @param clazz    The class to parse the JSON into
     * @param <T>      The type of the class
     * @return The parsed object
     * @throws IOException if file cannot be read or parsed
     */
    public static <T> T loadMockResponse(String filename, Class<T> clazz) throws IOException {
        String json = loadMockResponseAsString(filename);
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Load a JSON file from mock-responses directory and parse it using TypeReference
     *
     * @param filename      The name of the JSON file (without path)
     * @param typeReference The TypeReference for complex types like List<T>
     * @param <T>           The type of the class
     * @return The parsed object
     * @throws IOException if file cannot be read or parsed
     */
    public static <T> T loadMockResponse(String filename, TypeReference<T> typeReference) throws IOException {
        String json = loadMockResponseAsString(filename);
        return objectMapper.readValue(json, typeReference);
    }

    /**
     * Load a JSON file from mock-responses directory as a String
     *
     * @param filename The name of the JSON file (without path)
     * @return The JSON content as a String
     * @throws IOException if file cannot be read
     */
    public static String loadMockResponseAsString(String filename) throws IOException {
        String path = MOCK_RESPONSES_PATH + filename;
        try (InputStream is = MockResponseLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Mock response file not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Get the ObjectMapper instance used for JSON parsing
     *
     * @return ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

