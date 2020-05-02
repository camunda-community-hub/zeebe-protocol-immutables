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
package io.zeebe.protocol.immutables.record.jqwik.registry;

import io.zeebe.protocol.immutables.record.jqwik.arbitraries.RecordValueArbitrary;
import io.zeebe.protocol.immutables.record.jqwik.arbitraries.ZeebeArbitraries;
import io.zeebe.protocol.impl.record.UnifiedRecordValue;
import io.zeebe.protocol.impl.record.value.deployment.DeploymentRecord;
import io.zeebe.protocol.impl.record.value.error.ErrorRecord;
import io.zeebe.protocol.impl.record.value.job.JobRecord;
import io.zeebe.protocol.impl.record.value.message.MessageRecord;
import io.zeebe.protocol.impl.record.value.timer.TimerRecord;
import io.zeebe.protocol.record.RecordValue;
import io.zeebe.protocol.record.ValueType;
import io.zeebe.protocol.record.intent.DeploymentIntent;
import io.zeebe.protocol.record.intent.ErrorIntent;
import io.zeebe.protocol.record.intent.Intent;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.protocol.record.intent.MessageIntent;
import io.zeebe.protocol.record.intent.TimerIntent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class RecordValueRegistry {
  private final Map<Class<? extends UnifiedRecordValue>, RegistryEntry<?>> implRegistry;
  private final Map<ValueType, RegistryEntry<?>> typeRegistry;

  public RecordValueRegistry() {
    this.implRegistry = new HashMap<>();
    this.typeRegistry = new EnumMap<>(ValueType.class);
    registerAll();
  }

  public static RecordValueRegistry registry() {
    return Singleton.INSTANCE;
  }

  @SuppressWarnings("unchecked")
  public <T extends UnifiedRecordValue> RegistryEntry<T> get(final ValueType valueType) {
    return (RegistryEntry<T>) typeRegistry.get(valueType);
  }

  @SuppressWarnings("unchecked")
  public <T extends UnifiedRecordValue> RegistryEntry<T> get(final Class<T> valueClass) {
    return (RegistryEntry<T>) implRegistry.get(valueClass);
  }

  public Set<ValueType> types() {
    return typeRegistry.keySet();
  }

  public Set<Class<? extends UnifiedRecordValue>> classes() {
    return implRegistry.keySet();
  }

  private void registerAll() {
    register(
        DeploymentRecord.class,
        ValueType.DEPLOYMENT,
        DeploymentIntent.class,
        ZeebeArbitraries::deploymentRecords);
    register(ErrorRecord.class, ValueType.ERROR, ErrorIntent.class, ZeebeArbitraries::errorRecords);
    register(JobRecord.class, ValueType.JOB, JobIntent.class, ZeebeArbitraries::jobRecords);
    register(
        MessageRecord.class,
        ValueType.MESSAGE,
        MessageIntent.class,
        ZeebeArbitraries::messageRecords);
    register(TimerRecord.class, ValueType.TIMER, TimerIntent.class, ZeebeArbitraries::timerRecords);
  }

  private <T extends UnifiedRecordValue & RecordValue> void register(
      final Class<T> implClass,
      final ValueType valueType,
      final Class<? extends Enum<? extends Intent>> intentClass,
      final Supplier<RecordValueArbitrary<T>> arbitrary) {
    final var property = new RegistryEntry<>(implClass, valueType, intentClass, arbitrary);
    implRegistry.put(implClass, property);
    typeRegistry.put(valueType, property);
  }

  private static final class Singleton {
    private static final RecordValueRegistry INSTANCE = new RecordValueRegistry();
  }
}
