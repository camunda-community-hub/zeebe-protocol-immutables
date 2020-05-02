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
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.isCommandRecord;

import io.zeebe.protocol.Protocol;
import io.zeebe.protocol.immutables.record.jqwik.registry.RecordValueRegistry;
import io.zeebe.protocol.immutables.record.jqwik.registry.RegistryEntry;
import io.zeebe.protocol.impl.record.CopiedRecord;
import io.zeebe.protocol.impl.record.RecordMetadata;
import io.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordType;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class RecordArbitrary<T extends UnifiedRecordValue> implements Arbitrary<Record<T>> {

  private final RegistryEntry<T> registryEntry;

  private RandomGenerator<RecordMetadata> recordMetadata;
  private RandomGenerator<Integer> partitionId;
  private RandomGenerator<Long> position;
  private RandomGenerator<Long> timestamp;

  private RandomGenerator<Long> sourcePosition;
  private RandomGenerator<T> value;
  private RandomGenerator<Long> key;

  RecordArbitrary(final Class<T> valueClass) {
    this.registryEntry =
        Objects.requireNonNull(
            RecordValueRegistry.registry().get(valueClass),
            String.format(
                "Expected a registered record mapping for value class %s, but none found",
                valueClass));

    this.recordMetadata = defaultRecordMetadata();
    this.partitionId = defaultPartitionId();
    this.position = defaultPosition();
    this.timestamp = defaultTimestamp();
  }

  @Override
  public RandomGenerator<Record<T>> generator(final int genSize) {
    return this::getShrinkable;
  }

  private Shrinkable<Record<T>> getShrinkable(final Random random) {
    final var nextRecordMetadata = recordMetadata.next(random);
    final var nextPosition = position.next(random);
    final var nextPartitionId = partitionId.next(random);

    // todo: I think getting the shrinkable value directly messes up how we then later falsify
    // things, but it's good enough for now...
    final var nextValue = generateValue(nextPartitionId.value()).next(random);
    final var nextKey =
        generateKey(nextRecordMetadata.value().getRecordType(), nextPartitionId.value())
            .next(random);
    final var nextSourcePosition =
        generateSourcePosition(nextRecordMetadata.value().getRecordType(), nextPosition.value())
            .next(random);
    final var nextTimestamp = timestamp.next(random);
    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            nextRecordMetadata,
            nextValue,
            nextPosition,
            nextPartitionId,
            nextKey,
            nextSourcePosition,
            nextTimestamp);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private Record<T> createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)),
        cast(params.get(1)),
        cast(params.get(2)),
        cast(params.get(3)),
        cast(params.get(4)),
        cast(params.get(5)),
        cast(params.get(6)));
  }

  private Record<T> createChecked(
      final RecordMetadata metadata,
      final T value,
      final long position,
      final int partitionId,
      final long keyLowBits,
      final long sourcePosition,
      final long timestamp) {
    final var key =
        isCommandRecord(metadata.getRecordType())
            ? -1
            : Protocol.encodePartitionId(partitionId, keyLowBits);
    final var sourceRecordPosition = sourcePosition >= position ? -1 : sourcePosition;

    return new CopiedRecord<>(
        value, metadata, key, partitionId, position, sourceRecordPosition, timestamp);
  }

  private RandomGenerator<RecordMetadata> defaultRecordMetadata() {
    return ZeebeArbitraries.recordMetadata()
        .ofIntents(registryEntry.getIntentClass())
        .ofValueTypes(registryEntry.getValueType())
        .generator(1);
  }

  private RandomGenerator<Integer> defaultPartitionId() {
    return RandomGenerators.integers(1, 255);
  }

  private RandomGenerator<Long> defaultPosition() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private RandomGenerator<Long> defaultTimestamp() {
    return RandomGenerators.longs(0, Long.MAX_VALUE);
  }

  private RandomGenerator<Long> generateSourcePosition(
      final RecordType recordType, final long position) {
    if (sourcePosition != null) {
      return sourcePosition;
    }

    if (position == 1 || isCommandRecord(recordType)) {
      return ignored -> Shrinkable.unshrinkable(-1L);
    }

    return RandomGenerators.longs(1, position);
  }

  private RandomGenerator<T> generateValue(final int partitionId) {
    if (value != null) {
      return value;
    }

    return registryEntry.getArbitrarySupplier().ofPartitionId(partitionId).generator(1);
  }

  private RandomGenerator<Long> generateKey(final RecordType recordType, final int partitionId) {
    if (key != null) {
      return key;
    }

    if (isCommandRecord(recordType)) {
      return ignored -> Shrinkable.unshrinkable(-1L);
    }

    return RandomGenerators.longs(1, Long.MAX_VALUE)
        .map(key -> Protocol.encodePartitionId(partitionId, key));
  }
}
