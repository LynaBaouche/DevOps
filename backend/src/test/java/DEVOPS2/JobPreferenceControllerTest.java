package DEVOPS2;

import com.etudlife.controller.JobPreferenceController;
import com.etudlife.model.Compte;
import com.etudlife.model.JobPreference;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.JobPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPreferenceControllerTest {

    @Mock
    private JobPreferenceRepository preferenceRepo;

    @Mock
    private CompteRepository compteRepository;

    @InjectMocks
    private JobPreferenceController controller;

    @Test
    void testSavePreferences_CreateNew() {
        Compte compte = new Compte();
        compte.setId(1L);

        JobPreference newPref = new JobPreference();
        newPref.setMotsCles("Java");

        when(compteRepository.findAll()).thenReturn(List.of(compte));
        when(preferenceRepo.findByCompte(compte)).thenReturn(null);

        ResponseEntity<?> response = controller.savePreferences(newPref);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(preferenceRepo, times(1)).save(newPref); // Vérifie que save() a été appelé une fois
    }

    @Test
    void testSavePreferences_UpdateExisting() {
        Compte compte = new Compte();
        compte.setId(1L);

        JobPreference existingPref = new JobPreference();
        existingPref.setId(50L); // ID existant

        JobPreference incomingPref = new JobPreference();
        incomingPref.setMotsCles("Python");

        when(compteRepository.findAll()).thenReturn(List.of(compte));
        when(preferenceRepo.findByCompte(compte)).thenReturn(existingPref); // On trouve une pref existante !

        controller.savePreferences(incomingPref);

       //On vérifie que l'ID de la nouvelle pref a été remplacé par l'ancien (50L)
        assertEquals(50L, incomingPref.getId());
        verify(preferenceRepo).save(incomingPref);
    }
}