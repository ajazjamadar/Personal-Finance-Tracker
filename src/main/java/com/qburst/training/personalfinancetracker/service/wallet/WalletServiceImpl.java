package com.qburst.training.personalfinancetracker.service.wallet;

import com.qburst.training.personalfinancetracker.dto.WalletDto;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.exception.DuplicateResourceException;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import com.qburst.training.personalfinancetracker.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;

    public WalletServiceImpl(WalletRepository walletRepository,
                             UserRepository userRepository,
                             AuthContextService authContextService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
    }

    @Override
    @Transactional
    public WalletDto.Response createWallet(WalletDto.Request request) {
        Long userId = authContextService.currentUserId();
        String normalizedName = request.name().trim();

        if (walletRepository.existsByUserIdAndNameIgnoreCase(userId, normalizedName)) {
            throw new DuplicateResourceException("Wallet already exists with name: " + normalizedName);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Wallet wallet = Wallet.builder()
                .user(user)
                .name(normalizedName)
                .balance(request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance())
                .currency(resolveCurrency(request.currency()))
                .build();

        return toResponse(walletRepository.save(wallet));
    }

    @Override
    public List<WalletDto.Response> listWalletsForCurrentUser() {
        Long userId = authContextService.currentUserId();
        return walletRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WalletDto.Response getWalletById(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        authContextService.ensureCanAccessUser(wallet.getUser().getId());
        return toResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));
        authContextService.ensureCanAccessUser(wallet.getUser().getId());
        walletRepository.delete(wallet);
    }

    private WalletDto.Response toResponse(Wallet wallet) {
        return new WalletDto.Response(
                wallet.getId(),
                wallet.getUser() == null ? null : wallet.getUser().getId(),
                wallet.getName(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    private String resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return "INR";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
