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
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.intent.DeploymentDistributionIntent;
import io.camunda.zeebe.protocol.record.intent.DeploymentIntent;
import io.camunda.zeebe.protocol.record.intent.ErrorIntent;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.camunda.zeebe.protocol.record.intent.Intent;
import io.camunda.zeebe.protocol.record.intent.JobBatchIntent;
import io.camunda.zeebe.protocol.record.intent.JobIntent;
import io.camunda.zeebe.protocol.record.intent.MessageIntent;
import io.camunda.zeebe.protocol.record.intent.MessageStartEventSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.MessageSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessEventIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceCreationIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceResultIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessIntent;
import io.camunda.zeebe.protocol.record.intent.ProcessMessageSubscriptionIntent;
import io.camunda.zeebe.protocol.record.intent.TimerIntent;
import io.camunda.zeebe.protocol.record.intent.VariableDocumentIntent;
import io.camunda.zeebe.protocol.record.intent.VariableIntent;

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

  // allow  high cyclomatic complexity due to large switch case which is still easy to reason about
  @SuppressWarnings({"java:S138", "java:S1541"})
  private Class<? extends Enum<? extends Intent>> mapValueTypeToIntentClass(
      final ValueType valueType) {
    switch (valueType) {
      case JOB:
        return JobIntent.class;
      case DEPLOYMENT:
        return DeploymentIntent.class;
      case PROCESS_INSTANCE:
        return ProcessInstanceIntent.class;
      case INCIDENT:
        return IncidentIntent.class;
      case MESSAGE:
        return MessageIntent.class;
      case MESSAGE_SUBSCRIPTION:
        return MessageSubscriptionIntent.class;
      case PROCESS_MESSAGE_SUBSCRIPTION:
        return ProcessMessageSubscriptionIntent.class;
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
      case PROCESS_INSTANCE_CREATION:
        return ProcessInstanceCreationIntent.class;
      case ERROR:
        return ErrorIntent.class;
      case PROCESS_INSTANCE_RESULT:
        return ProcessInstanceResultIntent.class;
      case PROCESS:
        return ProcessIntent.class;
      case PROCESS_EVENT:
        return ProcessEventIntent.class;
      case DEPLOYMENT_DISTRIBUTION:
        return DeploymentDistributionIntent.class;
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
