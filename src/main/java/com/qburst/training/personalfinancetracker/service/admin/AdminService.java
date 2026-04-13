package com.qburst.training.personalfinancetracker.service.admin;

import com.qburst.training.personalfinancetracker.dto.AdminDashboardDto;
import com.qburst.training.personalfinancetracker.dto.BankAccountDto;
import com.qburst.training.personalfinancetracker.dto.TransactionDto;
import com.qburst.training.personalfinancetracker.dto.UserDto;

import java.util.List;

public interface AdminService {
    AdminDashboardDto.Response getDashboard();
    List<UserDto.Response> getAllUsers();
    UserDto.Response createUser(UserDto.Request request);
    UserDto.Response updateUser(Long id, UserDto.AdminUpdateRequest request);
    List<BankAccountDto.Response> getAllAccounts();
    BankAccountDto.Response updateAccount(Long id, BankAccountDto.AdminUpdateRequest request);
    List<TransactionDto.Response> getRecentActivities();
}
