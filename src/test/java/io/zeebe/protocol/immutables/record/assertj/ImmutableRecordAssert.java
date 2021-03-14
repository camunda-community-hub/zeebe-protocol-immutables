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
package io.zeebe.protocol.immutables.record.assertj;

import io.zeebe.protocol.immutables.record.ImmutableDeployedWorkflow;
import io.zeebe.protocol.immutables.record.ImmutableDeploymentRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableDeploymentResource;
import io.zeebe.protocol.immutables.record.ImmutableErrorRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableIncidentRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableJobBatchRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableJobRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableMessageRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableMessageStartEventSubscriptionRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableMessageSubscriptionRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableRecord;
import io.zeebe.protocol.immutables.record.ImmutableTimerRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableVariableDocumentRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableVariableRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableWorkflowInstanceCreationRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableWorkflowInstanceRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableWorkflowInstanceResultRecordValue;
import io.zeebe.protocol.immutables.record.ImmutableWorkflowInstanceSubscriptionRecordValue;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.value.DeploymentRecordValue;
import io.zeebe.protocol.record.value.ErrorRecordValue;
import io.zeebe.protocol.record.value.IncidentRecordValue;
import io.zeebe.protocol.record.value.JobBatchRecordValue;
import io.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.protocol.record.value.MessageRecordValue;
import io.zeebe.protocol.record.value.MessageStartEventSubscriptionRecordValue;
import io.zeebe.protocol.record.value.MessageSubscriptionRecordValue;
import io.zeebe.protocol.record.value.TimerRecordValue;
import io.zeebe.protocol.record.value.VariableDocumentRecordValue;
import io.zeebe.protocol.record.value.VariableRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceCreationRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceResultRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceSubscriptionRecordValue;
import io.zeebe.protocol.record.value.deployment.DeployedWorkflow;
import io.zeebe.protocol.record.value.deployment.DeploymentResource;
import java.util.Collections;
import org.assertj.core.api.AbstractObjectAssert;

public final class ImmutableRecordAssert<T extends RecordValue>
    extends AbstractObjectAssert<ImmutableRecordAssert<T>, ImmutableRecord<T>> {

  public ImmutableRecordAssert(final ImmutableRecord<T> record) {
    super(record, ImmutableRecordAssert.class);
  }

  /**
   * Equality between two records with possible different implementations is performed by first
   * copying the expected value into an equivalent {@code Immutable*} representation. This is done
   * because we can then easily compare equality between both objects as they now have the same
   * implementation.
   *
   * <p>NOTE: it may seem tautological to assert like this, but the generated builder is typically
   * quite good, so building, say, a {@link ImmutableDeploymentRecordValue} out of a {@link
   * DeploymentRecordValue} should produce a valid copy, which we can use to compare against the
   * deserialized object.
   *
   * <p>NOTE: there is one caveat, which is with records/values with nested {@code Immutable*}
   * classes. Since the builder will not convert those but rather just use the interface type, they
   * will not be directly comparable, so they must be converted manually.
   *
   * @param expected the record to compare against
   * @param <U> the type of the record's value
   * @return this assert for chaining
   */
  public <U extends RecordValue> ImmutableRecordAssert<T> isEqualTo(final Record<U> expected) {
    final ImmutableRecord<U> immutableExpected = copyRecordForComparison(expected);

    objects.assertEqual(myself.info, actual, immutableExpected);
    return myself;
  }

  @SuppressWarnings("unchecked")
  private <U extends RecordValue> ImmutableRecord<U> copyRecordForComparison(
      final Record<U> record) {
    final U value = (U) copyRecordValueForComparison(record.getValueType(), record.getValue());
    return ImmutableRecord.<U>builder().from(record).value(value).build();
  }

  private RecordValue copyRecordValueForComparison(final ValueType type, final RecordValue value) {
    switch (type) {
      case JOB:
        return ImmutableJobRecordValue.builder().from((JobRecordValue) value).build();
      case DEPLOYMENT:
        return copyDeploymentRecordForComparison((DeploymentRecordValue) value);
      case WORKFLOW_INSTANCE:
        return ImmutableWorkflowInstanceRecordValue.builder()
            .from((WorkflowInstanceRecordValue) value)
            .build();
      case INCIDENT:
        return ImmutableIncidentRecordValue.builder().from((IncidentRecordValue) value).build();
      case MESSAGE:
        return ImmutableMessageRecordValue.builder().from((MessageRecordValue) value).build();
      case MESSAGE_SUBSCRIPTION:
        return ImmutableMessageSubscriptionRecordValue.builder()
            .from((MessageSubscriptionRecordValue) value)
            .build();
      case WORKFLOW_INSTANCE_SUBSCRIPTION:
        return ImmutableWorkflowInstanceSubscriptionRecordValue.builder()
            .from((WorkflowInstanceSubscriptionRecordValue) value)
            .build();
      case JOB_BATCH:
        return copyJobBatchRecordForComparison((JobBatchRecordValue) value);
      case TIMER:
        return ImmutableTimerRecordValue.builder().from((TimerRecordValue) value).build();
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return ImmutableMessageStartEventSubscriptionRecordValue.builder()
            .from((MessageStartEventSubscriptionRecordValue) value)
            .build();
      case VARIABLE:
        return ImmutableVariableRecordValue.builder().from((VariableRecordValue) value).build();
      case VARIABLE_DOCUMENT:
        return ImmutableVariableDocumentRecordValue.builder()
            .from((VariableDocumentRecordValue) value)
            .build();
      case WORKFLOW_INSTANCE_CREATION:
        return ImmutableWorkflowInstanceCreationRecordValue.builder()
            .from((WorkflowInstanceCreationRecordValue) value)
            .build();
      case ERROR:
        return ImmutableErrorRecordValue.builder().from((ErrorRecordValue) value).build();
      case WORKFLOW_INSTANCE_RESULT:
        return ImmutableWorkflowInstanceResultRecordValue.builder()
            .from((WorkflowInstanceResultRecordValue) value)
            .build();
      case SBE_UNKNOWN:
      case NULL_VAL:
      default:
        throw new IllegalArgumentException("Unknown value type " + type);
    }
  }

  private ImmutableDeploymentRecordValue copyDeploymentRecordForComparison(
      final DeploymentRecordValue value) {
    final ImmutableDeploymentRecordValue.Builder valueBuilder =
        ImmutableDeploymentRecordValue.builder().from(value);

    valueBuilder.deployedWorkflows(Collections.emptyList());
    for (final DeployedWorkflow workflow : value.getDeployedWorkflows()) {
      valueBuilder.addDeployedWorkflows(ImmutableDeployedWorkflow.builder().from(workflow).build());
    }

    valueBuilder.resources(Collections.emptyList());
    for (final DeploymentResource resource : value.getResources()) {
      valueBuilder.addResources(ImmutableDeploymentResource.builder().from(resource).build());
    }

    return valueBuilder.build();
  }

  private ImmutableJobBatchRecordValue copyJobBatchRecordForComparison(
      final JobBatchRecordValue value) {
    final ImmutableJobBatchRecordValue.Builder valueBuilder =
        ImmutableJobBatchRecordValue.builder().from(value);

    valueBuilder.jobs(Collections.emptyList());
    for (final JobRecordValue job : value.getJobs()) {
      valueBuilder.addJobs(ImmutableJobRecordValue.builder().from(job).build());
    }

    return valueBuilder.build();
  }
}
