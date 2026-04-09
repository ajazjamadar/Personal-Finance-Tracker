package com.qburst.training.personalfinancetracker.controller;

import com.qburst.training.personalfinancetracker.dto.TransferDto;
import com.qburst.training.personalfinancetracker.service.transfer.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Fund transfers to accounts, mobile numbers, or UPI IDs")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Create a transfer")
    public ResponseEntity<TransferDto.Response> transfer(
            @Valid @RequestBody TransferDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transferService.transfer(request));
    }
}