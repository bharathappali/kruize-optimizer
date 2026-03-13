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

/**
 * Model representing a Kruize profile (metadata, metric, or layer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KruizeProfile {

    private String name;
    private Metadata metadata;

    @JsonProperty("profile_version")
    @JsonAlias({"version"})
    private String profileVersion;

    public KruizeProfile() {
    }

    public KruizeProfile(String name, String profileVersion) {
        this.name = name;
        this.profileVersion = profileVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(String profileVersion) {
        this.profileVersion = profileVersion;
    }

    @Override
    public String toString() {
        return "KruizeProfile{" +
                "name='" + name + '\'' +
                ", metadata=" + metadata +
                ", profileVersion='" + profileVersion + '\'' +
                '}';
    }

    /**
     * Nested metadata class for profiles
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String name;

        public Metadata() {
        }

        public Metadata(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}

