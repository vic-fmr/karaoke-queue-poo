// Localização: com.karaoke.backend.exception.RestExceptionHandler.java (Refatorado)

package com.karaoke.backend.exception;

import com.karaoke.backend.dtos.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    // --- 1. TRATAMENTO PARA 404 NOT FOUND ---
    @ExceptionHandler({SessionNotFoundException.class, VideoNotFoundException.class})
    public ResponseEntity<ErrorDetails> handleNotFoundException(RuntimeException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;


        ErrorDetails details = new ErrorDetails(
                status.value(),
                status.getReasonPhrase(), // "Not Found"
                ex.getMessage()
        );

        return ResponseEntity.status(status).body(details);
    }

    // --- 2. TRATAMENTO PARA 400 BAD REQUEST ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleBadRequest(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorDetails details = new ErrorDetails(
                status.value(),
                status.getReasonPhrase(), // "Bad Request"
                ex.getMessage()
        );

        return ResponseEntity.status(status).body(details);
    }

    // --- 3. TRATAMENTO GENÉRICO (500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        System.err.println("Erro interno do servidor: " + ex.getMessage());

        ErrorDetails details = new ErrorDetails(
                status.value(),
                status.getReasonPhrase(), // "Internal Server Error"
                "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde."
        );

        return ResponseEntity.status(status).body(details);
    }

    // --- 4. TRATAMENTO PARA 409 CONFLICT ---
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        ErrorDetails details = new ErrorDetails(
                status.value(),
                status.getReasonPhrase(), // "Conflict"
                ex.getMessage()
        );
        return ResponseEntity.status(status).body(details);
    }
}