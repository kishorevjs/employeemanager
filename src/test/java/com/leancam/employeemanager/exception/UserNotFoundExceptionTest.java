package com.leancam.employeemanager.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotFoundExceptionTest {

    @Test
    void shouldSetMessageCorrectly() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        assertThat(ex.getMessage()).isEqualTo("User not found");
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        UserNotFoundException ex = new UserNotFoundException("error");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldHaveResponseStatusNotFound() {
        ResponseStatus annotation = UserNotFoundException.class.getAnnotation(ResponseStatus.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}