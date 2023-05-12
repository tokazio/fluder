package fr.tokazio.fluder.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FluderJavaGeneratorTest {

    @Test
    void testBuilderNameOverridingClassName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.builderName("aBuilderName", "myClassName");
        //then
        assertThat(actual).isEqualTo("ABuilderName");
    }

    @Test
    void testBuilderNameFromClassName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.builderName("", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameBuilder");
    }

    @Test
    void testCustomBuilderNameFromClassName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.builderName("$QuiConstruit", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameQuiConstruit");
    }

    @Test
    void testCreatorNameOverridingClassName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.creatorName("aCreatorName", "myClassName");
        //then
        assertThat(actual).isEqualTo("ACreatorName");
    }

    @Test
    void testCreatorName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.creatorName("", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameCreator");
    }

    @Test
    void testCustomCreatorName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.creatorName("$QuiCree", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameQuiCree");
    }

    @Test
    void testInstanceMethodName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.instanceMethodName("MyInstanceMethodName");
        //then
        assertThat(actual).isEqualTo("myInstanceMethodName");
    }

    @Test
    void testEmptyInstanceMethodName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.instanceMethodName("");
        //then
        assertThat(actual).isEqualTo("getInstance");
    }

    @Test
    void testBuildMethodName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.buildMethodName("MyMethodName");
        //then
        assertThat(actual).isEqualTo("myMethodName");
    }

    @Test
    void testEmptyBuildMethodName() {
        //given
        FluderJavaGenerator fluderJavaGenerator = new FluderJavaGenerator();
        //when
        final String actual = fluderJavaGenerator.buildMethodName("");
        //then
        assertThat(actual).isEqualTo("build");
    }
}
