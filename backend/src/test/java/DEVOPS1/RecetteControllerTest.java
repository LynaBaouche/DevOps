package DEVOPS1;

import com.etudlife.EtudlifeApp;
import com.etudlife.controller.RecetteController;
import com.etudlife.service.RecetteService;
import com.etudlife.model.Recette;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = RecetteController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = EtudlifeApp.class)
public class RecetteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecetteService recetteService;

    // ✅ Test 1 : Le Menu de la semaine (Cas normal)
    @Test
    public void testGetMenuSemaine() throws Exception {
        Map<String, Map<String, Recette>> mockMenu = new HashMap<>();
        given(recetteService.getMenuDeLaSemaine()).willReturn(mockMenu);

        mockMvc.perform(get("/api/recettes/semaine"))
                .andExpect(status().isOk());
    }

    // ✅ Test 2 : Récupérer une recette par ID (Cas Succès 200)
    @Test
    public void testGetRecetteById_Success() throws Exception {
        // GIVEN
        Recette r = new Recette();
        r.setId(1L);
        r.setTitre("Pâtes Carbonara");
        given(recetteService.getRecetteById(1L)).willReturn(r);

        // WHEN & THEN
        mockMvc.perform(get("/api/recettes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Pâtes Carbonara"));
    }

    // ✅ Test 3 : Recette introuvable (Cas Erreur 404)
    @Test
    public void testGetRecetteById_NotFound() throws Exception {
        // GIVEN : On dit au mock de lancer une erreur si on cherche l'ID 999
        given(recetteService.getRecetteById(999L))
                .willThrow(new EntityNotFoundException("Pas trouvé"));

        // WHEN & THEN : On vérifie qu'on reçoit bien une 404
        mockMvc.perform(get("/api/recettes/999"))
                .andExpect(status().isNotFound());
    }
}