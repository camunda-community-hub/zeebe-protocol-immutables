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
package io.zeebe.protocol.immutables.record.jqwik.providers;

import io.zeebe.protocol.immutables.record.jqwik.arbitraries.ZeebeArbitraries;
import io.zeebe.protocol.immutables.record.jqwik.registry.RecordValueRegistry;
import io.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.zeebe.protocol.record.Record;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

public final class RecordArbitraryProvider implements ArbitraryProvider {
  private static final TypeUsage VALUE_TYPE_UPPER_BOUND = TypeUsage.of(UnifiedRecordValue.class);

  @Override
  public boolean canProvideFor(final TypeUsage targetType) {
    final var valueType = targetType.getTypeArgument(0);
    return targetType.isOfType(Record.class)
        && (valueType.isWildcard() || valueType.canBeAssignedTo(VALUE_TYPE_UPPER_BOUND));
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
      final TypeUsage targetType, final SubtypeProvider subtypeProvider) {
    final var subType = targetType.getTypeArgument(0);
    return Collections.singleton(provideForSubType(subType).flatMap(ZeebeArbitraries::records));
  }

  @SuppressWarnings("unchecked")
  private Arbitrary<Class<? extends UnifiedRecordValue>> provideForSubType(
      final TypeUsage subType) {
    final var rawType = (Class<? extends UnifiedRecordValue>) subType.getRawType();
    final Arbitrary<Class<? extends UnifiedRecordValue>> typeArbitrary;
    if (subType.isWildcard() || rawType == UnifiedRecordValue.class) {
      final List<Class<? extends UnifiedRecordValue>> classes =
          new ArrayList<>(RecordValueRegistry.registry().classes());
      return Arbitraries.of(classes);
    } else {
      typeArbitrary = Arbitraries.constant(rawType);
    }

    return typeArbitrary;
  }
}
