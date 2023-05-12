package fr.tokazio.fluder.demo;

import com.example.RenameAll;
import com.example.RenameAllgen;
import com.example.Renamer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RenameAllTest {

    @Test
    void testEmailRequired() {
        //given
        RenameAllgen builder = Renamer.createNew()
                .setTest("to");
        //when
        RenameAll actual = builder.go();

        //then
        assertThat(actual).extracting("test")
                .contains("to");
    }

}
