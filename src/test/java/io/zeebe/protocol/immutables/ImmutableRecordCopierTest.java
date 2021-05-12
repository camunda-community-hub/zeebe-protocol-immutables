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
package io.zeebe.protocol.immutables;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.camunda.zeebe.protocol.record.ValueType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

final class ImmutableRecordCopierTest {

  /**
   * This test checks that every known record value type is handled by the {@link
   * ImmutableRecordCopier}, and should fail if it isn't. This is a smoke test when updating Zeebe
   * versions to detect new {@link io.camunda.zeebe.protocol.record.ValueType} instances.
   */
  @EnumSource(
      value = ValueType.class,
      names = {"NULL_VAL", "SBE_UNKNOWN"},
      mode = Mode.EXCLUDE)
  @ParameterizedTest
  void shouldHandleEveryKnownValueType(final ValueType type) {
    assertThatCode(() -> ImmutableRecordCopier.deepCopyOfRecordValue(type, null))
        .isNotInstanceOf(IllegalArgumentException.class);
  }
}
