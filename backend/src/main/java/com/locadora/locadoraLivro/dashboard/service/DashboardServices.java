package com.locadora.locadoraLivro.dashboard.service;

import com.locadora.locadoraLivro.Books.repositories.BookRepository;
import com.locadora.locadoraLivro.Publishers.repositories.PublisherRepository;
import com.locadora.locadoraLivro.Renters.models.RenterModel;
import com.locadora.locadoraLivro.Renters.repositories.RenterRepository;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.models.RentStatusEnum;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import com.locadora.locadoraLivro.dashboard.DTOs.BooksMoreRented;
import com.locadora.locadoraLivro.dashboard.DTOs.DashboardOverviewDTO;
import com.locadora.locadoraLivro.dashboard.DTOs.DashboardStatusItemDTO;
import com.locadora.locadoraLivro.dashboard.DTOs.MonthlyRentPointDTO;
import com.locadora.locadoraLivro.dashboard.DTOs.RentsperRenterResponseDTO;
import com.locadora.locadoraLivro.dashboard.mappers.BookRentMapper;
import com.locadora.locadoraLivro.Users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServices {

    @Autowired
    RentRepository rentRepository;

    @Autowired
    RenterRepository renterRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PublisherRepository publisherRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private BookRentMapper bookRentMapper;

    public int getNumberOfRentals(int numberOfMonths){
        List<RentModel> totalRents = rentRepository.findAll();

        int rentsQuantity = (int) totalRents.stream()
                .filter(rent -> !rent.getRentDate().isBefore(LocalDate.now().minusMonths(numberOfMonths))
                        && !rent.getRentDate().isAfter(LocalDate.now())).count();

        return rentsQuantity;
    }

    public int getNumberOfRentalsLate(int numberOfMonths){
        List<RentModel> totalRentsLate = rentRepository.findAllByStatus(RentStatusEnum.LATE);
        int rentsLate = (int) totalRentsLate.stream()
                .filter(rent -> !rent.getRentDate().isBefore(LocalDate.now().minusMonths(numberOfMonths))
                        && !rent.getRentDate().isAfter(LocalDate.now())).count();

        return rentsLate;
    }

    public int getDeliveredInTime(int numberOfMonths){
        List<RentModel> totalRentsInTime = rentRepository.findAllByStatus(RentStatusEnum.IN_TIME);
        int rentsInTime = (int) totalRentsInTime.stream()
                .filter(rent -> !rent.getRentDate().isBefore(LocalDate.now().minusMonths(numberOfMonths))
                        && !rent.getRentDate().isAfter(LocalDate.now())).count();

        return rentsInTime;
    }

    public int getDeliveredWithDelay(int numberOfMonths){
        List<RentModel> totalRentsDeliveredLate = rentRepository.findAllByStatus(RentStatusEnum.DELIVERED_WITH_DELAY);
        int rentsWithDelay = (int) totalRentsDeliveredLate.stream()
                .filter(rent -> !rent.getRentDate().isBefore(LocalDate.now().minusMonths(numberOfMonths))
                        && !rent.getRentDate().isAfter(LocalDate.now())).count();

        return rentsWithDelay;
    }

    public Page<RentsperRenterResponseDTO> getRentsPerRenter(int page) {
        int size = 8;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        List<RenterModel> renters = renterRepository.findAll();
        List<RentsperRenterResponseDTO> renterRentList = new ArrayList<>();

        for (RenterModel renter : renters) {
            List<RentModel> rents = rentRepository.findAllByRenterId(renter.getId());
            List<RentModel> rentsActive = rents.stream()
                    .filter(rent -> rent.getStatus() == RentStatusEnum.RENTED || rent.getStatus() == RentStatusEnum.LATE)
                    .toList();
            renterRentList.add(new RentsperRenterResponseDTO(renter.getName(), rents.size(), rentsActive.size()));
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), renterRentList.size());

        Page<RentsperRenterResponseDTO> pageResult = new PageImpl<>(
                renterRentList.subList(start, end), pageable, renterRentList.size()
        );

        return pageResult;
    }

    public List<BooksMoreRented> getBooksMoreRented(int numberOfMonths) {
        return bookRentMapper.toBooksMoreRentedList(bookRepository.findAll(), numberOfMonths);
    }

    public List<RentsperRenterResponseDTO> getTop3RentsPerRenter() {
        return getTopRentersByPeriod(24).stream().limit(3).toList();
    }

        private List<RentsperRenterResponseDTO> getTopRentersByPeriod(int numberOfMonths) {
        int safeMonths = Math.max(1, Math.min(numberOfMonths, 24));
        LocalDate startDate = LocalDate.now().minusMonths(safeMonths - 1).withDayOfMonth(1);
        LocalDate now = LocalDate.now();

        List<RentModel> rentsInRange = rentRepository.findAll().stream()
            .filter(rent -> rent.getRentDate() != null)
            .filter(rent -> !rent.getRentDate().isBefore(startDate) && !rent.getRentDate().isAfter(now))
            .toList();

        List<RenterModel> renters = renterRepository.findAll().stream()
            .filter(renter -> !renter.isDeleted())
            .toList();

        List<RentsperRenterResponseDTO> renterRentList = new ArrayList<>();
        for (RenterModel renter : renters) {
            long rentsQuantity = rentsInRange.stream()
                .filter(rent -> rent.getRenter() != null && rent.getRenter().getId() == renter.getId())
                .count();

            long rentsActive = rentsInRange.stream()
                .filter(rent -> rent.getRenter() != null && rent.getRenter().getId() == renter.getId())
                .filter(rent -> rent.getStatus() == RentStatusEnum.RENTED || rent.getStatus() == RentStatusEnum.LATE)
                .count();

            if (rentsQuantity > 0) {
            renterRentList.add(new RentsperRenterResponseDTO(
                renter.getName(),
                (int) rentsQuantity,
                (int) rentsActive
            ));
            }
        }

        return renterRentList.stream()
            .sorted((a, b) -> Integer.compare(b.getTotalRents(), a.getTotalRents()))
            .toList();
        }

        public DashboardOverviewDTO getOverview(int numberOfMonths) {
        int safeMonths = Math.max(1, Math.min(numberOfMonths, 24));
        LocalDate startDate = LocalDate.now().minusMonths(safeMonths - 1).withDayOfMonth(1);
        LocalDate now = LocalDate.now();

        long totalBooks = bookRepository.countActiveBooks();
        long totalRenters = renterRepository.countActiveRenters();
        long totalPublishers = publisherRepository.countActivePublishers();
        long totalUsers = userRepository.countTotalUsers();

        List<RentModel> rentsInRange = rentRepository.findAll().stream()
            .filter(rent -> rent.getRentDate() != null)
            .filter(rent -> !rent.getRentDate().isBefore(startDate) && !rent.getRentDate().isAfter(now))
            .toList();

        long activeRents = rentsInRange.stream()
            .filter(rent -> rent.getStatus() == RentStatusEnum.RENTED)
            .count();

        long lateRents = rentsInRange.stream()
            .filter(rent -> rent.getStatus() == RentStatusEnum.LATE)
            .count();

        long finishedRents = rentsInRange.stream()
            .filter(rent -> rent.getStatus() == RentStatusEnum.IN_TIME
                || rent.getStatus() == RentStatusEnum.DELIVERED_WITH_DELAY
                || rent.getStatus() == RentStatusEnum.DELIVERED)
            .count();

        long totalDeliveries = rentsInRange.stream()
            .filter(rent -> rent.getStatus() == RentStatusEnum.IN_TIME
                || rent.getStatus() == RentStatusEnum.DELIVERED_WITH_DELAY)
            .count();

        long inTimeDeliveries = rentsInRange.stream()
            .filter(rent -> rent.getStatus() == RentStatusEnum.IN_TIME)
            .count();

        double onTimeRate = totalDeliveries > 0
            ? (inTimeDeliveries * 100.0) / totalDeliveries
            : 0.0;

        long totalOpenRents = activeRents + lateRents;
        double lateRate = totalOpenRents > 0
            ? (lateRents * 100.0) / totalOpenRents
            : 0.0;

        List<DashboardStatusItemDTO> statusBreakdown = List.of(
            new DashboardStatusItemDTO("Alugados", activeRents),
            new DashboardStatusItemDTO("Atrasados", lateRents),
            new DashboardStatusItemDTO("Finalizados", finishedRents)
        );

        var groupedByMonth = rentsInRange.stream()
            .collect(Collectors.groupingBy(
                rent -> YearMonth.from(rent.getRentDate()),
                Collectors.counting()
            ));

        LinkedHashMap<YearMonth, Long> orderedMonths = new LinkedHashMap<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = safeMonths - 1; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            orderedMonths.put(month, groupedByMonth.getOrDefault(month, 0L));
        }

        List<MonthlyRentPointDTO> monthlyRents = orderedMonths.entrySet().stream()
            .map(entry -> new MonthlyRentPointDTO(entry.getKey().toString(), entry.getValue()))
            .toList();

        List<BooksMoreRented> topBooks = bookRentMapper.toBooksMoreRentedList(bookRepository.findAll(), safeMonths);
        List<RentsperRenterResponseDTO> topRenters = getTopRentersByPeriod(safeMonths).stream().limit(3).toList();

        return new DashboardOverviewDTO(
            totalBooks,
            totalRenters,
            totalPublishers,
            totalUsers,
            activeRents,
            lateRents,
            finishedRents,
            onTimeRate,
            lateRate,
            statusBreakdown,
            monthlyRents,
            topBooks,
            topRenters
        );
        }

}