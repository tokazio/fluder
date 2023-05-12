package fr.tokazio.fluder.processor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FluderTest {

    @Test
    void testBuilderNameOverridingClassName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.builderName("aBuilderName", "myClassName");
        //then
        assertThat(actual).isEqualTo("ABuilderName");
    }

    @Test
    void testBuilderNameFromClassName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.builderName("", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameBuilder");
    }

    @Test
    void testCustomBuilderNameFromClassName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.builderName("$QuiConstruit", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameQuiConstruit");
    }

    @Test
    void testCreatorNameOverridingClassName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.creatorName("aCreatorName", "myClassName");
        //then
        assertThat(actual).isEqualTo("ACreatorName");
    }

    @Test
    void testCreatorName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.creatorName("", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameCreator");
    }

    @Test
    void testCustomCreatorName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.creatorName("$QuiCree", "myClassName");
        //then
        assertThat(actual).isEqualTo("MyClassNameQuiCree");
    }

    @Test
    void testInstanceMethodName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.instanceMethodName("MyInstanceMethodName");
        //then
        assertThat(actual).isEqualTo("myInstanceMethodName");
    }

    @Test
    void testEmptyInstanceMethodName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.instanceMethodName("");
        //then
        assertThat(actual).isEqualTo("getInstance");
    }

    @Test
    void testBuildMethodName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.buildMethodName("MyMethodName");
        //then
        assertThat(actual).isEqualTo("myMethodName");
    }

    @Test
    void testEmptyBuildMethodName() {
        //given
        Fluder fluder = new Fluder();
        //when
        final String actual = fluder.buildMethodName("");
        //then
        assertThat(actual).isEqualTo("build");
    }
}
