package fr.tokazio.fluder.demo;

import com.example.Email;
import com.example.EmailBuilder;
import com.example.EmailCreator;
import org.junit.Test;

public class FluderTest {

    @Test
    public void testEmail() {
        //given
        EmailCreator builder = EmailBuilder.getInstance()
                .setFrom("from")
                .setTo("to")
                .setSubject("subject")
                .setContent("content")
                .setBcc("bcc")
                .setCc("cc");
        //when
        Email actual = builder.build();

        //then
    }
}
