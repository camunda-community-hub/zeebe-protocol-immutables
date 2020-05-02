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
import static net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators.choose;

import io.zeebe.protocol.immutables.record.jqwik.registry.RecordValueRegistry;
import io.zeebe.protocol.impl.record.RecordMetadata;
import io.zeebe.protocol.record.RecordType;
import io.zeebe.protocol.record.RejectionType;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.Intent;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class RecordMetadataArbitrary extends AbstractArbitraryBase
    implements Arbitrary<RecordMetadata> {

  private RandomGenerator<RecordType> recordType = defaultRecordType();
  private RandomGenerator<ValueType> valueType = defaultValueType();
  private RandomGenerator<Intent> intent;
  private RandomGenerator<RejectionType> rejectionType;
  private RandomGenerator<String> rejectionReason;
  private RandomGenerator<Long> requestId;
  private RandomGenerator<Integer> streamId;

  RecordMetadataArbitrary() {}

  @Override
  public RandomGenerator<RecordMetadata> generator(final int genSize) {
    return this::getShrinkable;
  }

  public RecordMetadataArbitrary ofValueTypes(final ValueType... valueTypes) {
    final RecordMetadataArbitrary clone = typedClone();
    clone.valueType = RandomGenerators.choose(valueTypes);
    return clone;
  }

  public RecordMetadataArbitrary ofIntents(
      final Class<? extends Enum<? extends Intent>> intentClass) {
    final RecordMetadataArbitrary clone = typedClone();
    clone.intent =
        choose(intentClass.getEnumConstants()).map(Intent.class::cast).filter(t -> t.value() < 255);
    return clone;
  }

  private Shrinkable<RecordMetadata> getShrinkable(final Random random) {
    final var nextValueType = valueType.next(random);
    final var nextRecordType = recordType.next(random);
    final var nextIntent = generateIntent(nextValueType.value()).next(random);
    final var nextRejectionType = generateRejectionType(nextRecordType.value()).next(random);
    final var nextRejectionReason = generateRejectionReason(nextRejectionType.value()).next(random);
    final var nextRequestId = generateRequestId(nextRecordType.value()).next(random);
    final var nextStreamId = generateStreamId(nextRecordType.value()).next(random);
    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            nextRecordType,
            nextValueType,
            nextIntent,
            nextRejectionType,
            nextRejectionReason,
            nextRequestId,
            nextStreamId);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private RecordMetadata createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)),
        cast(params.get(1)),
        cast(params.get(2)),
        cast(params.get(3)),
        cast(params.get(4)),
        cast(params.get(5)),
        cast(params.get(6)));
  }

  private RecordMetadata createChecked(
      final RecordType recordType,
      final ValueType valueType,
      final Intent intent,
      final RejectionType rejectionType,
      final String rejectionReason,
      final long requestId,
      final int streamId) {
    final var metadata = new RecordMetadata();
    metadata
        .protocolVersion(1)
        .valueType(valueType)
        .recordType(recordType)
        .intent(intent)
        .rejectionType(rejectionType)
        .rejectionReason(rejectionReason)
        .requestId(requestId)
        .requestStreamId(streamId);

    return metadata;
  }

  private RandomGenerator<RecordType> defaultRecordType() {
    return choose(RecordType.class).filter(t -> t.value() < 255);
  }

  private RandomGenerator<ValueType> defaultValueType() {
    return choose(ValueType.class).filter(t -> t.value() < 255);
  }

  private RandomGenerator<RejectionType> generateRejectionType(final RecordType recordType) {
    if (rejectionType != null) {
      return rejectionType;
    }

    if (recordType == RecordType.COMMAND_REJECTION) {
      return choose(RejectionType.class).filter(t -> filterSbeEnum(t.value()));
    }

    return r -> Shrinkable.unshrinkable(RejectionType.NULL_VAL);
  }

  private RandomGenerator<String> generateRejectionReason(final RejectionType rejectionType) {
    if (rejectionReason != null) {
      return rejectionReason;
    }

    if (rejectionType != RejectionType.NULL_VAL) {
      return Arbitraries.strings().ofMinLength(1).ofMaxLength(30).alpha().generator(1);
    }

    return r -> Shrinkable.unshrinkable("");
  }

  private RandomGenerator<Long> generateRequestId(final RecordType recordType) {
    if (requestId != null) {
      return requestId;
    }

    if (isCommandRecord(recordType)) {
      return RandomGenerators.longs(1, Long.MAX_VALUE);
    }

    return r -> Shrinkable.unshrinkable(-1L);
  }

  private RandomGenerator<Integer> generateStreamId(final RecordType recordType) {
    if (streamId != null) {
      return streamId;
    }

    if (isCommandRecord(recordType)) {
      return RandomGenerators.integers(1, Integer.MAX_VALUE);
    }

    return r -> Shrinkable.unshrinkable(-1);
  }

  private RandomGenerator<Intent> generateIntent(final ValueType valueType) {
    if (intent != null) {
      return intent;
    }

    final Class<? extends Enum<? extends Intent>> intentClass =
        RecordValueRegistry.registry().get(valueType).getIntentClass();
    return Arbitraries.of(intentClass)
        .map(Intent.class::cast)
        .filter(t -> filterSbeEnum(t.value()))
        .generator(1);
  }

  private boolean filterSbeEnum(final short value) {
    return value < 255;
  }
}
