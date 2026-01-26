import com.etudlife.model.Compte;
import com.etudlife.model.Recette;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.EntreeMenuRepository;
import com.etudlife.repository.RecetteRepository;
import com.etudlife.service.MenuService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock private EntreeMenuRepository menuRepository;
    @Mock private RecetteRepository recetteRepository;
    @Mock private CompteRepository compteRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    public void testGenererMenuAutomatique() {
        // GIVEN
        Compte user = new Compte();
        user.setId(1L);
        Recette recette = new Recette();
        recette.setPrixEstime(5.0);

        when(compteRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recetteRepository.findAll()).thenReturn(List.of(recette));

        // WHEN
        menuService.genererMenuAutomatique(1L, 50.0);

        // THEN
        // Vérifie qu'on a bien sauvegardé quelque chose en base
        verify(menuRepository).saveAll(any());
        // Vérifie qu'on a supprimé l'ancien menu avant
        verify(menuRepository).deleteByUtilisateurId(1L);
    }
}