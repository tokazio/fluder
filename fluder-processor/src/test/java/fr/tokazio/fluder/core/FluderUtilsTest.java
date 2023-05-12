package fr.tokazio.fluder.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FluderUtilsTest {

    @Test
    void testFirstUpper() {
        //given
        //when
        final String actual = FluderUtils.firstUpper("testStr");
        //then
        assertThat(actual).isEqualTo("TestStr");
    }

    @Test
    void testFirstUpperFromNull() {
        //given
        //when
        final String actual = FluderUtils.firstUpper(null);
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFirstUpperFromEmpty() {
        //given
        //when
        final String actual = FluderUtils.firstUpper("");
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFirstLower() {
        //given
        //when
        final String actual = FluderUtils.firstLower("TestStr");
        //then
        assertThat(actual).isEqualTo("testStr");
    }

    @Test
    void testFirstLowerFromNull() {
        //given
        //when
        final String actual = FluderUtils.firstLower(null);
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void testFirstLowerFromEmpty() {
        //given
        //when
        final String actual = FluderUtils.firstLower("");
        //then
        assertThat(actual).isEmpty();
    }
}
