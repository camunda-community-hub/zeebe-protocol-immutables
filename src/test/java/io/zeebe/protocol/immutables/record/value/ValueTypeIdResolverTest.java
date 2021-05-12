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
package io.zeebe.protocol.immutables.record.value;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

final class ValueTypeIdResolverTest {

  /**
   * This test checks that every known record value type is handled. It doesn't validate the
   * correctness of the result - its goal is to be a smoke test to make sure no value types are
   * forgotten
   */
  @EnumSource(
      value = ValueType.class,
      names = {"NULL_VAL", "SBE_UNKNOWN"},
      mode = Mode.EXCLUDE)
  @ParameterizedTest
  void shouldHandleEveryKnownValueType(final ValueType type) throws IOException {
    // given
    final ObjectMapper mapper = new ObjectMapper();
    final DefaultDeserializationContext.Impl baseContext =
        new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
    final DefaultDeserializationContext context =
        baseContext.createInstance(
            mapper.getDeserializationConfig(),
            mapper.createParser("{}"),
            mapper.getInjectableValues());
    final ValueTypeIdResolver resolver = new ValueTypeIdResolver();
    final JavaType resolvedType = resolver.typeFromId(context, resolver.idFromValue(type));

    assertThat(RecordValue.class).isAssignableFrom(resolvedType.getRawClass());
  }
}
