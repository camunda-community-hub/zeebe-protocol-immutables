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

import io.zeebe.protocol.impl.record.UnifiedRecordValue;

public final class ZeebeArbitraries {
  private ZeebeArbitraries() {}

  public static MessageRecordArbitrary messageRecords() {
    return new MessageRecordArbitrary();
  }

  public static TimerRecordArbitrary timerRecords() {
    return new TimerRecordArbitrary();
  }

  public static RecordMetadataArbitrary recordMetadata() {
    return new RecordMetadataArbitrary();
  }

  public static <T extends UnifiedRecordValue> RecordArbitrary<T> records(
      final Class<T> valueClass) {
    return new RecordArbitrary<>(valueClass);
  }

  public static DeploymentRecordArbitrary deploymentRecords() {
    return new DeploymentRecordArbitrary();
  }

  public static ErrorRecordArbitrary errorRecords() {
    return new ErrorRecordArbitrary();
  }

  public static JobRecordArbitrary jobRecords() {
    return new JobRecordArbitrary();
  }

  public static DeploymentResourceArbitrary deploymentResources() {
    return new DeploymentResourceArbitrary();
  }

  public static WorkflowArbitrary workflows() {
    return new WorkflowArbitrary();
  }

  public static VariableArbitrary variables() {
    return new VariableArbitrary();
  }
}
