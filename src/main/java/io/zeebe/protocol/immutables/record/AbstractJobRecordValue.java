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

import io.zeebe.protocol.record.ErrorCode;
import io.zeebe.protocol.record.value.JobRecordValue;
import java.util.Collections;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@ZeebeStyle
abstract class AbstractJobRecordValue extends AbstractJsonSerializable implements JobRecordValue {

  @Value.Default
  @Override
  public Map<String, String> getCustomHeaders() {
    return Collections.emptyMap();
  }

  @Value.Default
  @Override
  public String getErrorMessage() {
    return "";
  }

  @Value.Default
  @Override
  public String getErrorCode() {
    return ErrorCode.NULL_VAL.name();
  }

  @Value.Default
  @Override
  public Map<String, Object> getVariables() {
    return Collections.emptyMap();
  }
}
