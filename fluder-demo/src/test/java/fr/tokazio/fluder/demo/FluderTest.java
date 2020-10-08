package fr.tokazio.fluder.demo;

import com.example.Email;
import com.example.EmailBuilder;
import com.example.EmailCreator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FluderTest {

    @Test
    public void testEmailRequired() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject("subject")
                .setBody("content")
                //.setBcc("bcc")
                //.setCc("cc")
                ;
        //when
        Email actual = builder.build();

        //then
        assertThat(actual).extracting("to", "from", "subject", "content", "bcc")
                .contains("to", "from", "subject", "content", "bcc-default");
    }

    @Test
    public void testEmailWithOptionnal() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject("subject")
                .setBody("content")
                .setBcc("bcc")
                .setCc("cc");
        //when
        Email actual = builder.build();

        //then
        assertThat(actual).extracting("to", "from", "subject", "content", "bcc", "cc")
                .contains("to", "from", "subject", "content", "bcc", "cc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmailWhenToIsNull() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setTo(null)
                .setFrom("from")
                .setSubject("subject")
                .setBody("content");
        //when
        Email actual = builder.build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmailWhenSubjectIsNull() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setTo("to")
                .setFrom("from")
                .setSubject(null)
                .setBody("content");
        //when
        Email actual = builder.build();

    }

}
