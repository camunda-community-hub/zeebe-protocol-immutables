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

import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.asTypedList;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.cast;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.convertToMsgPack;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.ofString;

import io.zeebe.protocol.Protocol;
import io.zeebe.protocol.impl.record.value.job.JobRecord;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class JobRecordArbitrary extends AbstractArbitraryBase
    implements Arbitrary<JobRecord>, RecordValueArbitrary<JobRecord> {
  private RandomGenerator<String> type = defaultType();
  private RandomGenerator<Map<String, String>> customHeaders = defaultCustomHeaders();
  private RandomGenerator<String> worker = defaultWorker();
  private RandomGenerator<Integer> retries = defaultRetries();
  private RandomGenerator<Long> deadline = defaultDeadline();
  private RandomGenerator<String> errorMessage = defaultErrorMessage();
  private RandomGenerator<String> errorCode = defaultErrorCode();
  private RandomGenerator<String> elementId = defaultElementId();
  private RandomGenerator<Long> elementInstanceKey = defaultElementInstanceKey();
  private RandomGenerator<String> bpmnProcessId = defaultBpmnProcessId();
  private RandomGenerator<Integer> workflowDefinitionVersion = defaultWorkflowDefinitionVersion();
  private RandomGenerator<Long> workflowInstanceKey = defaultWorkflowInstanceKey();
  private RandomGenerator<Long> workflowKey = defaultWorkflowKey();
  private RandomGenerator<Map<String, Object>> variables = defaultVariables();

  @Override
  public RandomGenerator<JobRecord> generator(final int genSize) {
    return this::getShrinkable;
  }

  @Override
  public JobRecordArbitrary ofPartitionId(final int partitionId) {
    final JobRecordArbitrary clone = typedClone();
    clone.workflowInstanceKey =
        defaultWorkflowInstanceKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    clone.workflowKey =
        defaultWorkflowKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    clone.elementInstanceKey =
        defaultElementInstanceKey().map(key -> Protocol.encodePartitionId(partitionId, key));
    return clone;
  }

  private Shrinkable<JobRecord> getShrinkable(final Random random) {
    final List<Shrinkable<Object>> shrinkables =
        asTypedList(
            type.next(random),
            customHeaders.next(random),
            variables.next(random),
            worker.next(random),
            retries.next(random),
            deadline.next(random),
            errorMessage.next(random),
            errorCode.next(random),
            elementId.next(random),
            elementInstanceKey.next(random),
            bpmnProcessId.next(random),
            workflowDefinitionVersion.next(random),
            workflowKey.next(random),
            workflowInstanceKey.next(random));
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private JobRecord createUnchecked(final List<Object> params) {
    return createChecked(
        cast(params.get(0)),
        cast(params.get(1)),
        cast(params.get(2)),
        cast(params.get(3)),
        cast(params.get(4)),
        cast(params.get(5)),
        cast(params.get(6)),
        cast(params.get(7)),
        cast(params.get(8)),
        cast(params.get(9)),
        cast(params.get(10)),
        cast(params.get(11)),
        cast(params.get(12)),
        cast(params.get(13)));
  }

  private JobRecord createChecked(
      final String type,
      final Map<String, String> customHeaders,
      final Map<String, Object> variables,
      final String worker,
      final int retries,
      final long deadline,
      final String errorMessage,
      final String errorCode,
      final String elementId,
      final long elementInstanceKey,
      final String bpmnProcessId,
      final int workflowDefinitionVersion,
      final long workflowKey,
      final long workflowInstanceKey) {
    return new JobRecord()
        .setType(type)
        .setBpmnProcessId(bpmnProcessId)
        .setCustomHeaders(convertToMsgPack(customHeaders))
        .setVariables(convertToMsgPack(variables))
        .setWorker(worker)
        .setRetries(retries)
        .setDeadline(deadline)
        .setErrorCode(ofString(errorCode))
        .setErrorMessage(errorMessage)
        .setElementId(elementId)
        .setElementInstanceKey(elementInstanceKey)
        .setWorkflowDefinitionVersion(workflowDefinitionVersion)
        .setWorkflowInstanceKey(workflowInstanceKey)
        .setWorkflowKey(workflowKey);
  }

  private RandomGenerator<String> defaultErrorMessage() {
    return Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(255).generator(1);
  }

  private RandomGenerator<String> defaultErrorCode() {
    return Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(255).generator(1);
  }

  private RandomGenerator<Map<String, String>> defaultCustomHeaders() {
    return Arbitraries.maps(
            Arbitraries.strings().ofMinLength(1).alpha(),
            Arbitraries.strings().ofMinLength(0).alpha())
        .generator(1);
  }

  private RandomGenerator<Map<String, Object>> defaultVariables() {
    return Arbitraries.maps(
            Arbitraries.strings().ofMinLength(1).alpha(), ZeebeArbitraries.variables())
        .generator(1);
  }

  private RandomGenerator<String> defaultType() {
    return Arbitraries.strings().alpha().ofMinLength(1).generator(1);
  }

  private RandomGenerator<String> defaultWorker() {
    return Arbitraries.strings().alpha().ofMinLength(1).generator(1);
  }

  private RandomGenerator<Integer> defaultRetries() {
    return RandomGenerators.integers(0, Integer.MAX_VALUE);
  }

  private RandomGenerator<Long> defaultDeadline() {
    return RandomGenerators.longs(0, Long.MAX_VALUE);
  }

  private RandomGenerator<String> defaultElementId() {
    return Arbitraries.strings().ofMinLength(1).alpha().generator(1);
  }

  private RandomGenerator<String> defaultBpmnProcessId() {
    return Arbitraries.strings().ofMinLength(1).alpha().generator(1);
  }

  private RandomGenerator<Long> defaultElementInstanceKey() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private RandomGenerator<Long> defaultWorkflowInstanceKey() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private RandomGenerator<Long> defaultWorkflowKey() {
    return RandomGenerators.longs(1, Long.MAX_VALUE);
  }

  private RandomGenerator<Integer> defaultWorkflowDefinitionVersion() {
    return RandomGenerators.integers(1, Integer.MAX_VALUE);
  }
}
