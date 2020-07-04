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
package io.zeebe.protocol.immutables.record;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.RejectionType;
import io.zeebe.protocol.record.intent.Intent;
import org.immutables.value.Value;

@Value.Immutable
@ZeebeStyle
abstract class AbstractRecord<T extends RecordValue> extends AbstractJsonSerializable
    implements Record<T> {

  @Value.Default
  @Override
  public long getSourceRecordPosition() {
    return -1;
  }

  @Value.Default
  @Override
  public long getKey() {
    return -1;
  }

  @Value.Default
  @JsonTypeInfo(use = Id.CUSTOM, include = As.EXTERNAL_PROPERTY, property = "valueType")
  @JsonTypeIdResolver(IntentTypeIdResolver.class)
  @Override
  public Intent getIntent() {
    return Intent.UNKNOWN;
  }

  @Value.Default
  @Override
  public RejectionType getRejectionType() {
    return RejectionType.NULL_VAL;
  }

  @Value.Default
  @Override
  public String getRejectionReason() {
    return "";
  }

  @JsonTypeInfo(use = Id.CUSTOM, include = As.EXTERNAL_PROPERTY, property = "valueType")
  @JsonTypeIdResolver(ValueTypeIdResolver.class)
  @Override
  public abstract T getValue();

  @SuppressWarnings({"MethodDoesntCallSuperMethod", "squid:S2975", "squid:S1182"})
  @Override
  public Record<T> clone() {
    return ImmutableRecord.copyOf(this);
  }
}
