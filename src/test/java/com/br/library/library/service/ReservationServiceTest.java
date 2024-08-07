package com.br.library.library.service;

import com.br.library.library.domain.Book;
import com.br.library.library.domain.Reservation;
import com.br.library.library.domain.Usuario;
import com.br.library.library.dtos.reservation.ReservationDto;
import com.br.library.library.dtos.showQueryPersonalized.ShowReservationAndBookDTO;
import com.br.library.library.dtos.usuario.AuthenticationDtoPost;
import com.br.library.library.enums.StatusToReserve;
import com.br.library.library.exception.BadRequestException;
import com.br.library.library.methodsToCheckThings.CheckThingsIFIsCorrect;
import com.br.library.library.repository.ReservationRepository;
import com.br.library.library.repository.UsuarioRepository;
import com.br.library.library.util.BookCreate;
import com.br.library.library.util.ReservationCreate;
import com.br.library.library.util.UsuarioCreate;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BookService bookService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CheckThingsIFIsCorrect checkThingsIFIsCorrect;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        BDDMockito.when(reservationRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(ReservationCreate.createReservationValid()));

        BDDMockito.when(reservationRepository.findBookMostReserved())
                .thenReturn(List.of(BookCreate.createBookValid()));

        BDDMockito.when(reservationRepository.findReservationByUsuario(ArgumentMatchers.anyLong()))
                .thenReturn(List.of(ReservationCreate.createShowReservationAndBookDTO()));

        BDDMockito.doNothing().when(checkThingsIFIsCorrect)
                .checkPasswordIsOk(ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        BDDMockito.when(reservationRepository.save(ArgumentMatchers.any(Reservation.class)))
                .thenReturn(ReservationCreate.createReservationValid());

        BDDMockito.when(usuarioRepository.findByLogin(ArgumentMatchers.anyString()))
                .thenReturn(UsuarioCreate.createUsuario());

        BDDMockito.when(usuarioRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(UsuarioCreate.createUsuario()));


        BDDMockito.when(bookService.findByTitle(ArgumentMatchers.anyString()))
                .thenReturn(BookCreate.createBookValid());

        BDDMockito.when(bookService.findByTitleAndGenreAndAuthor
                (ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(BookCreate.createBookValid());

        BDDMockito.when(usuarioRepository.findByLoginAndEmail(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(UsuarioCreate.createUsuario()));

        BDDMockito.when(reservationRepository
                .findByBookAndUsuario(ArgumentMatchers.any(Book.class), ArgumentMatchers.any(Usuario.class)))
                .thenReturn(Optional.of(ReservationCreate.createReservationValid()));
    }



    @Test
    @DisplayName("Should find reservation by ID when is successful")
    void findById() {
        Reservation reservationByID = reservationService.findById(1L);
        Reservation expectedReservation = ReservationCreate.createReservationValid();

        assertThat(reservationByID).isNotNull();

        assertThat(reservationByID.getBook().getTitle()).isEqualTo(expectedReservation.getBook().getTitle());
        assertThat(reservationByID.getBook().getAuthor()).isEqualTo(expectedReservation.getBook().getAuthor());
        assertThat(reservationByID.getBook().getGenre()).isEqualTo(expectedReservation.getBook().getGenre());

        assertThat(reservationByID.getUsuario().getLogin()).isEqualTo(expectedReservation.getUsuario().getLogin());
        assertThat(reservationByID.getUsuario().getPassword()).isEqualTo(expectedReservation.getUsuario().getPassword());
        assertThat(reservationByID.getUsuario().getEmail()).isEqualTo(expectedReservation.getUsuario().getEmail());

        assertThat(reservationByID.getReservationDate()).isEqualTo(expectedReservation.getReservationDate());
        assertThat(reservationByID.getReturnDate()).isEqualTo(expectedReservation.getReturnDate());

    }


    @Test
    @DisplayName("Should find reservation most reserved when is successful")
    void findBooksMostReserved() {
        List<Book> booksMostReserved = reservationService.findBooksMostReserved();
        Book expectedBook = BookCreate.createBookValid();

        assertThat(booksMostReserved).isNotNull();
        assertThat(booksMostReserved.size()).isEqualTo(1);

        assertThat(booksMostReserved.get(0).getTitle()).isEqualTo(expectedBook.getTitle());
        assertThat(booksMostReserved.get(0).getAuthor()).isEqualTo(expectedBook.getAuthor());
        assertThat(booksMostReserved.get(0).getGenre()).isEqualTo(expectedBook.getGenre());
        assertThat(booksMostReserved.get(0).getDatePublished()).isEqualTo(expectedBook.getDatePublished());
        assertThat(booksMostReserved.get(0).getStatusToReserve()).isEqualTo(expectedBook.getStatusToReserve());

    }

    @Test
    @DisplayName("Should find reservation by Usuario  when is successful")
    void findReservationByUsuario() {
        List<ShowReservationAndBookDTO> reservationByUsuario = reservationService
                .findReservationByUsuario(ReservationCreate.createAuthenticationDto());

        ShowReservationAndBookDTO expectedShowReservation = ReservationCreate.createShowReservationAndBookDTO();

        assertThat(reservationByUsuario).isNotNull();
        assertThat(reservationByUsuario.size()).isEqualTo(1);

        assertThat(reservationByUsuario.get(0).getTitle()).isEqualTo(expectedShowReservation.getTitle());
        assertThat(reservationByUsuario.get(0).getAuthor()).isEqualTo(expectedShowReservation.getAuthor());
        assertThat(reservationByUsuario.get(0).getGenre()).isEqualTo(expectedShowReservation.getGenre());
        assertThat(reservationByUsuario.get(0).getDatePublished()).isEqualTo(expectedShowReservation.getDatePublished());
        assertThat(reservationByUsuario.get(0).getStatusToReserve()).isEqualTo(expectedShowReservation.getStatusToReserve());
        assertThat(reservationByUsuario.get(0).getReservationDate()).isEqualTo(expectedShowReservation.getReservationDate());
        assertThat(reservationByUsuario.get(0).getReturnDate()).isEqualTo(expectedShowReservation.getReturnDate());

    }
    @Test
    @DisplayName("Should throw EntityNotFoundException reservation by Usuario not found when is successful")
    void findReservationByUsuarioWhenUserNotFound() {

            AuthenticationDtoPost authenticationDtoPost = new AuthenticationDtoPost("username", "password");

            BDDMockito.when(usuarioRepository.findByLogin(authenticationDtoPost.login())).thenReturn(null);


            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> reservationService.findReservationByUsuario(authenticationDtoPost)
            );

            assertEquals("Usuario not found", exception.getMessage());

    }

    @Test
    @DisplayName("Should make reservation when is successful")
    void makeReservation() {
        Reservation reservation = reservationService.makeReservation(ReservationCreate.createReservationDtoValid());
        Reservation expectedReservation = ReservationCreate.createReservationValid();

        assertThat(reservation).isNotNull();
        assertThat(reservation.getBook().getTitle()).isEqualTo(expectedReservation.getBook().getTitle());
        assertThat(reservation.getBook().getAuthor()).isEqualTo(expectedReservation.getBook().getAuthor());
        assertThat(reservation.getBook().getGenre()).isEqualTo(expectedReservation.getBook().getGenre());
        assertThat(reservation.getBook().getStatusToReserve()).isEqualTo(expectedReservation.getBook().getStatusToReserve());
        assertThat(reservation.getBook().getDatePublished()).isEqualTo(expectedReservation.getBook().getDatePublished());

        assertThat(reservation.getUsuario().getLogin()).isEqualTo(expectedReservation.getUsuario().getLogin());
        assertThat(reservation.getUsuario().getPassword()).isEqualTo(expectedReservation.getUsuario().getPassword());
        assertThat(reservation.getUsuario().getEmail()).isEqualTo(expectedReservation.getUsuario().getEmail());

        assertThat(reservation.getReservationDate()).isEqualTo(expectedReservation.getReservationDate());
        assertThat(reservation.getReturnDate()).isEqualTo(expectedReservation.getReturnDate());

    }
    @Test
    @DisplayName("Should throw IllegalArgumentException when fields of User are incorrect is successful")
    void makeReservationWhenIllegalArgumentException() {
        ReservationDto reservationDtoValid = ReservationCreate.createReservationDtoValid();

        BDDMockito.when(usuarioRepository.findByLogin(reservationDtoValid.getLogin()))
                .thenReturn(UsuarioCreate.createUsuario());

        BDDMockito.when(usuarioRepository.findByEmail(reservationDtoValid.getEmail()))
                .thenReturn(Optional.of(UsuarioCreate.createUsuario2()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.makeReservation(reservationDtoValid));

        assertEquals("Fields of the User aren't the corrects , check it", exception.getMessage());

    }

    @Test
    @DisplayName("Should throw BadRequestException when book not available is successful")
    void makeReservationWhenBadRequestException() {
        ReservationDto reservationDtoValid = ReservationCreate.createReservationDtoValid();

        Book canceledBook = bookService.findByTitle(reservationDtoValid.getTitle());
        canceledBook.setStatusToReserve(StatusToReserve.CANCELED);

        BDDMockito.when(bookService.findByTitle(reservationDtoValid.getTitle()))
                .thenReturn(canceledBook);


        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> reservationService.makeReservation(reservationDtoValid)
        );

        assertEquals("Book is not available ", exception.getMessage());


    }

    @Test
    @DisplayName("Should return book of reservation when is successful")
    void returnBook() {
        BDDMockito.when(bookService.findByTitleAndGenreAndAuthor
                (ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                        .thenReturn(BookCreate.createBook());


        assertThatCode(() ->  reservationService.returnBook(ReservationCreate.createReservationDtoValid()))
                .doesNotThrowAnyException();

    }

    @Test
    @DisplayName("Should throw BadRequestException when book not reserved is successful")
    void returnBookWhenTheBookIsNotReserved() {
        ReservationDto reservationDtoValid = ReservationCreate.createReservationDtoValid();
        Book book = BookCreate.createBook();
        book.setStatusToReserve(StatusToReserve.AVAILABLE);

        BDDMockito.when(bookService.findByTitleAndGenreAndAuthor
                (ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                        .thenReturn(book);


        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> reservationService.returnBook(reservationDtoValid)
        );

        assertEquals("Do you reserved the book ? Check it , maybe you returned it", exception.getMessage());

    }


}