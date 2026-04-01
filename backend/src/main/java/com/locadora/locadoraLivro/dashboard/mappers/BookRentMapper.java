package com.locadora.locadoraLivro.dashboard.mappers;

import com.locadora.locadoraLivro.Books.models.BookModel;
import com.locadora.locadoraLivro.Rents.models.RentModel;
import com.locadora.locadoraLivro.Rents.repositories.RentRepository;
import com.locadora.locadoraLivro.dashboard.DTOs.BooksMoreRented;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookRentMapper {

    @Autowired
    private RentRepository rentRepository;

    public List<BooksMoreRented> toBooksMoreRentedList(List<BookModel> bookList, int numberOfMonths) {
        int safeMonths = Math.max(1, Math.min(numberOfMonths, 24));
        return bookList.stream()
            .map(book -> toBooksMoreRented(book, safeMonths))
            .filter(book -> book.totalRents() > 0)
                .sorted((b1, b2) -> Integer.compare(b2.totalRents(), b1.totalRents()))
                .limit(3)
                .collect(Collectors.toList());
    }

    private BooksMoreRented toBooksMoreRented(BookModel book, int numberOfMonths) {
        LocalDate startDate = LocalDate.now().minusMonths(numberOfMonths - 1).withDayOfMonth(1);
        LocalDate now = LocalDate.now();

        List<RentModel> rentsInLastYear = rentRepository.findAllByBookId(book.getId()).stream()
            .filter(rent -> rent.getRentDate() != null)
            .filter(rent -> !rent.getRentDate().isBefore(startDate)
                && !rent.getRentDate().isAfter(now))
                .collect(Collectors.toList());

        int rentCount = rentsInLastYear.size();
        return new BooksMoreRented(book.getName(), rentCount);
    }
}
