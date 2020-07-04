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

import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.asTypedList;
import static io.zeebe.protocol.immutables.record.jqwik.arbitraries.ArbitrariesUtil.cast;

import io.zeebe.protocol.impl.record.value.deployment.DeploymentResource;
import io.zeebe.protocol.record.value.deployment.ResourceType;
import java.util.List;
import java.util.Random;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.RandomGenerator;
import net.jqwik.api.Shrinkable;
import net.jqwik.engine.properties.arbitraries.AbstractArbitraryBase;
import net.jqwik.engine.properties.arbitraries.randomized.RandomGenerators;
import net.jqwik.engine.properties.shrinking.CombinedShrinkable;

/**
 * Note that this class does not produce valid workflows as resources. This is another, bigger
 * scope, though also very interesting :)
 */
public final class DeploymentResourceArbitrary extends AbstractArbitraryBase
    implements Arbitrary<DeploymentResource> {
  private RandomGenerator<byte[]> resource = defaultResource();
  private RandomGenerator<ResourceType> resourceType = defaultResourceType();
  private RandomGenerator<String> resourceName = defaultResourceName();

  @Override
  public RandomGenerator<DeploymentResource> generator(final int genSize) {
    return this::getShrinkable;
  }

  private Shrinkable<DeploymentResource> getShrinkable(final Random random) {
    final var nextResource = resource.next(random);
    final var nextResourceType = resourceType.next(random);
    final var nextResourceName = resourceName.next(random);

    final List<Shrinkable<Object>> shrinkables =
        asTypedList(nextResource, nextResourceType, nextResourceName);
    return new CombinedShrinkable<>(shrinkables, this::createUnchecked);
  }

  private DeploymentResource createUnchecked(final List<Object> params) {
    return createChecked(cast(params.get(0)), cast(params.get(1)), cast(params.get(2)));
  }

  private DeploymentResource createChecked(
      final byte[] resource, final ResourceType resourceType, final String resourceName) {
    return new DeploymentResource()
        .setResource(resource)
        .setResourceType(resourceType)
        .setResourceName(resourceName);
  }

  private RandomGenerator<byte[]> defaultResource() {
    return RandomGenerators.list(RandomGenerators.bytes(Byte.MIN_VALUE, Byte.MAX_VALUE), 0, 4096)
        .map(ArbitrariesUtil::toArray);
  }

  private RandomGenerator<ResourceType> defaultResourceType() {
    return RandomGenerators.choose(ResourceType.values());
  }

  private RandomGenerator<String> defaultResourceName() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).alpha().generator(1);
  }
}
