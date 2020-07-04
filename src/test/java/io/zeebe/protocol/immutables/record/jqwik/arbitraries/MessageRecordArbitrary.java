/*
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import io.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.zeebe.protocol.impl.record.value.message.MessageRecord;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;
import org.agrona.concurrent.UnsafeBuffer;

public final class MessageRecordArbitrary extends AbstractArbitraryBase
    implements Arbitrary<MessageRecord>, RecordValueArbitrary<MessageRecord> {
  private RandomGenerator<String> name = defaultName();
  private RandomGenerator<String> correlationKey = defaultCorrelationKey();
  private RandomGenerator<String> messageId = defaultMessageId();
  private RandomGenerator<Long> timeToLive = defaultTimeToLive();
  private RandomGenerator<Map<String, Object>> variables = defaultVariables();

  MessageRecordArbitrary() {}

  @Override
  public RandomGenerator<MessageRecord> generator(final int genSize) {
    return this::getShrinkable;
  }

  private Shrinkable<MessageRecord> getShrinkable(final Random random) {
    final var nextMessageName = name.next(random);
    final var nextCorrelationKey = correlationKey.next(random);
    final var nextMessageId = messageId.next(random);
    final var nextTimeToLive = timeToLive.next(random);
    final var nextVariables = variables.next(random);
    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            nextMessageName, nextCorrelationKey, nextMessageId, nextTimeToLive, nextVariables);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private MessageRecord createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)),
        cast(params.get(1)),
        cast(params.get(2)),
        cast(params.get(3)),
        cast(params.get(4)));
  }

  private MessageRecord createChecked(
      final String messageName,
      final String correlationKey,
      final String messageId,
      final long timeToLive,
      final Map<String, Object> variables) {
    final var record = new MessageRecord();
    final var packedVariables = new UnsafeBuffer(MsgPackConverter.convertToMsgPack(variables));
    if (messageId != null) {
      record.setMessageId(messageId);
    }

    return record
        .setName(messageName)
        .setCorrelationKey(correlationKey)
        .setTimeToLive(timeToLive)
        .setVariables(packedVariables);
  }

  private RandomGenerator<String> defaultName() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }

  private RandomGenerator<String> defaultCorrelationKey() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }

  private RandomGenerator<String> defaultMessageId() {
    return Arbitraries.strings()
        .ascii()
        .ofMinLength(1)
        .ofMaxLength(255)
        .injectNull(0.25)
        .generator(1);
  }

  private RandomGenerator<Long> defaultTimeToLive() {
    return RandomGenerators.longs(0, Duration.ofDays(30).toMillis());
  }

  private RandomGenerator<Map<String, Object>> defaultVariables() {
    return Arbitraries.maps(
            Arbitraries.strings().ofMinLength(1).ascii(),
            Arbitraries.oneOf(
                ZeebeArbitraries.variables(),
                ZeebeArbitraries.variables().list().ofMinSize(1).ofMaxSize(10)))
        .ofMinSize(0)
        .ofMaxSize(255)
        .generator(1);
  }
}
