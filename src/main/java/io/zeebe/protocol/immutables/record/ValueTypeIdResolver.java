/*
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.protocol.immutables.record;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.ValueType;

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

  private Class<? extends RecordValue> mapValueTypeToRecordValue(final ValueType valueType) {
    switch (valueType) {
      case JOB:
        return ImmutableJobRecordValue.class;
      case DEPLOYMENT:
        return ImmutableDeploymentRecordValue.class;
      case WORKFLOW_INSTANCE:
        return ImmutableWorkflowInstanceRecordValue.class;
      case INCIDENT:
        return ImmutableIncidentRecordValue.class;
      case MESSAGE:
        return ImmutableMessageRecordValue.class;
      case MESSAGE_SUBSCRIPTION:
        return ImmutableMessageSubscriptionRecordValue.class;
      case WORKFLOW_INSTANCE_SUBSCRIPTION:
        return ImmutableWorkflowInstanceSubscriptionRecordValue.class;
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
      case WORKFLOW_INSTANCE_CREATION:
        return ImmutableWorkflowInstanceCreationRecordValue.class;
      case ERROR:
        return ImmutableErrorRecordValue.class;
      case WORKFLOW_INSTANCE_RESULT:
        return ImmutableWorkflowInstanceResultRecordValue.class;
      case SBE_UNKNOWN:
      case NULL_VAL:
      default:
        throw new IllegalArgumentException("Unknown value type " + valueType);
    }
  }
}
