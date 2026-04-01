package com.locadora.locadoraLivro.dashboard.DTOs;

import java.util.List;

public record DashboardOverviewDTO(
        long totalBooks,
        long totalRenters,
        long totalPublishers,
        long totalUsers,
        long activeRents,
        long lateRents,
        long finishedRents,
        double onTimeRate,
        double lateRate,
        List<DashboardStatusItemDTO> statusBreakdown,
        List<MonthlyRentPointDTO> monthlyRents,
        List<BooksMoreRented> topBooks,
        List<RentsperRenterResponseDTO> topRenters
) {
}
