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

import io.camunda.zeebe.protocol.record.Record;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.value.DeploymentDistributionRecordValue;
import io.camunda.zeebe.protocol.record.value.DeploymentRecordValue;
import io.camunda.zeebe.protocol.record.value.ErrorRecordValue;
import io.camunda.zeebe.protocol.record.value.IncidentRecordValue;
import io.camunda.zeebe.protocol.record.value.JobBatchRecordValue;
import io.camunda.zeebe.protocol.record.value.JobRecordValue;
import io.camunda.zeebe.protocol.record.value.MessageRecordValue;
import io.camunda.zeebe.protocol.record.value.MessageStartEventSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.MessageSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessEventRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceCreationRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceResultRecordValue;
import io.camunda.zeebe.protocol.record.value.ProcessMessageSubscriptionRecordValue;
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;
import io.camunda.zeebe.protocol.record.value.VariableDocumentRecordValue;
import io.camunda.zeebe.protocol.record.value.VariableRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.DeploymentResource;
import io.camunda.zeebe.protocol.record.value.deployment.Process;
import io.camunda.zeebe.protocol.record.value.deployment.ProcessMetadataValue;
import io.zeebe.protocol.immutables.record.value.ImmutableDeploymentDistributionRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableDeploymentRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableErrorRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableIncidentRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableJobBatchRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableJobRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableMessageRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableMessageStartEventSubscriptionRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableMessageSubscriptionRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableProcessEventRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableProcessInstanceCreationRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableProcessInstanceRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableProcessInstanceResultRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableProcessMessageSubscriptionRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableRecord;
import io.zeebe.protocol.immutables.record.value.ImmutableRecord.Builder;
import io.zeebe.protocol.immutables.record.value.ImmutableTimerRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableVariableDocumentRecordValue;
import io.zeebe.protocol.immutables.record.value.ImmutableVariableRecordValue;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableDeploymentResource;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableProcess;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableProcessMetadata;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to perform deep copy of any {@link Record<>} implementation to an equivalent {@link
 * ImmutableRecord<>} implementation.
 *
 * <p>This is necessary as by default the {@code copyOf()} methods generated by the library perform
 * only shallow copies to some extent - that is, they will correctly detect when a member has an
 * equivalent {@code Immutable*} type, but not when said member is a collection or a container.
 *
 * <p>If you want to perform deep copies, for example to compare two implementations, you can use
 * the methods below.
 */
public final class ImmutableRecordCopier {

  private ImmutableRecordCopier() {}

  @SuppressWarnings("unchecked")
  public static <T extends RecordValue, U extends T> ImmutableRecord<U> deepCopyOfRecord(
      final Record<T> record) {
    final U value = (U) deepCopyOfRecordValue(record.getValueType(), record.getValue());
    final Builder<T> originalBuilder = ImmutableRecord.<T>builder().from(record);
    final Builder<U> convertedBuilder = (Builder<U>) originalBuilder;

    return convertedBuilder.value(value).build();
  }

  // allow  high cyclomatic complexity due to large switch case which is still easy to reason about
  @SuppressWarnings({"unchecked", "java:S138", "java:S1541"})
  public static <T extends RecordValue, U extends T> T deepCopyOfRecordValue(
      final ValueType type, final T value) {
    switch (type) {
      case JOB:
        return (U) ImmutableJobRecordValue.builder().from((JobRecordValue) value).build();
      case DEPLOYMENT:
        return (U) deepCopyOfDeploymentRecordValue((DeploymentRecordValue) value);
      case PROCESS_INSTANCE:
        return (U)
            ImmutableProcessInstanceRecordValue.builder()
                .from((ProcessInstanceRecordValue) value)
                .build();
      case INCIDENT:
        return (U) ImmutableIncidentRecordValue.builder().from((IncidentRecordValue) value).build();
      case MESSAGE:
        return (U) ImmutableMessageRecordValue.builder().from((MessageRecordValue) value).build();
      case MESSAGE_SUBSCRIPTION:
        return (U)
            ImmutableMessageSubscriptionRecordValue.builder()
                .from((MessageSubscriptionRecordValue) value)
                .build();
      case PROCESS_MESSAGE_SUBSCRIPTION:
        return (U)
            ImmutableProcessMessageSubscriptionRecordValue.builder()
                .from((ProcessMessageSubscriptionRecordValue) value)
                .build();
      case JOB_BATCH:
        return (U) deepCopyOfJobBatchRecordValue((JobBatchRecordValue) value);
      case TIMER:
        return (U) ImmutableTimerRecordValue.builder().from((TimerRecordValue) value).build();
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return (U)
            ImmutableMessageStartEventSubscriptionRecordValue.builder()
                .from((MessageStartEventSubscriptionRecordValue) value)
                .build();
      case VARIABLE:
        return (U) ImmutableVariableRecordValue.builder().from((VariableRecordValue) value).build();
      case VARIABLE_DOCUMENT:
        return (U)
            ImmutableVariableDocumentRecordValue.builder()
                .from((VariableDocumentRecordValue) value)
                .build();
      case PROCESS_INSTANCE_CREATION:
        return (U)
            ImmutableProcessInstanceCreationRecordValue.builder()
                .from((ProcessInstanceCreationRecordValue) value)
                .build();
      case ERROR:
        return (U) ImmutableErrorRecordValue.builder().from((ErrorRecordValue) value).build();
      case PROCESS_INSTANCE_RESULT:
        return (U)
            ImmutableProcessInstanceResultRecordValue.builder()
                .from((ProcessInstanceResultRecordValue) value)
                .build();
      case PROCESS:
        return (U) ImmutableProcess.builder().from((Process) value).build();
      case DEPLOYMENT_DISTRIBUTION:
        return (U)
            ImmutableDeploymentDistributionRecordValue.builder()
                .from((DeploymentDistributionRecordValue) value)
                .build();
      case PROCESS_EVENT:
        return (U)
            ImmutableProcessEventRecordValue.builder()
                .from((ProcessEventRecordValue) value)
                .build();
      case SBE_UNKNOWN:
      case NULL_VAL:
      default:
        throw new IllegalArgumentException("Unknown value type " + type);
    }
  }

  private static ImmutableDeploymentRecordValue deepCopyOfDeploymentRecordValue(
      final DeploymentRecordValue value) {
    final List<ProcessMetadataValue> processes = new ArrayList<>();
    final List<DeploymentResource> resources = new ArrayList<>();

    for (final ProcessMetadataValue process : value.getProcessesMetadata()) {
      final ImmutableProcessMetadata immutableProcess;
      if (process instanceof ImmutableProcessMetadata) {
        immutableProcess = (ImmutableProcessMetadata) process;
      } else {
        immutableProcess = ImmutableProcessMetadata.builder().from(process).build();
      }

      processes.add(immutableProcess);
    }

    for (final DeploymentResource resource : value.getResources()) {
      final ImmutableDeploymentResource immutableResource;
      if (resource instanceof ImmutableDeploymentResource) {
        immutableResource = (ImmutableDeploymentResource) resource;
      } else {
        immutableResource = ImmutableDeploymentResource.builder().from(resource).build();
      }

      resources.add(immutableResource);
    }

    return ImmutableDeploymentRecordValue.builder()
        .from(value)
        .resources(resources)
        .processesMetadata(processes)
        .build();
  }

  private static ImmutableJobBatchRecordValue deepCopyOfJobBatchRecordValue(
      final JobBatchRecordValue value) {
    final List<JobRecordValue> jobs = new ArrayList<>();

    for (final JobRecordValue job : value.getJobs()) {
      final ImmutableJobRecordValue immutableJob;
      if (job instanceof ImmutableJobRecordValue) {
        immutableJob = (ImmutableJobRecordValue) job;
      } else {
        immutableJob = ImmutableJobRecordValue.builder().from(job).build();
      }

      jobs.add(immutableJob);
    }

    return ImmutableJobBatchRecordValue.builder().from(value).jobs(jobs).build();
  }
}
