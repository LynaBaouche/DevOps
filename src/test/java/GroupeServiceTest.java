import com.etudlife.model.Compte;
import com.etudlife.model.Groupe;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.GroupeRepository;
import com.etudlife.service.GroupeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GroupeServiceTest {

    @Mock private GroupeRepository groupeRepository;
    @Mock private CompteRepository compteRepository;

    @InjectMocks private GroupeService groupeService;

    @Test
    void testAlgorithmeRecommandation() {
        // 1. SETUP : Un utilisateur qui aime le SPORT et la MUSIQUE
        Long userId = 1L;
        Compte user = new Compte();
        user.setId(userId);
        user.setHobbies(new HashSet<>(Arrays.asList("Sport", "Musique")));

        // 2. SETUP : Des groupes disponibles en base
        Groupe g1 = new Groupe("Club Foot", "...", "Sport");      // Doit être recommandé
        Groupe g2 = new Groupe("Chorale", "...", "Musique");      // Doit être recommandé
        Groupe g3 = new Groupe("Club Echecs", "...", "Jeux");     // NE DOIT PAS être recommandé

        // On dit aux mocks quoi répondre quand le service les appelle
        when(compteRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupeRepository.findAll()).thenReturn(Arrays.asList(g1, g2, g3));

        // 3. ACTION
        List<Groupe> resultats = groupeService.getRecommandations(userId);

        // 4. ASSERTIONS
        assertEquals(2, resultats.size());
        assertTrue(resultats.contains(g1));
        assertTrue(resultats.contains(g2));
        assertFalse(resultats.contains(g3));
    }
}