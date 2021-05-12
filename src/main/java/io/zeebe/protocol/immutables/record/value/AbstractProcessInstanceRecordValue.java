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

import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;
import io.zeebe.protocol.immutables.ZeebeStyle;
import org.immutables.value.Value;

@Value.Immutable
@ZeebeStyle
public abstract class AbstractProcessInstanceRecordValue extends AbstractJsonSerializable
    implements ProcessInstanceRecordValue {

  @Value.Default
  @Override
  public BpmnElementType getBpmnElementType() {
    return BpmnElementType.UNSPECIFIED;
  }
}
