package fr.tokazio.fluder.processor;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.Collections;

public class FluderProcessorTest {

    private static final String NEW_LINE = "/n";



            /*
            .forSourceString(
            "com.example.A",
            Joiner.on(NEW_LINE).join(
                    "package com.example;",
                    "",
                    "import fr.tokazio.fluder.annotations.Buildable;",
                    "@Buildable",
                    "public class A {",
                    "   ",
                    "   String to;",
                    "   ",
                    "}"
            )
    );
             */

/*                    .forSourceString(
            "com.example.ABuilder",
            Joiner.on(NEW_LINE).join(
                    "package com.example;",
                    "",
                    "",
                    "",
                    "public class ABuilder implements ATo {",
                    "",
                    "   private java.lang.String to;",
                    "",
                    "   private ABuilder() {",
                    "      super();",
                    "   }",
                    "",
                    "   @Override",
                    "   public A build() {",
                    "       A out = null;",
                    "   }",
                    "",
                    "",
                    "",
                    "}"
            )
    );

 */

    @Test
    public void testGeneratedJavaFile() {

        final JavaFileObject simple = JavaFileObjects
                .forResource("Simple.java");
        final JavaFileObject outputSimpleBuilder = JavaFileObjects
                .forResource("SimpleBuilder.java");
        final JavaFileObject outputSimpleAField = JavaFileObjects
                .forResource("SimpleAField.java");
        final JavaFileObject outputSimpleCreator = JavaFileObjects
                .forResource("SimpleCreator.java");


        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Collections.singletonList(simple))
                .processedWith(new FluderProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(outputSimpleBuilder, outputSimpleAField, outputSimpleCreator)
                .withNoteContaining("'doNotBuildTransientFields' is 'transient', FluderProcessor has ignored it")
                .and()
                .withNoteContaining("'finalFieldShouldBeIgnored' is 'final', FluderProcessor has ignored it")
                .and()
                .withNoteContaining("'notBuildableBecauseIgnored' is annotated @Ignore, FluderProcessor has ignored it")
                .and()
                .withWarningContaining("@Group not already supported")
        ;
    }

    @Test
    public void testBuildablePrivateNoArgCtor() {

        final JavaFileObject privateNoArgCtor = JavaFileObjects
                .forResource("PrivateNoArgCtor.java");

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Collections.singletonList(privateNoArgCtor))
                .processedWith(new FluderProcessor())
                .compilesWithoutError()
                .withWarningContaining("constructor is private");
    }

    @Test
    public void testBuildableWithoutANoArgCtor() {

        final JavaFileObject withoutNoArgCtor = JavaFileObjects
                .forResource("WithoutNoArgCtor.java");

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Collections.singletonList(withoutNoArgCtor))
                .processedWith(new FluderProcessor())
                .failsToCompile()
                .withErrorContaining("needs a no arg constructor in order to FluderProcessor be able to generate a fluent builder");
    }

    @Test
    public void testBuildablePartialOrder() {

        final JavaFileObject partialOrder = JavaFileObjects
                .forResource("PartialOrder.java");

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Collections.singletonList(partialOrder))
                .processedWith(new FluderProcessor())
                .failsToCompile()
                .withErrorContaining("You must use @Order on each: non transient / @Ignore / non @Optional fields");
    }


}
