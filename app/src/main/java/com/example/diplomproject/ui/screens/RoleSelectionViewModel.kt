package com.example.diplomproject.ui.screens

import androidx.lifecycle.ViewModel
import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.repository.RoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RoleSelectionViewModel @Inject constructor(
    private val roleRepository: RoleRepository,
) : ViewModel() {
    val roles: List<UserRole> = roleRepository.getAvailableRoles()
}
