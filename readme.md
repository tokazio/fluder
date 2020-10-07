# Fluder

Fluent Builder generated by an annotation processor.

This force to build an object by giving the needed data in specific order.

### @Buildable
Mark a class for wich you want a fluent builder to be generated.

### @Optional
An @Optional field can be defined in any order (after the required fields).
You can give a default value via @Optional or the one defined at the field initialisation will be kept.

### @Order
Change/force the definition order of the fields.
If you use it, you must specify it for each fields.

### transient/final
Transient/final fields are ignored.

### private
The builder use reflection API to build a private field

## Example

This Email
```
@Buildable
public class Email {


    @Order(0)
    String to;
    @Order(2)
    String subject;
    @Order(3)
    String content;
    @Optional("\"bcc-default\"")
    String bcc;
    @Optional
    String cc;
    @Order(1)
    private String from;

    transient Object notInBuilder;
}
```
Can now be created like this
```
Email email = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject("subject")
                .setContent("content")
                //.setBcc("bcc")
                //.setCc("cc")
                .build();
```