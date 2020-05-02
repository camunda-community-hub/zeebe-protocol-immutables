[![Build Status](https://travis-ci.org/zeebe-io/zeebe-protocol-immutables.svg?branch=master)](https://travis-ci.org/zeebe-io/zeebe-protocol-immutables)

# zeebe-protocol-immutables

> Note: the library was not yet released any where, this will be done shortly.

This library provides an implementation of the Zeebe protocol which can be serialized and deserialized using Jackson.

It can be used in your exporters to copy and serialize Zeebe records to any data format Jackson supports. It can also deserialize
records exported as JSON via `Record#toJson`. This means you can now easily rehydrate the records from JSON into
equivalent `Record` objects, which are then compatible with standard Zeebe tooling. This is particularly useful for
integration tests for Zeebe itself, for your exporters, and any application which has to deal with Zeebe records. 

## Usage

Add the Maven dependency to your `pom.xml`

```xml
<dependency>
  <groupId>io.zeebe.protocol</groupId>
  <artifactId>zeebe-protocol-immutables</artifactId>
  <version>1.0.0</version>
</dependency>
```

or the appropriate Gradle dependency:

```groovy
implementation 'io.zeebe.protocol:zeebe-protocol-immutables:1.0.0'
```

### Deserialize from JSON

If you want to deserialize records that were serialized using `Record#toJson`, you can use the provided
`RecordTypeReference` with an `ObjectReader`, e.g.

```java
final RecordTypeReference<?> genericRecordType = new RecordTypeReference<>();
final ObjectMapper mapper = new ObjectMapper();
final Record<?> record = mapper.readValue(json, genericRecordType);
```

The deserialized record will have the proper value concrete type, though you may have to cast it. If you
already know the type you expect, however, you can also type the reference properly, e.g.

```java
final RecordTypeReference<DeploymentRecordValue> recordType = new RecordTypeReference<>();
final ObjectMapper mapper = new ObjectMapper();
final Record<DeploymentRecordValue> record = mapper.readValue(json, recordType);
```

### Serialize record

If you are writing an exporter and want to serialize an incoming record, but don't want to bother with writing your own
serialization mechanism, you can also leverage this library by creating an `ImmutableRecord` from an existing record, and
using any of Jackson's supported serialization formats, e.g. message pack, protobuf, cbor, YAML, etc.

For example, an exporter could do the following:

```java
private static final ObjectMapper MAPPER = new ObjectMapper();

public void export(Record record) {
  final Record clone = ImmutableRecord.builder().from(expected);
  
  try(final OutputStream out = createOutputStream()) {
    MAPPER.writeValue(out, clone);  
  }
}
```

You could then have configured the `ObjectMapper` to write YAML, CBOR, etc., beforehand.

## Development

You can simply build the project from source using the provided Gradle wrapper:

```shell
./gradlew build
```

To enforce code style and conventions, we use checkstyle and spotless, so run your builds as:

```shell
./gradlew spotlessApply build
```

To deploy and install the maven artifact, you can use the install task:

```shell
./gradlew install
```

## Design

The actual implementation of the protocol is generated via [Immutables](https://immutables.github.io/) at compile time.

> If you're using IntelliJ, you should [configure your IDE accordingly](https://immutables.github.io/apt.html#intellij-idea)

Since the correct interfaces have to be annotated, we cannot simply use the ones provided in the `zeebe-protocol` module, hence the abstract classes
which implement these interfaces and are properly annotated. Note that these classes are package-private by design,
as users should be using the generated `Immutable*` versions of these classes.

### Deserialization

The `Record` class in the protocol is typed; the value's concrete class and the intent's concrete enum are both derived from
the `Record#getValueType`. As such, both fields are annotated using `@JsonTypeInfo` which points to that property, and are given
a corresponding type resolver (see `ValueTypeIdResolver` and `IntentTypeIdResolver`), which allows Jackson to properly deserialize
a `Record<DeploymentRecordValue>` concretely as `ImmutableRecord<ImmutableDeploymentRecordValue>`. 

## Testing

Currently testing is sort of a playground - I decided to go for property based testing using jqwik, and I'm learning as I go, so there are bound
to be mistakes in how I'm doing this. Contributions are more than welcome :)

## Code of Conduct

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to
code-of-conduct@zeebe.io.
