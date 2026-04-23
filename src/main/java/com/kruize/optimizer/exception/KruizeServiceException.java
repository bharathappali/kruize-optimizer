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
package com.kruize.optimizer.exception;

/**
 * Custom exception for Kruize service errors
 */
public class KruizeServiceException extends RuntimeException {

    private final int statusCode;

    public KruizeServiceException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public KruizeServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public KruizeServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public KruizeServiceException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

