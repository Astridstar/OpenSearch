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
 *     http://www.apache.org/licenses/LICENSE-2.0
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

package org.opensearch.cluster.routing.allocation;

import org.opensearch.Version;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.cluster.routing.allocation.NodeAllocationResult;
import org.opensearch.cluster.routing.allocation.NodeAllocationResult.ShardStoreInfo;
import org.opensearch.cluster.routing.allocation.decider.Decision;
import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.test.OpenSearchTestCase;

import java.io.IOException;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Unit tests for the {@link NodeAllocationResult} class.
 */
public class NodeAllocationResultTests extends OpenSearchTestCase {

    public void testSerialization() throws IOException {
        DiscoveryNode node = new DiscoveryNode("node1", buildNewFakeTransportAddress(), emptyMap(), emptySet(), Version.CURRENT);
        Decision decision = randomFrom(Decision.YES, Decision.THROTTLE, Decision.NO);
        NodeAllocationResult explanation = new NodeAllocationResult(node, decision, 1);
        BytesStreamOutput output = new BytesStreamOutput();
        explanation.writeTo(output);
        NodeAllocationResult readExplanation = new NodeAllocationResult(output.bytes().streamInput());
        assertNodeExplanationEquals(explanation, readExplanation);
    }

    public void testShardStore() throws IOException {
        DiscoveryNode node = new DiscoveryNode("node1", buildNewFakeTransportAddress(), emptyMap(), emptySet(), Version.CURRENT);
        Decision decision = randomFrom(Decision.YES, Decision.THROTTLE, Decision.NO);
        long matchingBytes = (long) randomIntBetween(1, 1000);
        ShardStoreInfo shardStoreInfo = new ShardStoreInfo(matchingBytes);
        NodeAllocationResult explanation = new NodeAllocationResult(node, shardStoreInfo, decision);
        BytesStreamOutput output = new BytesStreamOutput();
        explanation.writeTo(output);
        NodeAllocationResult readExplanation = new NodeAllocationResult(output.bytes().streamInput());
        assertNodeExplanationEquals(explanation, readExplanation);
        assertEquals(matchingBytes, explanation.getShardStoreInfo().getMatchingBytes());
        assertNull(explanation.getShardStoreInfo().getAllocationId());
        assertFalse(explanation.getShardStoreInfo().isInSync());
        assertFalse(explanation.getShardStoreInfo().hasMatchingSyncId());

        String allocId = randomAlphaOfLength(5);
        boolean inSync = randomBoolean();
        shardStoreInfo = new ShardStoreInfo(allocId, inSync, randomBoolean() ? new Exception("bad stuff") : null);
        explanation = new NodeAllocationResult(node, shardStoreInfo, decision);
        output = new BytesStreamOutput();
        explanation.writeTo(output);
        readExplanation = new NodeAllocationResult(output.bytes().streamInput());
        assertNodeExplanationEquals(explanation, readExplanation);
        assertEquals(inSync, explanation.getShardStoreInfo().isInSync());
        assertEquals(-1, explanation.getShardStoreInfo().getMatchingBytes());
        assertFalse(explanation.getShardStoreInfo().hasMatchingSyncId());
        assertEquals(allocId, explanation.getShardStoreInfo().getAllocationId());
    }

    private void assertNodeExplanationEquals(NodeAllocationResult expl1, NodeAllocationResult expl2) {
        assertEquals(expl1.getNode(), expl2.getNode());
        assertEquals(expl1.getCanAllocateDecision(), expl2.getCanAllocateDecision());
        assertEquals(0, Float.compare(expl1.getWeightRanking(), expl2.getWeightRanking()));
        if (expl1.getShardStoreInfo() != null) {
            assertEquals(expl1.getShardStoreInfo().isInSync(), expl2.getShardStoreInfo().isInSync());
            assertEquals(expl1.getShardStoreInfo().getAllocationId(), expl2.getShardStoreInfo().getAllocationId());
            assertEquals(expl1.getShardStoreInfo().getMatchingBytes(), expl2.getShardStoreInfo().getMatchingBytes());
            assertEquals(expl1.getShardStoreInfo().hasMatchingSyncId(), expl2.getShardStoreInfo().hasMatchingSyncId());
        } else {
            assertNull(expl2.getShardStoreInfo());
        }
    }
}
