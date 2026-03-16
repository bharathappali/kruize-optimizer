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
package com.kruize.optimizer.model.kruize;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

/**
 * Model representing a Kruize profile (metadata, metric, or layer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KruizeProfile {

    @JsonProperty("name")
    @JsonAlias({"metadata"})
    @JsonDeserialize(using = NameDeserializer.class)
    private String name;

    @JsonProperty("profile_version")
    @JsonDeserialize(using = ProfileVersionDeserializer.class)
    private String profileVersion;

    @JsonProperty("profile_type")
    private String profileType;

    public KruizeProfile() {
    }

    public KruizeProfile(String name, String profileVersion) {
        this.name = name;
        this.profileVersion = profileVersion;
    }

    public KruizeProfile(String name, String profileVersion, String profileType) {
        this.name = name;
        this.profileVersion = profileVersion;
        this.profileType = profileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(String profileVersion) {
        this.profileVersion = profileVersion;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    @Override
    public String toString() {
        return "KruizeProfile{" +
                "name='" + name + '\'' +
                ", profileVersion='" + profileVersion + '\'' +
                ", profileType='" + profileType + '\'' +
                '}';
    }

    /**
     * Custom deserializer to extract name from metadata object or direct name field
     */
    public static class NameDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            
            // If it's a string, return it directly
            if (node.isTextual()) {
                return node.asText();
            }
            
            // If it's an object (metadata), extract the name field
            if (node.isObject() && node.has("name")) {
                return node.get("name").asText();
            }
            
            return null;
        }
    }

    /**
     * Custom deserializer to handle both numeric and string profile versions
     * Simply converts any value to string representation
     */
    public static class ProfileVersionDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // Just convert whatever value to string
            return p.getValueAsString();
        }
    }
}

