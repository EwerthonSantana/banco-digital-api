package com.compass.bank.transfer;

import com.compass.bank.common.exception.ApiError;
import com.compass.bank.transfer.dto.MovementResponse;
import com.compass.bank.transfer.dto.TransferRequest;
import com.compass.bank.transfer.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transferencias", description = "Transferencia de fundos e consulta de movimentacoes")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfers")
    @Operation(summary = "Realiza uma transferencia entre duas contas",
            description = "Debita a conta de origem e credita a de destino de forma atomica. "
                    + "Aceita o header opcional 'Idempotency-Key' para evitar duplicidade em retries.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transferencia concluida"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @Parameter(description = "Chave de idempotencia opcional")
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        TransferResponse response = transferService.transfer(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts/{accountId}/movements")
    @Operation(summary = "Consulta as movimentacoes financeiras de uma conta (extrato)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentacoes retornadas"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Page<MovementResponse>> getMovements(
            @PathVariable UUID accountId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transferService.getMovements(accountId, pageable));
    }
}
