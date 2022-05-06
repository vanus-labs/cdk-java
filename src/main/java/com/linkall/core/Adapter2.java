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

import io.cloudevents.CloudEvent;

/**
 * Adapter describes how original data will be transformed into a CloudEvent
 * @param <D1> D1 stands for Data Type 1
 * @param <D2> D2 stands for Data Type 2
 */
public interface Adapter2<D1,D2> extends Adapter{
    CloudEvent adapt(D1 data, D2 data2);
}