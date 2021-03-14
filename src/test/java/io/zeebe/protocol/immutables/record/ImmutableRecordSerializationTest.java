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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.broker.exporter.debug.DebugHttpExporter;
import io.zeebe.protocol.immutables.record.assertj.ImmutableAssertions;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.test.exporter.ExporterIntegrationRule;
import io.zeebe.test.util.record.RecordingExporter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

final class ImmutableRecordSerializationTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void shouldSerializeRecords() throws IOException {
    // given
    final ExporterIntegrationRule testHarness = new ExporterIntegrationRule();
    testHarness.configure("debug", DebugHttpExporter.class, Map.of("port", 9000, "limit", 3000));

    // when
    testHarness.start();
    testHarness.performSampleWorkload();

    // then
    // collecting all exported records will wait up to 2 seconds before returning, giving us some
    // assumption for now that everything has been exported
    RecordingExporter.setMaximumWaitTime(2_000);
    final List<Record<RecordValue>> exportedRecords =
        RecordingExporter.records().collect(Collectors.toList());
    final int exportedCount = exportedRecords.size();
    final List<ImmutableRecord<?>> deserializedRecords = fetchJsonRecords(exportedCount);
    assertThat(deserializedRecords).hasSameSizeAs(exportedRecords);

    // since the DebugHttpExporter reverses the order, flip it again to compare against the
    // RecordingExporter
    for (int i = 0; i < exportedCount; i++) {
      final ImmutableRecord<?> deserializedRecord =
          deserializedRecords.get((exportedCount - 1) - i);
      final Record<RecordValue> exportedRecord = exportedRecords.get(i);
      ImmutableAssertions.assertThat(deserializedRecord).isEqualTo(exportedRecord);
    }
  }

  private List<ImmutableRecord<?>> fetchJsonRecords(final int expectedCount) throws IOException {
    final URL url = new URL("http://localhost:9000/records.json");

    return Awaitility.await("until we have at least " + expectedCount + " records")
        .pollInSameThread()
        .until(
            () -> MAPPER.readerFor(new TypeReference<List<ImmutableRecord<?>>>() {}).readValue(url),
            records -> records.size() >= expectedCount);
  }
}
