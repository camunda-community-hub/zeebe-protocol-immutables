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

import io.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.zeebe.protocol.record.RecordType;
import java.util.ArrayList;
import java.util.List;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

final class ArbitrariesUtil {
  private ArbitrariesUtil() {}

  static byte[] toArray(final List<Byte> byteList) {
    final var byteArray = new byte[byteList.size()];
    for (int index = 0; index < byteList.size(); index++) {
      byteArray[index] = byteList.get(index);
    }

    return byteArray;
  }

  static DirectBuffer convertToMsgPack(final Object value) {
    return new UnsafeBuffer(MsgPackConverter.convertToMsgPack(value));
  }

  static DirectBuffer ofString(final String value) {
    return new UnsafeBuffer(value.getBytes());
  }

  @SuppressWarnings("unchecked")
  static <T> List<T> asTypedList(final Object... objects) {
    final List<T> list = new ArrayList<>();
    for (final Object object : objects) {
      list.add((T) object);
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  static <T> T cast(final Object value) {
    return (T) value;
  }

  static boolean isCommandRecord(final RecordType recordType) {
    return recordType != RecordType.EVENT;
  }
}
