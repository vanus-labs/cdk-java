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
package com.linkall.core;

/**
 * Adapter describes how original data will be transformed into a CloudEvent
 * <p>
 * Check {@link com.linkall.core.Adapter1} and {@link com.linkall.core.Adapter2}
 * to transform some specific data into CloudEvents.
 * <p>
 * Users should implement specific adapters based on the number of Data Type they
 * need to generate a CloudEvent.
 * <p>
 * <ul>
 *      <li>they should implement {@link com.linkall.core.Adapter1}
 *      if they only extract data from a json string to generate a CloudEvent
 *      </li>
 *      <li>they should implement {@link com.linkall.core.Adapter2}
 *      if they need data both from http headers and body to generate a CloudEvent
 *      </li>
 * </ul>
 */
public interface Adapter{}
