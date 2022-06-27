/*
 * Copyright 2022-Present The Vance Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.linkall.vance.core.http;

public class HttpResponseInfo {
    private int successCode;
    private String successChunk;
    private int failureCode;
    private String failureChunk;

    public HttpResponseInfo(int successCode, String successChunk, int failureCode, String failureChunk) {
        this.successCode = successCode;
        this.successChunk = successChunk;
        this.failureCode = failureCode;
        this.failureChunk = failureChunk;
    }

    public int getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(int successCode) {
        this.successCode = successCode;
    }

    public String getSuccessChunk() {
        return successChunk;
    }

    public void setSuccessChunk(String successChunk) {
        this.successChunk = successChunk;
    }

    public int getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(int failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureChunk() {
        return failureChunk;
    }

    public void setFailureChunk(String failureChunk) {
        this.failureChunk = failureChunk;
    }
}
