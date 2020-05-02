/*
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.protocol.immutables.record.jqwik.arbitraries;

import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.asTypedList;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.cast;

import io.zeebe.protocol.Protocol;
import io.zeebe.protocol.impl.record.value.deployment.Workflow;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class WorkflowArbitrary extends AbstractArbitraryBase implements Arbitrary<Workflow> {
  private RandomGenerator<String> bpmnProcessName = defaultBpmnProcessName();
  private RandomGenerator<String> resourceName = defaultResourceName();
  private RandomGenerator<Integer> version = defaultVersion();
  private RandomGenerator<Long> key = defaultKey();

  @Override
  public RandomGenerator<Workflow> generator(final int genSize) {
    return this::getShrinkable;
  }

  public WorkflowArbitrary ofPartitionId(final int partitionId) {
    final WorkflowArbitrary clone = typedClone();
    clone.key = defaultKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    return clone;
  }

  private Shrinkable<Workflow> getShrinkable(final Random random) {
    final var nextBpmnProcessName = bpmnProcessName.next(random);
    final var nextResourceName = resourceName.next(random);
    final var nextVersion = version.next(random);
    final var nextKey = key.next(random);

    final List<Shrinkable<Object>> shrinkables =
        asTypedList(nextBpmnProcessName, nextResourceName, nextVersion, nextKey);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private Workflow createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)), cast(params.get(1)), cast(params.get(2)), cast(params.get(3)));
  }

  private Workflow createChecked(
      final String bpmnProcessName, final String resourceName, final int version, final long key) {
    return new Workflow()
        .setBpmnProcessId(bpmnProcessName)
        .setResourceName(resourceName)
        .setVersion(version)
        .setKey(key);
  }

  private RandomGenerator<String> defaultBpmnProcessName() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }

  private RandomGenerator<String> defaultResourceName() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }

  private RandomGenerator<Integer> defaultVersion() {
    return RandomGenerators.integers(1, 255);
  }

  private RandomGenerator<Long> defaultKey() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }
}
