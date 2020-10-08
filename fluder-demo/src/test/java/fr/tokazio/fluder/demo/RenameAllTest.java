package fr.tokazio.fluder.demo;

import com.example.RenameAll;
import com.example.RenameAllBuildme;
import com.example.RenameAllGen;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RenameAllTest {

    @Test
    public void testEmailRequired() {
        //given
        RenameAllGen builder = RenameAllBuildme.createNew()
                .setTest("to");
        //when
        RenameAll actual = builder.go();

        //then
        assertThat(actual).extracting("test")
                .contains("to");
    }

}
