package fr.tokazio.fluder.demo;

import com.example.Email;
import com.example.EmailBuilder;
import com.example.EmailCreator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FluderTest {

    @Test
    public void testEmail() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject("subject")
                .setContent("content")
                //.setBcc("bcc")
                //.setCc("cc")
                ;
        //when
        Email actual = builder.build();

        //then
        assertThat(actual).extracting("to", "from", "subject", "content", "bcc").contains("to", "from", "subject", "content", "bcc-default");
    }
}
