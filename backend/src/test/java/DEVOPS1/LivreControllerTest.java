package DEVOPS1;

import com.etudlife.EtudlifeApp; // Ta classe principale
import com.etudlife.model.LivreBu;
import com.etudlife.repository.LivreBuRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// AJOUT DE (classes = EtudlifeApp.class) ICI :
@SpringBootTest(
        classes = EtudlifeApp.class,
        properties = {
                "RAPIDAPI_KEY=test_key_dummy",
                "OPENAI_API_KEY=test_key_dummy",
                "NAVITIA_TOKEN=test_key_dummy"
        }
)
@AutoConfigureMockMvc
public class LivreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivreBuRepository livreBuRepository;

    @Test
    public void testEndpointReservationLivre() throws Exception {
        // 1. Préparer
        LivreBu livre = new LivreBu();
        livre.setTitre("Spring Boot pour les Nuls");
        livre.setDisponible(true);

        livre = livreBuRepository.save(livre);
        String generatedId = livre.getId().toString();

        // 2. Agir (Simulation de l'appel API du Front-end)
        mockMvc.perform(post("/api/reservation")
                        .param("iduser", "1")
                        .param("livreId", generatedId)
                        .param("dateRecuperation", "2026-01-20")
                        .param("domicile", "true"))
                .andExpect(status().isOk());
    }
}