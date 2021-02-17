/*
 * detector
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detector.result;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.detectable.detectable.explanation.Explanation;

public class PassedDetectorResult extends DetectorResult {
    public PassedDetectorResult() {
        this("Passed.", null, Collections.emptyList(), Collections.emptyList());
    }

    public PassedDetectorResult(@NotNull final String description) {
        this(description, null, Collections.emptyList(), Collections.emptyList());
    }

    public PassedDetectorResult(@NotNull final String description, final Class resultClass, List<Explanation> explanations, List<File> relevantFiles) {
        super(true, description, resultClass, explanations, relevantFiles);
    }
}
