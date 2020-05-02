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
package io.zeebe.protocol.immutables.record.jqwik.registry;

import io.zeebe.protocol.immutables.record.jqwik.arbitraries.RecordValueArbitrary;
import io.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.Intent;
import java.util.function.Supplier;

public final class RegistryEntry<T extends UnifiedRecordValue> {
  private final Class<T> valueClass;
  private final ValueType valueType;
  private final Class<? extends Enum<? extends Intent>> intentClass;
  private final Supplier<RecordValueArbitrary<T>> arbitrarySupplier;

  public RegistryEntry(
      final Class<T> valueClass,
      final ValueType valueType,
      final Class<? extends Enum<? extends Intent>> intentClass,
      final Supplier<RecordValueArbitrary<T>> arbitrarySupplier) {
    this.valueClass = valueClass;
    this.valueType = valueType;
    this.intentClass = intentClass;
    this.arbitrarySupplier = arbitrarySupplier;
  }

  public Class<? extends UnifiedRecordValue> getValueClass() {
    return valueClass;
  }

  public ValueType getValueType() {
    return valueType;
  }

  public Class<? extends Enum<? extends Intent>> getIntentClass() {
    return intentClass;
  }

  public RecordValueArbitrary<T> getArbitrarySupplier() {
    return arbitrarySupplier.get();
  }
}
