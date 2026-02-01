package DEVOPS1;

import com.etudlife.model.LivreBu;
import com.etudlife.model.Reservation;
import com.etudlife.repository.LivreBuRepository;
import com.etudlife.repository.ReservationRepository;
import com.etudlife.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceUnitTest {

    @Mock
    private LivreBuRepository livreRepo; // On simule le repository (Mock)

    @Mock
    private ReservationRepository reservationRepo;

    @InjectMocks
    private ReservationService reservationService; // On injecte les simulations dedans

    @Test
    public void testReserverLivre_Succes() {
        // ARRANGE : On prépare le comportement du Mock
        LivreBu livre = new LivreBu();
        livre.setId(10L);
        livre.setDisponible(true);

        when(livreRepo.findById(10L)).thenReturn(Optional.of(livre));

        // ACT : On appelle la méthode du service
        reservationService.reserverLivre(10L, 1L, LocalDate.now(), true);

        // ASSERT : On vérifie que la logique a bien fonctionné
        assertFalse(livre.isDisponible()); // Le livre doit être passé à false
        verify(livreRepo, times(1)).save(livre); // On vérifie que save() a été appelé
        verify(reservationRepo, times(1)).save(any(Reservation.class));
    }

    @Test
    public void testReserverLivre_Echec_DejaReserve() {
        // ARRANGE : Le livre existe mais disponible = false
        LivreBu livreIndisponible = new LivreBu();
        livreIndisponible.setId(10L);
        livreIndisponible.setDisponible(false);

        when(livreRepo.findById(10L)).thenReturn(Optional.of(livreIndisponible));

        // ACT & ASSERT : On vérifie qu'une exception est bien levée
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.reserverLivre(10L, 1L, LocalDate.now(), true);
        });

        assertEquals("Désolé, ce livre est déjà emprunté.", exception.getMessage());

        // On vérifie que save n'a JAMAIS été appelé (puisque ça a échoué)
        verify(reservationRepo, never()).save(any(Reservation.class));
    }

    @Test
    public void testReserverLivre_Echec_LivreInexistant() {
        // ARRANGE : Le repository renvoie "vide"
        when(livreRepo.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            reservationService.reserverLivre(99L, 1L, LocalDate.now(), true);
        });

        // Vérification que rien n'a été enregistré
        verify(livreRepo, never()).save(any());
        verify(reservationRepo, never()).save(any());
    }
}