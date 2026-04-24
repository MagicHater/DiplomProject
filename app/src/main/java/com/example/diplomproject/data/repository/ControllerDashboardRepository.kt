package com.example.diplomproject.data.repository

import com.example.diplomproject.data.remote.AppApi
import com.example.diplomproject.data.remote.ControllerDashboardResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControllerDashboardRepository @Inject constructor(
    private val api: AppApi
) {
    suspend fun getDashboard(): ControllerDashboardResponseDto = api.getControllerDashboard()
}
