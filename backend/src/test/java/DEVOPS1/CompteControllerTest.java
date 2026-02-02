package DEVOPS1;

import com.etudlife.EtudlifeApp;
import com.etudlife.controller.CompteController;
import com.etudlife.model.Compte;
import com.etudlife.repository.CompteRepository;
import com.etudlife.service.CompteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CompteController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = EtudlifeApp.class)
public class CompteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CompteService compteService;
    @MockBean private CompteRepository compteRepository; // Nécessaire car injecté dans le controller

    @Test
    public void testLoginSuccess() throws Exception {
        // GIVEN
        Compte c = new Compte();
        c.setEmail("test@fac.fr");
        given(compteService.login("test@fac.fr", "pass")).willReturn(c);

        // WHEN
        String jsonBody = "{\"email\": \"test@fac.fr\", \"password\": \"pass\"}";
        mockMvc.perform(post("/api/comptes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginFail_WrongPassword() throws Exception {
        // GIVEN : On simule l'erreur levée par le service
        given(compteService.login(anyString(), anyString()))
                .willThrow(new IllegalArgumentException("Mot de passe incorrect"));

        // WHEN
        String jsonBody = "{\"email\": \"bad@fac.fr\", \"password\": \"wrong\"}";
        mockMvc.perform(post("/api/comptes/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized()); // Vérifie le 401
    }

    @Test
    public void testInscription_DejaPris() throws Exception {
        // GIVEN
        given(compteService.creerCompte(any(Compte.class)))
                .willThrow(new IllegalStateException("Email pris"));

        // WHEN
        mockMvc.perform(post("/api/comptes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict()); // Vérifie le 409
    }
}