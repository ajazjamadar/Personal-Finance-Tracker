package com.qburst.training.personalfinancetracker.service;

import com.qburst.training.personalfinancetracker.dto.WalletDto;
import com.qburst.training.personalfinancetracker.entity.User;
import com.qburst.training.personalfinancetracker.entity.Wallet;
import com.qburst.training.personalfinancetracker.exception.ResourceNotFoundException;
import com.qburst.training.personalfinancetracker.repository.UserRepository;
import com.qburst.training.personalfinancetracker.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
                             UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WalletDto.Response createWallet(WalletDto.Request request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.userId()));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(request.walletName());
        wallet.setBalance(request.initialBalance());

        return toResponse(walletRepository.save(wallet));
    }

    @Override
    public WalletDto.Response getWalletById(Long id) {
        return walletRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with id: " + id));
    }

    @Override
    public List<WalletDto.Response> getWalletsByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteWallet(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new ResourceNotFoundException("Wallet not found with id: " + id);
        }
        walletRepository.deleteById(id);
    }

    private WalletDto.Response toResponse(Wallet wallet) {
        return new WalletDto.Response(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getWalletName(),
                wallet.getBalance(),
                wallet.getCreatedAt());
    }
}