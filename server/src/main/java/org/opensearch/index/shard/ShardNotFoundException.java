/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index.shard;

import org.opensearch.ResourceNotFoundException;
import org.opensearch.common.io.stream.StreamInput;

import java.io.IOException;

public class ShardNotFoundException extends ResourceNotFoundException {
    public ShardNotFoundException(ShardId shardId) {
        this(shardId, null);
    }

    public ShardNotFoundException(ShardId shardId, Throwable ex) {
        this(shardId, "no such shard", ex);
    }

    public ShardNotFoundException(ShardId shardId, String msg, Object... args) {
        this(shardId, msg, null, args);
    }

    public ShardNotFoundException(ShardId shardId, String msg, Throwable ex, Object... args) {
        super(msg, ex, args);
        setShard(shardId);
    }

    public ShardNotFoundException(StreamInput in) throws IOException{
        super(in);
    }
}
