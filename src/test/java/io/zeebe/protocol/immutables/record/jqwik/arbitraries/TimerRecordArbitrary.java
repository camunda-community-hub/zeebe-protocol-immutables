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
import io.zeebe.protocol.impl.record.value.timer.TimerRecord;
import io.zeebe.util.buffer.BufferUtil;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class TimerRecordArbitrary extends AbstractArbitraryBase
    implements Arbitrary<TimerRecord>, RecordValueArbitrary<TimerRecord> {
  private RandomGenerator<Long> elementInstanceKey = defaultElementInstanceKey();
  private RandomGenerator<Long> workflowInstanceKey = defaultWorkflowInstanceKey();
  private RandomGenerator<Long> workflowKey = defaultWorkflowKey();
  private RandomGenerator<String> targetElementId = defaultTargetElementId();
  private RandomGenerator<Integer> repetitions = defaultRepetitions();
  private RandomGenerator<Long> dueDate = defaultDueDate();

  TimerRecordArbitrary() {}

  @Override
  public RandomGenerator<TimerRecord> generator(final int genSize) {
    return this::getShrinkable;
  }

  @Override
  public TimerRecordArbitrary ofPartitionId(final int partitionId) {
    final TimerRecordArbitrary clone = typedClone();
    clone.elementInstanceKey =
        defaultElementInstanceKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    clone.workflowInstanceKey =
        defaultWorkflowInstanceKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    clone.workflowKey =
        defaultWorkflowKey().map(key -> Protocol.encodePartitionId(partitionId, key));

    return clone;
  }

  private Shrinkable<TimerRecord> getShrinkable(final Random random) {
    final var nextElementInstanceKey = elementInstanceKey.next(random);
    final var nextWorkflowInstanceKey = workflowInstanceKey.next(random);
    final var nextWorkflowKey = workflowKey.next(random);
    final var nextTargetElementId = targetElementId.next(random);
    final var nextRepetitions = repetitions.next(random);
    final var nextDueDate = dueDate.next(random);
    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            nextElementInstanceKey,
            nextWorkflowInstanceKey,
            nextWorkflowKey,
            nextTargetElementId,
            nextRepetitions,
            nextDueDate);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private TimerRecord createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)),
        cast(params.get(1)),
        cast(params.get(2)),
        cast(params.get(3)),
        cast(params.get(4)),
        cast(params.get(5)));
  }

  private TimerRecord createChecked(
      final long elementInstanceKey,
      final long workflowInstanceKey,
      final long workflowKey,
      final String targetElementId,
      final int repetitions,
      final long dueDate) {
    final var record = new TimerRecord();

    return record
        .setElementInstanceKey(elementInstanceKey)
        .setTargetElementId(BufferUtil.wrapString(targetElementId))
        .setWorkflowInstanceKey(workflowInstanceKey)
        .setWorkflowKey(workflowKey)
        .setRepetitions(repetitions)
        .setDueDate(dueDate);
  }

  private RandomGenerator<Long> defaultElementInstanceKey() {
    return Arbitraries.longs().between(1, Long.MAX_VALUE).generator(1);
  }

  private RandomGenerator<Long> defaultWorkflowInstanceKey() {
    return Arbitraries.longs().between(1, Long.MAX_VALUE).generator(1);
  }

  private RandomGenerator<Long> defaultWorkflowKey() {
    return Arbitraries.longs().between(1, Long.MAX_VALUE).generator(1);
  }

  private RandomGenerator<String> defaultTargetElementId() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }

  private RandomGenerator<Integer> defaultRepetitions() {
    return RandomGenerators.integers(0, 5);
  }

  private RandomGenerator<Long> defaultDueDate() {
    return RandomGenerators.longs(1, Duration.ofDays(30).toMillis());
  }
}
