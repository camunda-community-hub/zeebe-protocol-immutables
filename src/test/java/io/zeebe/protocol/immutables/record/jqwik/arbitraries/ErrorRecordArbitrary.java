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
import io.zeebe.protocol.impl.record.value.error.ErrorRecord;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;
import org.agrona.LangUtil;

public final class ErrorRecordArbitrary extends AbstractArbitraryBase
    implements Arbitrary<ErrorRecord>, RecordValueArbitrary<ErrorRecord> {
  private RandomGenerator<Long> workflowInstanceKey = defaultWorkflowInstanceKey();
  private RandomGenerator<String> exceptionMessage = defaultExceptionMessage();
  private RandomGenerator<Class<? extends Throwable>> throwableClass = defaultThrowableClass();
  private RandomGenerator<Long> errorEventPosition = defaultErrorEventPosition();

  @Override
  public RandomGenerator<ErrorRecord> generator(final int genSize) {
    return this::getShrinkable;
  }

  @Override
  public ErrorRecordArbitrary ofPartitionId(final int partitionId) {
    final ErrorRecordArbitrary clone = typedClone();
    clone.workflowInstanceKey =
        defaultWorkflowInstanceKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    return clone;
  }

  private Shrinkable<ErrorRecord> getShrinkable(final Random random) {
    final var nextWorkflowInstanceKey = workflowInstanceKey.next(random);
    final var nextExceptionMessage = exceptionMessage.next(random);
    final var nextThrowableClass = throwableClass.next(random);
    final var nextErrorEventPosition = errorEventPosition.next(random);

    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            nextWorkflowInstanceKey,
            nextExceptionMessage,
            nextThrowableClass,
            nextErrorEventPosition);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private ErrorRecord createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)), cast(params.get(1)), cast(params.get(2)), cast(params.get(3)));
  }

  private ErrorRecord createChecked(
      final long workflowInstanceKey,
      final String exceptionMessage,
      final Class<? extends Throwable> throwableClass,
      final long errorEventPosition) {
    final var record = new ErrorRecord();
    record.initErrorRecord(generateThrowable(throwableClass, exceptionMessage), errorEventPosition);
    return record.setWorkflowInstanceKey(workflowInstanceKey);
  }

  private RandomGenerator<Long> defaultWorkflowInstanceKey() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private RandomGenerator<String> defaultExceptionMessage() {
    return Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(255).generator(1);
  }

  private RandomGenerator<Class<? extends Throwable>> defaultThrowableClass() {
    final List<Class<? extends Throwable>> throwableClasses =
        asTypedList(Exception.class, RuntimeException.class, IOException.class);
    return RandomGenerators.choose(throwableClasses);
  }

  private RandomGenerator<Long> defaultErrorEventPosition() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private <T extends Throwable> T generateThrowable(
      final Class<T> throwableClass, final String message) {
    try {
      return throwableClass.getDeclaredConstructor(String.class).newInstance(message);
    } catch (IllegalAccessException
        | InstantiationException
        | InvocationTargetException
        | NoSuchMethodException e) {
      LangUtil.rethrowUnchecked(e);
      return null;
    }
  }
}
