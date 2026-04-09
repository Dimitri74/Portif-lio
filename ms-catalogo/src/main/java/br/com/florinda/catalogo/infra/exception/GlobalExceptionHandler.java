package br.com.florinda.catalogo.infra.exception;

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
            return buildError(Response.Status.NOT_FOUND, ex.getMessage());
        }

        if (ex instanceof IllegalStateException) {
            return buildError(422, "Unprocessable Entity", ex.getMessage());
        }

        if (ex instanceof IllegalArgumentException) {
            return buildError(Response.Status.BAD_REQUEST, ex.getMessage());
        }

        if (ex instanceof ConstraintViolationException cve) {
            String detalhes = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return buildError(Response.Status.BAD_REQUEST, detalhes);
        }

        return buildError(Response.Status.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente.");
    }

    private Response buildError(Response.Status status, String mensagem) {
        return Response.status(status)
                .entity(Map.of(
                        "status",    status.getStatusCode(),
                        "erro",      status.getReasonPhrase(),
                        "mensagem",  mensagem,
                        "timestamp", OffsetDateTime.now().toString()
                ))
                .build();
    }

    private Response buildError(int statusCode, String reasonPhrase, String mensagem) {
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
