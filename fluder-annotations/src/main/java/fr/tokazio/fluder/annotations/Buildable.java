package fr.tokazio.fluder.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Buildable {

    String builderName() default "Builder";

    String creatorName() default "Creator";

    String intermediatePrefix() default "";

    String buildMethodName() default "build";

    String instanceMethodName() default "getInstance";

}
