package com.compass.bank.account;

import com.compass.bank.account.dto.AccountResponse;
import com.compass.bank.account.dto.CreateAccountRequest;
import com.compass.bank.account.dto.UpdateAccountRequest;
import com.compass.bank.common.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Contas", description = "Gestao de contas (CRUD)")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Cria uma nova conta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        AccountResponse created = accountService.create(request);
        URI location = uriBuilder.path("/api/v1/accounts/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma conta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lista contas (paginado)")
    public ResponseEntity<Page<AccountResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(accountService.list(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados cadastrais de uma conta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AccountResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma conta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta removida"),
            @ApiResponse(responseCode = "404", description = "Conta nao encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
