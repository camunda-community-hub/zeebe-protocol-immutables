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
package io.zeebe.protocol.immutables.record.assertj;

import io.zeebe.protocol.immutables.record.ImmutableRecord;
import io.zeebe.protocol.record.Assertions;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import org.assertj.core.api.AbstractObjectAssert;

public final class ImmutableRecordAssert<T extends RecordValue>
    extends AbstractObjectAssert<ImmutableRecordAssert<T>, ImmutableRecord<T>> {
  public ImmutableRecordAssert(final ImmutableRecord<T> record) {
    super(record, ImmutableRecordAssert.class);
  }

  public <U extends RecordValue> ImmutableRecordAssert<T> isEqualToIgnoringValue(
      final Record<U> expected) {
    Assertions.assertThat(actual)
        .hasKey(expected.getKey())
        .hasPartitionId(expected.getPartitionId())
        .hasPosition(expected.getPosition())
        .hasSourceRecordPosition(expected.getSourceRecordPosition())
        .hasTimestamp(expected.getTimestamp())
        .hasValueType(expected.getValueType())
        .hasIntent(expected.getIntent())
        .hasRecordType(expected.getRecordType())
        .hasRejectionType(expected.getRejectionType())
        .hasRejectionReason(expected.getRejectionReason());

    return myself;
  }
}
