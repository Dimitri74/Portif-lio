package br.com.florinda.pedidos.infra.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception ex) {

        if (ex instanceof NotFoundException) {
            return error(Response.Status.NOT_FOUND, ex.getMessage());
        }
        if (ex instanceof IllegalStateException) {
            return error(422, "Unprocessable Entity", ex.getMessage());
        }
        if (ex instanceof IllegalArgumentException) {
            return error(Response.Status.BAD_REQUEST, ex.getMessage());
        }
        if (ex instanceof ConstraintViolationException cve) {
            String detalhes = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return error(Response.Status.BAD_REQUEST, detalhes);
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR, "Erro interno.");
    }

    private Response error(Response.Status status, String mensagem) {
        return Response.status(status)
                .entity(Map.of(
                        "status",    status.getStatusCode(),
                        "erro",      status.getReasonPhrase(),
                        "mensagem",  mensagem,
                        "timestamp", OffsetDateTime.now().toString()
                ))
                .build();
    }

    private Response error(int statusCode, String reasonPhrase, String mensagem) {
        return Response.status(statusCode)
                .entity(Map.of(
                        "status", statusCode,
                        "erro", reasonPhrase,
                        "mensagem", mensagem,
                        "timestamp", OffsetDateTime.now().toString()
                ))
                .build();
    }
}
