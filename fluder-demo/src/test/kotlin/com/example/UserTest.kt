package com.example

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun testEmailRequired() {
        //given
        val builder = UserBuilder.getInstance()
            .setName("toto")

        //when
        val actual: User = builder.build()

        //then
        Assertions.assertThat(actual).extracting("name")
            .contains("toto")
    }

}