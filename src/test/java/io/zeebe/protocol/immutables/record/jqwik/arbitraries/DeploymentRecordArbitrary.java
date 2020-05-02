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
package io.zeebe.protocol.immutables.record.jqwik.arbitraries;

import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.asTypedList;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.cast;

import io.zeebe.protocol.impl.record.value.deployment.DeploymentRecord;
import io.zeebe.protocol.impl.record.value.deployment.DeploymentResource;
import io.zeebe.protocol.impl.record.value.deployment.Workflow;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

public final class DeploymentRecordArbitrary extends AbstractArbitraryBase
    implements Arbitrary<DeploymentRecord>, RecordValueArbitrary<DeploymentRecord> {
  private RandomGenerator<List<DeploymentResource>> resources = defaultResources();
  private RandomGenerator<List<Workflow>> workflows = defaultWorkflows();

  @Override
  public RandomGenerator<DeploymentRecord> generator(final int genSize) {
    return this::getShrinkable;
  }

  @Override
  public DeploymentRecordArbitrary ofPartitionId(final int partitionId) {
    final var newWorkflows =
        defaultWorkflows(ZeebeArbitraries.workflows().ofPartitionId(partitionId));
    return ofWorkflows(newWorkflows);
  }

  public DeploymentRecordArbitrary ofWorkflows(final RandomGenerator<List<Workflow>> workflows) {
    final DeploymentRecordArbitrary clone = typedClone();
    clone.workflows = workflows;
    return clone;
  }

  public DeploymentRecordArbitrary ofResources(
      final RandomGenerator<List<DeploymentResource>> resources) {
    final DeploymentRecordArbitrary clone = typedClone();
    clone.resources = resources;
    return clone;
  }

  private Shrinkable<DeploymentRecord> getShrinkable(final Random random) {
    final var nextResources = resources.next(random);
    final var nextWorkflows = workflows.next(random);

    final List<Shrinkable<Object>> shrinkables = asTypedList(nextResources, nextWorkflows);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private DeploymentRecord createUnchecked(final List<Object> params) {
    return createChecked(cast(params.get(0)), cast(params.get(1)));
  }

  private DeploymentRecord createChecked(
      final List<DeploymentResource> resources, final List<Workflow> workflows) {
    final var record = new DeploymentRecord();
    final var resourcesArray = record.resources();
    final var workflowsArray = record.workflows();

    resources.forEach(
        resource ->
            resourcesArray
                .add()
                .setResourceName(resource.getResourceName())
                .setResource(resource.getResource())
                .setResourceType(resource.getResourceType()));

    workflows.forEach(
        workflow ->
            workflowsArray
                .add()
                .setResourceName(workflow.getResourceName())
                .setBpmnProcessId(workflow.getBpmnProcessId())
                .setVersion(workflow.getVersion())
                .setKey(workflow.getKey()));

    return record;
  }

  private RandomGenerator<List<DeploymentResource>> defaultResources() {
    return ZeebeArbitraries.deploymentResources().list().ofMinSize(0).ofMaxSize(5).generator(1);
  }

  private RandomGenerator<List<Workflow>> defaultWorkflows() {
    return defaultWorkflows(ZeebeArbitraries.workflows());
  }

  private RandomGenerator<List<Workflow>> defaultWorkflows(final WorkflowArbitrary arbitrary) {
    return arbitrary.list().ofMinSize(0).ofMaxSize(5).generator(1);
  }
}
