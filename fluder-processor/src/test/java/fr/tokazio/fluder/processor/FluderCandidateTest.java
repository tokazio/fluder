package fr.tokazio.fluder.processor;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FluderCandidateTest {

    @Test
    public void testFirstUpper() {
        //given
        //when
        final String actual = FluderCandidate.firstUpper("testStr");
        //then
        assertThat(actual).isEqualTo("TestStr");
    }

    @Test
    public void testFirstUpperFromNull() {
        //given
        //when
        final String actual = FluderCandidate.firstUpper(null);
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    public void testFirstUpperFromEmpty() {
        //given
        //when
        final String actual = FluderCandidate.firstUpper("");
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    public void testFirstLower() {
        //given
        //when
        final String actual = FluderCandidate.firstLower("TestStr");
        //then
        assertThat(actual).isEqualTo("testStr");
    }

    @Test
    public void testFirstLowerFromNull() {
        //given
        //when
        final String actual = FluderCandidate.firstLower(null);
        //then
        assertThat(actual).isEmpty();
    }

    @Test
    public void testFirstLowerFromEmpty() {
        //given
        //when
        final String actual = FluderCandidate.firstLower("");
        //then
        assertThat(actual).isEmpty();
    }
}
