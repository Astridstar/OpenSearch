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

package org.opensearch.search.aggregations.bucket;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.automaton.RegExp;
import org.opensearch.search.aggregations.Aggregator.SubAggCollectionMode;
import org.opensearch.search.aggregations.BaseAggregationTestCase;
import org.opensearch.search.aggregations.BucketOrder;
import org.opensearch.search.aggregations.bucket.terms.IncludeExclude;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregatorFactory.ExecutionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class TermsTests extends BaseAggregationTestCase<TermsAggregationBuilder> {

    private static final String[] executionHints;

    static {
        ExecutionMode[] executionModes = ExecutionMode.values();
        executionHints = new String[executionModes.length];
        for (int i = 0; i < executionModes.length; i++) {
            executionHints[i] = executionModes[i].toString();
        }
    }

    @Override
    protected TermsAggregationBuilder createTestAggregatorBuilder() {
        String name = randomAlphaOfLengthBetween(3, 20);
        TermsAggregationBuilder factory = new TermsAggregationBuilder(name);
        String field = randomAlphaOfLengthBetween(3, 20);
        randomFieldOrScript(factory, field);
        if (randomBoolean()) {
            factory.missing("MISSING");
        }
        if (randomBoolean()) {
            factory.size(randomIntBetween(1, Integer.MAX_VALUE));
        }
        if (randomBoolean()) {
            factory.shardSize(randomIntBetween(1, Integer.MAX_VALUE));
        }
        if (randomBoolean()) {
            int minDocCount = randomInt(4);
            switch (minDocCount) {
            case 0:
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                minDocCount = randomIntBetween(0, Integer.MAX_VALUE);
                break;
            default:
                fail();
            }
            factory.minDocCount(minDocCount);
        }
        if (randomBoolean()) {
            int shardMinDocCount = randomInt(4);
            switch (shardMinDocCount) {
            case 0:
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                shardMinDocCount = randomIntBetween(0, Integer.MAX_VALUE);
                break;
            default:
                fail();
            }
            factory.shardMinDocCount(shardMinDocCount);
        }
        if (randomBoolean()) {
            factory.collectMode(randomFrom(SubAggCollectionMode.values()));
        }
        if (randomBoolean()) {
            factory.executionHint(randomFrom(executionHints));
        }
        if (randomBoolean()) {
            factory.format("###.##");
        }
        if (randomBoolean()) {
            IncludeExclude incExc = null;
            switch (randomInt(6)) {
            case 0:
                incExc = new IncludeExclude(new RegExp("foobar"), null);
                break;
            case 1:
                incExc = new IncludeExclude(null, new RegExp("foobaz"));
                break;
            case 2:
                incExc = new IncludeExclude(new RegExp("foobar"), new RegExp("foobaz"));
                break;
            case 3:
                SortedSet<BytesRef> includeValues = new TreeSet<>();
                int numIncs = randomIntBetween(1, 20);
                for (int i = 0; i < numIncs; i++) {
                    includeValues.add(new BytesRef(randomAlphaOfLengthBetween(1, 30)));
                }
                SortedSet<BytesRef> excludeValues = null;
                incExc = new IncludeExclude(includeValues, excludeValues);
                break;
            case 4:
                SortedSet<BytesRef> includeValues2 = null;
                SortedSet<BytesRef> excludeValues2 = new TreeSet<>();
                int numExcs2 = randomIntBetween(1, 20);
                for (int i = 0; i < numExcs2; i++) {
                    excludeValues2.add(new BytesRef(randomAlphaOfLengthBetween(1, 30)));
                }
                incExc = new IncludeExclude(includeValues2, excludeValues2);
                break;
            case 5:
                SortedSet<BytesRef> includeValues3 = new TreeSet<>();
                int numIncs3 = randomIntBetween(1, 20);
                for (int i = 0; i < numIncs3; i++) {
                    includeValues3.add(new BytesRef(randomAlphaOfLengthBetween(1, 30)));
                }
                SortedSet<BytesRef> excludeValues3 = new TreeSet<>();
                int numExcs3 = randomIntBetween(1, 20);
                for (int i = 0; i < numExcs3; i++) {
                    excludeValues3.add(new BytesRef(randomAlphaOfLengthBetween(1, 30)));
                }
                incExc = new IncludeExclude(includeValues3, excludeValues3);
                break;
            case 6:
                final int numPartitions = randomIntBetween(1, 100);
                final int partition = randomIntBetween(0, numPartitions - 1);
                incExc = new IncludeExclude(partition, numPartitions);
                break;
            default:
                fail();
            }
            factory.includeExclude(incExc);
        }
        if (randomBoolean()) {
            List<BucketOrder> order = randomOrder();
            if(order.size() == 1 && randomBoolean()) {
                factory.order(order.get(0));
            } else {
                factory.order(order);
            }
        }
        if (randomBoolean()) {
            factory.showTermDocCountError(randomBoolean());
        }
        return factory;
    }

    private List<BucketOrder> randomOrder() {
        List<BucketOrder> orders = new ArrayList<>();
        switch (randomInt(4)) {
        case 0:
            orders.add(BucketOrder.key(randomBoolean()));
            break;
        case 1:
            orders.add(BucketOrder.count(randomBoolean()));
            break;
        case 2:
            orders.add(BucketOrder.aggregation(randomAlphaOfLengthBetween(3, 20), randomBoolean()));
            break;
        case 3:
            orders.add(BucketOrder.aggregation(randomAlphaOfLengthBetween(3, 20), randomAlphaOfLengthBetween(3, 20), randomBoolean()));
            break;
        case 4:
            int numOrders = randomIntBetween(1, 3);
            for (int i = 0; i < numOrders; i++) {
                orders.addAll(randomOrder());
            }
            break;
        default:
            fail();
        }
        return orders;
    }

}
