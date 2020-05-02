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
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.DeploymentIntent;
import io.zeebe.protocol.record.intent.ErrorIntent;
import io.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.protocol.record.intent.Intent;
import io.zeebe.protocol.record.intent.JobBatchIntent;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.zeebe.protocol.record.intent.TimerIntent;
import io.zeebe.protocol.record.intent.VariableDocumentIntent;
import io.zeebe.protocol.record.intent.VariableIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceCreationIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceResultIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceSubscriptionIntent;

final class IntentTypeIdResolver extends TypeIdResolverBase {
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
    return typeFactory.constructType(mapValueTypeToIntentClass(valueType));
  }

  private Class<? extends Enum<? extends Intent>> mapValueTypeToIntentClass(
      final ValueType valueType) {
    switch (valueType) {
      case JOB:
        return JobIntent.class;
      case DEPLOYMENT:
        return DeploymentIntent.class;
      case WORKFLOW_INSTANCE:
        return WorkflowInstanceIntent.class;
      case INCIDENT:
        return IncidentIntent.class;
      case MESSAGE:
        return MessageIntent.class;
      case MESSAGE_SUBSCRIPTION:
        return MessageSubscriptionIntent.class;
      case WORKFLOW_INSTANCE_SUBSCRIPTION:
        return WorkflowInstanceSubscriptionIntent.class;
      case JOB_BATCH:
        return JobBatchIntent.class;
      case TIMER:
        return TimerIntent.class;
      case MESSAGE_START_EVENT_SUBSCRIPTION:
        return MessageStartEventSubscriptionIntent.class;
      case VARIABLE:
        return VariableIntent.class;
      case VARIABLE_DOCUMENT:
        return VariableDocumentIntent.class;
      case WORKFLOW_INSTANCE_CREATION:
        return WorkflowInstanceCreationIntent.class;
      case ERROR:
        return ErrorIntent.class;
      case WORKFLOW_INSTANCE_RESULT:
        return WorkflowInstanceResultIntent.class;
      case SBE_UNKNOWN:
      case NULL_VAL:
      default:
        return UnknownIntent.class;
    }
  }

  private enum UnknownIntent implements Intent {
    UNKNOWN;

    @Override
    public short value() {
      return Intent.UNKNOWN.value();
    }
  }
}
