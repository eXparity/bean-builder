Exparity Stub  [![Build Status](https://travis-ci.org/eXparity/exparity-stub.svg?branch=master)](https://travis-ci.org/eXparity/exparity-stub) [![Coverage Status](https://coveralls.io/repos/eXparity/exparity-stub/badge.png?branch=master)](https://coveralls.io/r/eXparity/exparity-stub?branch=master)
=============
A Java library to support creation of test stubs. The library can create complete object graphs populated completely randomly, or can be populated with a mix of random and configured values.

Licensed under [BSD License][].

Downloads
---------
You can obtain exparity-stub jar from [maven central][]. To include your project in:

A maven project

    <dependency>
        <groupId>org.exparity</groupId>
        <artifactId>exparity-stub</artifactId>
        <version>2.0.7</version>
    </dependency>

Versions 2.x.x onwards require Java 8. If you are using an earlier version of Java 8 then include version although this does not contain all the up-to-date features

    <dependency>
        <groupId>org.exparity</groupId>
        <artifactId>exparity-stub</artifactId>
        <version>1.1.5</version>
    </dependency>
            
Usage
-------------

How To Create Random Types, Collections, and Beans
--------------------------------------------------

The RandomBuilder class will create random instances of java primitive and native types and fully populate Java classes with random data. It is ideal for testing persistence, marshaling, and for unit and integration testing. Random objects can be created using the static methods exposed on either the RandomBuilder class, the BeanBuilder class, or the StubBuilder. The preferred approach is to use the RandomBuilder class as it provides a terser interface, however when applying multiple overrides to the builder the BeanBuilder or StubBuilder may be better. The BeanBuilder is for use on classes which implement the Java Beans standard whereas the StubBuilder will support interfaces and abstract classes.

The RandomBuilder can be used to create random values for primitives, for arrays, and for complete object graphs including classes, interfaces, and abstract classes. For example

	String randomName = RandomBuilder.aRandomString();
	Gender raGender = RandomBuilder.aRandomEnum(Gender.class);
	Person aPerson = RandomBuilder.aRandomInstanceOf(Person.class);
	Person []  aCrowd = RandomBuilder.aRandomArrayOf(Person.class);
	List<Person>  aCrowd = RandomBuilder.aRandomListOf(Person.class);

The RandomBuilder can be configured to restrict how certain properties, types, paths, subtypes, and collections are built. For example.

	Person aPerson = RandomBuilder.aRandomInstanceOf(Person.class,
	RandomBuilder.path("person.siblings.firstName","Bob"));
	Person aPerson = RandomBuilder.aRandomInstanceOf(Person.class, RandomBuilder.property("firstName","Bob"));
	Person aPersonWithBrothersAndSisters = RandomBuilder.aRandomInstanceOf(Person.class,
	RandomBuilder.collectionSizeForPath("person.sibling",2,5));
	  
or after static importing

	Person aPerson = aRandomInstanceOf(Person.class, path("person.siblings.firstName","Bob"));
	Person aPerson = aRandomInstanceOf(Person.class, property("firstName","Bob"));
	Person aPersonWithBrothersAndSisters = aRandomInstanceOf(Person.class, 
			collectionSizeForPath("person.sibling",2,5)
		);

Multiple restrictions can be applied at the same time. For example

	Person aPerson = aRandomInstanceOf(Person.class, 
			collectionSizeForPath("person.sibling",2), 
			path("person.siblings.firstName","Bob")
		);

The RandomBuilder class includes includes factory methods for:

* __aRandomArrayOf__ - Create a random array of a class, interface, or abstract class
* __aRandomArrayOfEnum__ - Create a random array of an Enum
* __aRandomBoolean__ - Create a random Boolean
* __aRandomByte__ - Create a random Byte
* __aRandomByteArray__ - Create a random byte[]
* __aRandomChar__ - Create a random Character
* __aRandomCollectionOf__ - Create a random collection of a class, interface, or abstract class
* __aRandomDate__ - Create a random Date
* __aRandomLocalDate__ - Create a random LocalDate
* __aRandomLocalDateTime__ - Create a random LocalDateTime
* __aRandomZonedDateTime__ - Create a random ZonedDateTime
* __aRandomLocalTime__ - Create a random LocalTime
* __aRandomInstant__ - Create a random Instant
* __aRandomEnum__ - Create a random instance of an Enum
* __aRandomDecimal__ - Create a random Boolean
* __aRandomDouble__ - Create a random Double
* __aRandomFloat__ - Create a random Float
* __aRandomInteger__ - Create a random Integer
* __aRandomInstanceOf__ - Create a random instance of class, interface, or abstract class
* __aRandomListOf__ - Create a random list of a class, interface, or abstract class
* __aRandomLong__ - Create a random Long
* __aRandomShort__ - Create a random Short
* __aRandomString__ - Create a random alphanumeric String
* __aRandomAsciiString__ - Create a random ascii String
* __aRandomAlphabeticString__ - Create a random alphabetic String
* __aRandomNumericString__ - Create a random numeric String
* __aRandomStubOf__ - Create a random stub instance (See below section on StubBuilder)

It also includes factory methods for the restrictions to be applied when building an instance of an object:

* __path__ - Restrict the value of a path
* __property__ - Restrict the value of a property
* __excludePath__ - Exclude a path from being populated randomly
* __excludeProperty__ - Exclude a property from being populated randomly
* __subtype__ - Define which subtype to use when instantiating a subtype
* __collectionSize__ - Define the size to use for collections
* __collectionSizeForPath__ - Define the size to use for the collection for a path
* __collectionSizeForProperty__ - Define the size to use for the collection for a property

The Javadocs include examples on all methods so you can look there for examples for specific methods

How To Create Random Stub Interfaces 
------------------------------------

If you are creating tests where you would like an interface or any java class to return random values then the StubBuilder class can do this for you. It is ideal for unit testing where you are less concerned about the responses from the objects, and more that they respond with non null values.

	Person aPerson = StubBuilder.aRandomStubOf(Person.class).build();
	MyInterface service = StubBuilder.aRandomStubOf(MyInterface.class).with(mockService);

You can override the instance returned by the stub for a given method by using the .with() method.

    MyService mockService = Mockito.mock(MyService.class);
    MyInterface service = StubBuilder.aRandomStubOf(MyInterface.class).with(mockService).build()

A Stub can also be created using the RandomBuilder; 

	MyInterface service = RandomBuilder.aRandomStubOf(MyInterface.class);
	Person aPerson = RandomBuilder.aRandomStubOf(Person.class);

Source
------
The source is structured along the lines of the maven standard folder structure for a jar project.

  * Core classes [src/main/java]
  * Unit tests [src/test/java]

The source includes a pom.xml for building with Maven 

Release Notes
-------------
Changes 2.0.6 -> 2.0.7
  * Fix building of map properties containing arrays
  * Add support for creating arrays via aRandomInstanceOf
  * Add support for creating interfaces via aRandomInstanceOf 
  * Add support for creating abstract classes via aRandomInstanceOf 

Changes 2.0.5 -> 2.0.6
  * Fix stub void handling
  * Reduce default random string length to 10
  * Add aRandomAlphabetic
  * Add aRandomAscii
  * Add aRandomNumeric
  
Changes 2.0.4 -> 2.0.5
  * Fix inconsistent handling of hashcode and equals on stubs
  
Changes 2.0.2 -> 2.0.4
  * Add Support for building random types and objects without default constructors
  
Changes 2.0.1 -> 2.0.2
  * Add LocalTime support and implement with overrides for instance
  
Changes 2.0.0 -> 2.0.1
  * Add StubBuilder
  
Changes 1.1.5 -> 2.0.0
  * Updates for Java 8. Add java.time.* types and annotate with @FunctionalInterface
  
Changes 1.1.2 -> 1.1.3
  * Bump version of exparity-bean to 1.0.3

Changes 1.1.1 -> 1.1.2
  * Fix Issue #4.

Changes 1.1.0 -> 1.1
  * Port from uk.co.it.modular beans library to exparity-bean

Acknowledgements
----------------
Developers:
  * Stewart Bissett

[BSD License]: http://opensource.org/licenses/BSD-3-Clause
[Maven central]: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22hamcrest-date%22
