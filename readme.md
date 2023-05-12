# Fluder

[![Java CI with Gradle, Sonar and Codecov](https://github.com/tokazio/fluderJavaGenerator/actions/workflows/gradle.yml/badge.svg)](https://github.com/tokazio/fluderJavaGenerator/actions/workflows/gradle.yml)
[![CodeScene Code Health](https://codescene.io/projects/39222/status-badges/code-health)](https://codescene.io/projects/39222)
[![CodeScene System Mastery](https://codescene.io/projects/39222/status-badges/system-mastery)](https://codescene.io/projects/39222)

Flu(ent) (Buil)der generated by an annotation processor.

This force to build an object by giving the needed data in specific order.

See https://dzone.com/articles/fluent-builder-pattern

## @Buildable
Mark a class for wich you want a fluent builder to be generated.

### Parameters

##### builderName
Change the generated builder class name.
Empty actually use the target class name appending 'Builder' to it.
Remember that's a class name, the first letter will be upper case.
You can use '$' that will be replaced by the target class name.
      
##### creatorName
Change the generated creator class name.
Empty actually use the target class name appending 'Creator' to it.
Remember that's a class name, the first letter will be upper case.
You can use '$' that will be replaced by the target class name.
      
##### intermediatePrefix default ""
For the chaining, FluderProcessor generating multiple intermediate interfaces.
This can put a prefix behind these interfaces.
Remember that's an interface name, the first letter will be upper case.

##### buildMethodName default "build"

Change the finisher 'build()' call to your own.    
Remember that's a method name, the first letter will be lower case.

##### instanceMethodName default "getInstance"

Change the initial 'getInstance' call to your own.
Remember that's a method name, the first letter will be lower case.

## Configuration annotations

### @Group

Not already implemented

### @Ignore

Ignore the field

### @Name

Rename the setter, setContent can be transformed to setBody with @Name("body") on the 'content' field.

### @Optional
An @Optional field can be defined in any order (after the required fields).
You can give a default value via @Optional or the one defined at the field initialisation will be kept.

### @Order
Change/force the definition order of the fields.
If you use it, you must specify it for each fields.

### transient/final

Transient/final fields are ignored.

### non public

The builder use reflection API to build a private field

### non public constructor

When you make a builder, you generally hide the possibility to instanciate the target object without this Builder.
Then the private constructor is accessed via the reflection API.

### Validation annotation

The builder can handle some javax.validation annotations

### @Nonnull / @NotNull

This will add a check in the setter to ensure that it's a non nullable value that will be set.
It throws an IllegalArgumentException when passing a null value when building.

```
@Override
	public EmailFrom setTo(java.lang.String in){
		if(in==null){
			throw new IllegalArgumentException("setTo can't be called with a null parameter. The target field is marked as @Nonnull");
		}
		this.to = in;
		return this;
	}
```

### @NotEmpty

This will add a check in the setter to ensure that it's a non empty value that will be set.
It throws an IllegalArgumentException when passing an empty value when building.

```
@Override
	public EmailFrom setTo(java.lang.String in){
		if(in==null){
			throw new IllegalArgumentException("setTo can't be called with a null parameter. The target field is marked as @Nonnull");
		}
		this.to = in;
		return this;
	}
```

## Example

This Email

```
@Buildable
public class Email {

    @Nonnull
    @Order(0)
    String to;
    @NotNull
    @Order(2)
    String subject;
    @Order(3)
    @Name("body")
    String content;
    @Optional("\"bcc-default\"")
    String bcc;
    @Optional
    String cc;
    @Order(1)
    private String from;

    @Ignore
    private Date date;

    transient Object notInBuilder;

    //Hide me forcing the builder usage
    private Email(){
        super();
    }
}
```
Can now be created like this
```
Email email = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject("subject")
                .setBody("content")
                //.setBcc("bcc")
                //.setCc("cc")
                .build();
```
