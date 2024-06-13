package com.br.library.library.service;

import com.br.library.library.domain.Book;
import com.br.library.library.domain.Reservation;
import com.br.library.library.domain.Usuario;
import com.br.library.library.dtos.AuthenticationDtoPost;
import com.br.library.library.dtos.ReservationDtoPost;
import com.br.library.library.exception.BadRequestException;
import com.br.library.library.repository.ReservationRepository;
import com.br.library.library.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.br.library.library.enums.ReservationStatus.AVAILABLE;
import static com.br.library.library.enums.ReservationStatus.RESERVED;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final BookService bookService;
    private final UsuarioRepository usuarioRepository;


    public Reservation findById(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reservation not found"));
    }

    public List<Reservation> findByUsuario(AuthenticationDtoPost authentication) {
        Usuario usuario = usuarioRepository.findByLogin(authentication.login());

        if(Objects.isNull(usuario)) {
            throw new EntityNotFoundException("Usuario not found");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean checkingPassword = encoder.matches(authentication.password(), usuario.getPassword());

        if(!checkingPassword) {
            throw new IllegalArgumentException("The password is incorrect, check it");
        }


        return reservationRepository.findByUsuario(usuario);
    }

    @Transactional
    public Reservation doReserve(ReservationDtoPost reservationPost) {
        Usuario usuario1 = usuarioRepository.findByEmail(reservationPost.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Usuario not found, check the fields"));

        Usuario usuario2 = usuarioRepository.findByLogin(usuario1.getUsername()
                .describeConstable().orElseThrow(() -> new EntityNotFoundException("Usuario not found, check the fields")));

        if(!Objects.equals(usuario1, usuario2)){
            throw new IllegalArgumentException("Fields of the User aren't the corrects , check it");
        }


        Book book = bookService.findByTitle(reservationPost.getTitle());

        if(book.isAvailable()) {
            Reservation reservation = new Reservation(usuario1, book);
            reservation.getBook().setAvailable(false);
            reservation.setCurrentStatusReservation(RESERVED);
            reservation.setReservationDate(LocalDate.now());
            return reservationRepository.save(reservation);
        }
        throw new BadRequestException("Book is not available");

    }

    @Transactional
    public void removeReserve(ReservationDtoPost reservationPost) {

        Book bookByTitle = bookService.findByTitle(reservationPost.getTitle());
        Usuario userByLogin = usuarioRepository.findByLogin(reservationPost.getLogin());

        Reservation reservation = reservationRepository.findByBookAndUsuario(bookByTitle, userByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found"));


        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean checkingPassword = encoder.matches(reservationPost.getPassword(), userByLogin.getPassword());

        if(Objects.equals(reservation.getUsuario().getUsername(),reservationPost.getLogin()) && checkingPassword){
            bookByTitle.setAvailable(true);
            reservation.setCurrentStatusReservation(AVAILABLE);
            reservation.setReturnDate(LocalDate.now());


        } else
            throw new BadRequestException("Are you sure you have reserved the book ? Check the user typed");

    }



}
