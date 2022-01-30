# zeebe-protocol-immutables

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Deprecated-yellowgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#deprecated-)
[![](https://img.shields.io/github/v/release/zeebe-io/zeebe-protocol-immutables?sort=semver)](https://github.com/zeebe-io/zeebe-protocol-immutables/releases/latest)
[![Java CI](https://github.com/camunda-community-hub/zeebe-protocol-immutables/actions/workflows/ci.yml/badge.svg)](https://github.com/camunda-community-hub/zeebe-protocol-immutables/actions/workflows/ci.yml)

## ⚠️ This extension is deprecated for Zeebe 1.3 and onwards - you should make use of `io.camunda:zeebe-protocol-jackson` instead.

This library provides an implementation of the Zeebe protocol which can be serialized and
deserialized using Jackson.

It can be used in your exporters to copy and serialize Zeebe records to any data format Jackson
supports. It can also deserialize records exported as JSON via `Record#toJson`. This means you can
now easily rehydrate the records from JSON into equivalent `Record` objects, which are then
compatible with standard Zeebe tooling. This is particularly useful for integration tests for Zeebe
itself, for your exporters, and any application which has to deal with Zeebe records.

## Usage

Add the Maven dependency to your `pom.xml`

```xml
<dependency>
  <groupId>io.zeebe.protocol</groupId>
  <artifactId>zeebe-protocol-immutables</artifactId>
  <version>2.0.0</version>
</dependency>
```

or the appropriate Gradle dependency:

```groovy
implementation 'io.zeebe.protocol:zeebe-protocol-immutables:2.0.0'
```

### Compatibility

`zeebe-protocol-immutables` 1.x is compatible with Zeebe versions 0.23.x (inclusive) to 1.x 
(exclusive).

`zeebe-protocol-immutables` 2.x is compatible with Zeebe 1.x.

It's recommended that you try to always update to the latest version, as this being a community 
project, older versions are less likely to receive updates.

### Deserialize from JSON

If you want to deserialize records that were serialized using `Record#toJson`, you can use the
provided `RecordTypeReference` with an `ObjectReader`, e.g.

```java
final RecordTypeReference<?> genericRecordType = new RecordTypeReference<>();
final ObjectMapper mapper = new ObjectMapper();
final Record<?> record = mapper.readValue(json, genericRecordType);
```

The deserialized record will have the proper value concrete type, though you may have to cast it. If
you already know the type you expect, however, you can also type the reference properly, e.g.

```java
final RecordTypeReference<DeploymentRecordValue> recordType = new RecordTypeReference<>();
final ObjectMapper mapper = new ObjectMapper();
final Record<DeploymentRecordValue> record = mapper.readValue(json, recordType);
```

### Serialize record

If you are writing an exporter and want to serialize an incoming record, but don't want to bother
with writing your own serialization mechanism, you can also leverage this library by creating an
`ImmutableRecord` from an existing record, and using any of Jackson's supported serialization
formats, e.g. message pack, protobuf, cbor, YAML, etc.

For example, an exporter could do the following:

```java
private static final ObjectMapper MAPPER = new ObjectMapper();

public void export(Record<?> record) {
  final Record<?> clone = ImmutableRecordCopier.deepCopyOf(record);
  
  try(final OutputStream out = createOutputStream()) {
    MAPPER.writeValue(out, clone);  
  }
}
```

You could then have configured the `ObjectMapper` to write YAML, CBOR, etc., beforehand.

### Copying and comparing

If you want to compare two `Record<T>` instances with potentially different implementations, the
recommended way is to first convert them both to `ImmutableRecord<T>`.

The easiest way is to use the `ImmutableRecordCopier` utility class. You can do a deep copy of any 
`Record<>` as is, and it will return an equivalent `ImmutableRecord<>`.

```java
final Record<?> record = ...;
final ImmutableRecord<?> copiedRecord = ImmutableRecordCopier.deepCopyOf(record);
```

If you just want to copy the record value, or if you want to have a 
`ImmutableRecord<ImmutableJobRecordValue>`, for example, then you can copy the value first and copy
the record yourself as:

```java
final Record<DeploymentRecordValue> record = ...;
final ImmutableDeploymentRecordValue copiedValue = ImmutableRecordCopier.deepCopyOf(record.getValueType(), record.getValue()); 
final ImmutableRecord<ImmutableDeploymentRecordValue> copiedRecord = ImmutableRecord.builder().from(record).value(copiedValue).build();
```

## Development

### Prerequisites

- [Maven 3.6.x](https://sdkman.io/sdks#maven)
- [Java 11 SDK](https://sdkman.io/jdks)

### Building

This project uses annotations via [Immutables](https://immutables.github.io/) to generate the 
protocol implementation. In order to have the best possible experience, look into 
[integrating it with your IDE](https://immutables.github.io/apt.html). 

Otherwise, to build from the command line:

```shell
mvn install -DskipTests
```

To run the tests, simply run:

```shell
mvn verify
```

## Design

The actual implementation of the protocol is generated via
[Immutables](https://immutables.github.io/) at compile time.

Since the correct interfaces have to be annotated, we cannot simply use the ones provided in the 
`zeebe-protocol` module, hence the abstract classes which implement these interfaces and are
properly annotated. Note that these classes are package-private by design, as users should be using
the generated `Immutable*` versions of these classes.

### Deserialization

Since `Record<T>` is a typed interface, we need to resolve `T` during deserialization. The way to do
so in Zeebe is by using the `Record#getValueType()`.

> You can look at `ValueTypeIdResolver` to see how we resolve the value type to the right value
> class.

The `Intent` of the record is an interface, which also needs to be resolved to the correct type
during deserialization.

> You can look at `IntentTypeIdResolver` to see how raw intents are mapped to the right type.

With these out of the way, you can then easily deserialize a raw JSON payload into an
`ImmutableRecord<T>`, where all types are properly resolved. See [usage](#usage) for more.

## Testing

We assume that the `immutables` library works properly, and as such focus primarily on the
serialization capabilities.

To test this, we use two built-in exporters: specifically the `DebugHttpExporter` and
the `RecordingExporter`.

We start a Zeebe broker, run a sample workload, then wait for all records to be exported.

> NOTE: waiting for all records to be exported is difficult due to the black box nature of the test
> infrastructure, so we simply wait up until some seconds have passed since the last record was
> exported. This is relatively safe since the `ExporterIntegrationRule` already waits until the
> workload is finished and exported to the `RecordingExporter`, so waiting just a few seconds more
> is mostly just to be safe.

The `DebugHttpExporter` provides an endpoint where we can get all exported records as JSON. This is
our sample data set from which we can test the deserialization capabilities of the library.

The `RecordingExporter` provides us with the raw exported records against which we can then compare.

To simplify the comparison, the raw records are first converted to an equivalent
`ImmutableRecord<?>` representation. This may seem tautological, but as mentioned, we assume the
generated code is valid (which includes the builders), and just want to verify that serialization
works as expected.

## Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](/CODE_OF_CONDUCT.md). By
participating, you are expected to uphold this code. Please report unacceptable behavior to
code-of-conduct@zeebe.io.
