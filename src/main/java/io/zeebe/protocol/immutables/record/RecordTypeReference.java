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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import java.lang.reflect.Type;

/**
 * An implementation of {@link TypeReference} which can be used to deserialize incoming JSON records
 * into {@link Record} instances using {@link ImmutableRecord} as implementation type.
 */
public final class RecordTypeReference<T extends RecordValue> extends TypeReference<Record<T>> {
  private final Type type;

  public RecordTypeReference() {
    this.type = TypeFactory.defaultInstance().constructType(ImmutableRecord.class);
  }

  @Override
  public Type getType() {
    return type;
  }
}
