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

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import io.camunda.zeebe.protocol.record.RecordValue;
import io.camunda.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableProcess;

final class ValueTypeIdResolver extends TypeIdResolverBase {

  @Override
  public String idFromValue(final Object value) {
    return ((ValueType) value).name();
  }

  @Override
  public String idFromValueAndType(final Object value, final Class<?> suggestedType) {
    return idFromValue(value);
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }

  @Override
  public JavaType typeFromId(final DatabindContext context, final String id) {
    final var valueType = ValueType.valueOf(id);
    final var typeFactory = context.getTypeFactory();
    return typeFactory.constructType(mapValueTypeToRecordValue(valueType));
  }

  // allow  high cyclomatic complexity due to large switch case which is still easy to reason about
  @SuppressWarnings({"java:S138", "java:S1541"})
  private Class<? extends RecordValue> mapValueTypeToRecordValue(final ValueType valueType) {
    switch (valueType) {
      case JOB:
        return ImmutableJobRecordValue.class;
      case DEPLOYMENT:
        return ImmutableDeploymentRecordValue.class;
      case PROCESS_INSTANCE:
        return ImmutableProcessInstanceRecordValue.class;
      case INCIDENT:
        return ImmutableIncidentRecordValue.class;
      case MESSAGE:
        return ImmutableMessageRecordValue.class;
      case MESSAGE_SUBSCRIPTION:
        return ImmutableMessageSubscriptionRecordValue.class;
      case PROCESS_MESSAGE_SUBSCRIPTION:
        return ImmutableProcessMessageSubscriptionRecordValue.class;
      case JOB_BATCH:
        return ImmutableJobBatchRecordValue.class;
      case TIMER:
        return ImmutableTimerRecordValue.class;
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return ImmutableMessageStartEventSubscriptionRecordValue.class;
      case VARIABLE:
        return ImmutableVariableRecordValue.class;
      case VARIABLE_DOCUMENT:
        return ImmutableVariableDocumentRecordValue.class;
      case PROCESS_INSTANCE_CREATION:
        return ImmutableProcessInstanceCreationRecordValue.class;
      case ERROR:
        return ImmutableErrorRecordValue.class;
      case PROCESS_INSTANCE_RESULT:
        return ImmutableProcessInstanceResultRecordValue.class;
      case PROCESS_EVENT:
        return ImmutableProcessEventRecordValue.class;
      case PROCESS:
        return ImmutableProcess.class;
      case DEPLOYMENT_DISTRIBUTION:
        return ImmutableDeploymentDistributionRecordValue.class;
      case SBE_UNKNOWN:
      case NULL_VAL:
      default:
        throw new IllegalArgumentException("Unknown value type " + valueType);
    }
  }
}
