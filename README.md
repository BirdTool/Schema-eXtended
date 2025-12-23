# Schema eXtended

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-1.3.0-blue.svg?style=for-the-badge)

## 📖 Overview

**Schema eXtended** is a robust validation library for the JVM, heavily inspired by **Zod** (JavaScript/TypeScript). It allows developers to validate data structures and user inputs with a fluent, concise, and type-safe API.

Gone are the days of verbose `if/else` blocks. Define your schema once, validate everywhere.

## 🚀 Technologies

* **Core:** Written in **Java** to ensure maximum compatibility across the entire JVM ecosystem.
* **Extensions:** Includes powerful **Kotlin** extensions for a modern, expressive, and fluent developer experience.

## ❓ Why Schema eXtended?

### Why mix Java and Kotlin?
By using **Java** for the core logic, we guarantee that the library can be used in any JVM project (Legacy Java, Modern Java, Kotlin, Scala, etc.) without issues.
However, we provide dedicated **Kotlin** extensions to leverage features like *extension functions*, making the syntax even cleaner for Kotlin users.

### The Problem it Solves
Instead of writing repetitive validation logic:
```java
// The old way
if (age < 18 || age > 100) {
    throw new Exception("Invalid age");
}
```
You write declarative schemas:

```java
// The Schema eXtended way
new IntegerSchema().min(18).max(100).parse(age);
```

- - -

## 💻 Usage Examples
### ☕ Java Usage
Using the library in a standard Java environment.

#### 1. Validating Numbers 

```Java
import studio.styx.schemaEXtended.core.schemas.numbersSchemas.IntegerSchema;

public class Main {
    public static void main(String[] args) {
        IntegerSchema ageSchema = new IntegerSchema()
        .min(18, "You must be at least 18 years old")
        .max(120)
        .defaultValue(18);
        
        // Valid parsing
        var result = ageSchema.parse(25); 
        System.out.println(result.getValue()); // 25
        
        // Handling errors
        var errorResult = ageSchema.parse(10);
        if (!errorResult.isSuccess()) {
             System.out.println(errorResult.getErrors()); // ["You must be at least 18 years old"]
        }
    }
}
```

#### 2. Validating Complex Objects
```Java

import studio.styx.schemaEXtended.core.schemas.ObjectSchema;

ObjectSchema userSchema = new ObjectSchema()
    .addProperty("username", new StringSchema().minLength(3))
    .addProperty("age", new IntegerSchema().min(18))
    .strict(); // Disallow unknown keys

// Can parse Maps, JSON-like Strings, or POJOs
userSchema.parse("username=JohnDoe, age=30");
```

### 🟣 Kotlin Usage (The Super Fluent Way)
If you are using Kotlin, you can use our extension functions to validate data directly on the objects.

#### 1. Validate "on the fly" (`.withSchema`)
Perfect for quick validations of existing variables.

```Kotlin
val email = "contact@styx.studio" // this mail is a example
    .withSchema()
    .email()
    .parseOrThrow()

val age = 25
    .withSchema()
    .min(18)
    .max(50)
    .parse() // Returns ParseResult
```

#### 2. Parse and Convert (`.asTypeSchema`)
Perfect for processing raw user input (Strings) into typed data automatically.

```Kotlin
// "coerce" is enabled automatically!
val userAge: Int = "70"
    .asIntSchema()
    .min(18)
    .parseOrThrow()

// Handles currency or precision math
val price: BigDecimal = "99.90"
    .asBigDecimalSchema()
    .min(1.0)
    .parseOrThrow()
```

## 📦 Batch Validation

Sometimes you need to validate multiple fields at once (like a form submission) and retrieve **all** errors simultaneously, rather than stopping at the first failure.

### 🟣 Kotlin (DSL)
Use the `validateBatch` block for a clean, declarative syntax.

```kotlin
try {
    val result = validateBatch {
        // Syntax: "Key" rules value.withSchema()...
        "username" rules "Jo".withSchema().min(3).max(20)
        "age"      rules "15".asIntSchema().min(18) // Auto-coercion
        "email"    rules "invalid-email".withSchema().email()
    }.getOrThrow()

    // If successful, 'result' is a Map<String, Any> containing the clean values
    println(result)

} catch (e: SchemaIllegalArgumentException) {
    // If failed, you get ALL errors at once
    val errors = e.parseResult.errors
    println(errors) 
    // Output: ["[username] Length must be at least 3", "[age] Must be at least 18", ...]
}

```

### ☕ Java

Use the `BatchValidator` builder.
**Note:** In Java, you must explicitly `.bind(value)` the data to the schema instance.

```java
import studio.styx.schemaEXtended.core.BatchValidator;

// 1. Prepare validator
BatchValidator validator = new BatchValidator()
    .add("username", new StringSchema().min(3).bind("Jo"))
    .add("age", new IntegerSchema().coerce().min(18).bind("15"));

// 2. Validate
try {
    Map<String, Object> result = validator.validate().getOrThrow();
    System.out.println("Success: " + result);
    
} catch (SchemaIllegalArgumentException e) {
    // Access all errors
    System.out.println("Validation failed: " + e.getParseResult().getErrors());
}
```

## 🛠️ Installation

Currently, **Schema eXtended** is not hosted on a public repository (like Maven Central). To use it, you must build and install it locally.

### Step 1: Clone and Install
Clone this repository and run the gradle task to publish it to your local Maven cache.

```bash
git clone [https://github.com/YourUsername/SchemaEXtended.git](https://github.com/YourUsername/SchemaEXtended.git)
cd SchemaEXtended
./gradlew publishToMavenLocal

```

### Step 2: Add to your Project

In your project's `build.gradle.kts` (Kotlin DSL) or `build.gradle` (Groovy), add `mavenLocal()` to your repositories and include the dependency.

**Kotlin DSL (`build.gradle.kts`):**

```kotlin
repositories {
    mavenLocal() // Important: Look in your local machine first
    mavenCentral()
}

dependencies {
    // Check the 'version' in the library's build.gradle file
    implementation("studio.styx.schemaEXtended:SchemaEXtended:1.3.0") 
}

```

**Groovy (`build.gradle`):**

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'studio.styx.schemaEXtended:SchemaEXtended:1.3.0'
}

```