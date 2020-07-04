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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.protocol.immutables.record.assertj.ImmutableAssertions;
import io.zeebe.protocol.impl.record.value.deployment.DeploymentRecord;
import io.zeebe.protocol.impl.record.value.error.ErrorRecord;
import io.zeebe.protocol.impl.record.value.job.JobRecord;
import io.zeebe.protocol.impl.record.value.message.MessageRecord;
import io.zeebe.protocol.impl.record.value.timer.TimerRecord;
import io.zeebe.protocol.record.Assertions;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.value.DeploymentRecordValue;
import io.zeebe.protocol.record.value.ErrorRecordValue;
import io.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.protocol.record.value.MessageRecordValue;
import io.zeebe.protocol.record.value.TimerRecordValue;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public final class ImmutableRecordSerializationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Property(tries = 100)
  void canSerializeDeploymentRecord(@ForAll final Record<DeploymentRecord> expected)
      throws JsonProcessingException {
    // given
    final var serialized = MAPPER.writeValueAsString(expected);

    // when
    final var actual =
        MAPPER.readValue(
            serialized, new TypeReference<ImmutableRecord<DeploymentRecordValue>>() {});

    // then
    final var expectedValue = expected.getValue();
    final var actualValue = actual.getValue();
    ImmutableAssertions.assertThat(actual).isEqualToIgnoringValue(expected);
    assertThat(actualValue.getDeployedWorkflows())
        .zipSatisfy(
            expectedValue.getDeployedWorkflows(),
            (a, e) -> {
              Assertions.assertThat(a)
                  .hasBpmnProcessId(e.getBpmnProcessId())
                  .hasWorkflowKey(e.getWorkflowKey())
                  .hasResourceName(e.getResourceName())
                  .hasVersion(e.getVersion());
            });
    assertThat(actualValue.getResources())
        .zipSatisfy(
            expectedValue.getResources(),
            (a, e) -> {
              Assertions.assertThat(a)
                  .hasResource(e.getResource())
                  .hasResourceType(e.getResourceType())
                  .hasResourceName(e.getResourceName());
            });
  }

  @Property(tries = 100)
  void canSerializeErrorRecord(@ForAll final Record<ErrorRecord> expected)
      throws JsonProcessingException {
    // given
    final var serialized = MAPPER.writeValueAsString(expected);

    // when
    final var actual =
        MAPPER.readValue(serialized, new TypeReference<ImmutableRecord<ErrorRecordValue>>() {});

    // then
    final var actualValue = actual.getValue();
    final var expectedValue = expected.getValue();
    ImmutableAssertions.assertThat(actual).isEqualToIgnoringValue(expected);
    Assertions.assertThat(actualValue)
        .hasErrorEventPosition(expectedValue.getErrorEventPosition())
        .hasWorkflowInstanceKey(expectedValue.getWorkflowInstanceKey())
        .hasExceptionMessage(expectedValue.getExceptionMessage())
        .hasStacktrace(expectedValue.getStacktrace());
  }

  @Property(tries = 100)
  void canSerializeJobRecord(@ForAll final Record<JobRecord> expected)
      throws JsonProcessingException {
    // given
    final var serialized = MAPPER.writeValueAsString(expected);

    // when
    final var actual =
        MAPPER.readValue(serialized, new TypeReference<ImmutableRecord<JobRecordValue>>() {});

    // then
    final var actualValue = actual.getValue();
    final var expectedValue = expected.getValue();
    ImmutableAssertions.assertThat(actual).isEqualToIgnoringValue(expected);
    Assertions.assertThat(actualValue)
        .hasBpmnProcessId(expectedValue.getBpmnProcessId())
        .hasCustomHeaders(expectedValue.getCustomHeaders())
        .hasDeadline(expectedValue.getDeadline())
        .hasElementId(expectedValue.getElementId())
        .hasElementInstanceKey(expectedValue.getElementInstanceKey())
        .hasErrorCode(expectedValue.getErrorCode())
        .hasErrorMessage(expectedValue.getErrorMessage())
        .hasRetries(expectedValue.getRetries())
        .hasType(expectedValue.getType())
        .hasWorker(expectedValue.getWorker())
        .hasWorkflowDefinitionVersion(expectedValue.getWorkflowDefinitionVersion())
        .hasWorkflowKey(expectedValue.getWorkflowKey())
        .hasFieldOrPropertyWithValue("workflowInstanceKey", expectedValue.getWorkflowInstanceKey());
  }

  @Property(tries = 100)
  void canSerializeMessageRecord(@ForAll final Record<MessageRecord> expected)
      throws JsonProcessingException {
    // given
    final var serialized = MAPPER.writeValueAsString(expected);

    // when
    final var actual =
        MAPPER.readValue(serialized, new TypeReference<ImmutableRecord<MessageRecordValue>>() {});

    // then
    final var actualValue = actual.getValue();
    final var expectedValue = expected.getValue();
    ImmutableAssertions.assertThat(actual).isEqualToIgnoringValue(expected);
    Assertions.assertThat(actualValue)
        .hasMessageId(expectedValue.getMessageId())
        .hasCorrelationKey(expectedValue.getCorrelationKey())
        .hasName(expectedValue.getName())
        .hasTimeToLive(expectedValue.getTimeToLive());
    assertThat(actualValue.getVariables()).containsExactlyEntriesOf(expectedValue.getVariables());
  }

  @Property(tries = 100)
  void canSerializeTimerRecord(@ForAll final Record<TimerRecord> expected)
      throws JsonProcessingException {
    // given
    final var serialized = MAPPER.writeValueAsString(expected);

    // when
    final var actual =
        MAPPER.readValue(serialized, new TypeReference<ImmutableRecord<TimerRecordValue>>() {});

    // then
    final var expectedValue = expected.getValue();
    ImmutableAssertions.assertThat(actual).isEqualToIgnoringValue(expected);
    Assertions.assertThat(actual.getValue())
        .hasElementInstanceKey(expectedValue.getElementInstanceKey())
        .hasWorkflowInstanceKey(expectedValue.getWorkflowInstanceKey())
        .hasWorkflowKey(expectedValue.getWorkflowKey())
        .hasDueDate(expectedValue.getDueDate())
        .hasTargetElementId(expectedValue.getTargetElementId())
        .hasRepetitions(expectedValue.getRepetitions());
  }
}
