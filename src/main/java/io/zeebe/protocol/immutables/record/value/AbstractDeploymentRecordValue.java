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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.camunda.zeebe.protocol.record.value.DeploymentRecordValue;
import io.camunda.zeebe.protocol.record.value.deployment.DeploymentResource;
import io.camunda.zeebe.protocol.record.value.deployment.ProcessMetadataValue;
import io.zeebe.protocol.immutables.ZeebeStyle;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableDeploymentResource;
import io.zeebe.protocol.immutables.record.value.deployment.ImmutableProcessMetadata;
import java.util.Collections;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ZeebeStyle
public abstract class AbstractDeploymentRecordValue extends AbstractJsonSerializable
    implements DeploymentRecordValue {

  @Value.Default
  @JsonDeserialize(contentAs = ImmutableDeploymentResource.class)
  @Override
  public List<DeploymentResource> getResources() {
    return Collections.emptyList();
  }

  @Value.Default
  @JsonDeserialize(contentAs = ImmutableProcessMetadata.class)
  @Override
  public List<ProcessMetadataValue> getProcessesMetadata() {
    return Collections.emptyList();
  }
}
