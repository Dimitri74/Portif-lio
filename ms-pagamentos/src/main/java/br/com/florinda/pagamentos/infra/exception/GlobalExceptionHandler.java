package br.com.florinda.pagamentos.infra.exception;

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
            return error(404, "Not Found", ex.getMessage());
        }
        // Correção: Response.status(422) explícito — UNPROCESSABLE_ENTITY não existe no JAX-RS
        if (ex instanceof IllegalStateException) {
            return error(422, "Unprocessable Entity", ex.getMessage());
        }
        if (ex instanceof IllegalArgumentException) {
            return error(400, "Bad Request", ex.getMessage());
        }
        if (ex instanceof ConstraintViolationException cve) {
            String detalhes = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return error(400, "Bad Request", detalhes);
        }
        return error(500, "Internal Server Error", "Erro interno. Tente novamente.");
    }

    private Response error(int status, String erro, String mensagem) {
        return Response.status(status)
                .entity(Map.of(
                        "status",    status,
                        "erro",      erro,
                        "mensagem",  mensagem,
                        "timestamp", OffsetDateTime.now().toString()
                ))
                .build();
    }
}
