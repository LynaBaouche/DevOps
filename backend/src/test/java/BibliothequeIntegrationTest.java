import com.etudlife.EtudlifeApp;
import com.etudlife.model.*;
import com.etudlife.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
@SpringBootTest(classes = EtudlifeApp.class)
@AutoConfigureMockMvc
public class BibliothequeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivreBuRepository livreRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSalleRepository salleRepository;

    private Long livreId;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        salleRepository.deleteAll();
        livreRepository.deleteAll();

        // On prépare un livre "Disponible"
        LivreBu livre = new LivreBu();
        livre.setTitre("Java DevOps");
        livre.setDisponible(true);
        livre = livreRepository.save(livre);
        livreId = livre.getId();
    }

    @Test
    public void testScenarioCompletBibliotheque() throws Exception {

        // 1. TEST : Le catalogue s'affiche
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre", is("Java DevOps")));

        // 2. TEST : Réserver le livre
        mockMvc.perform(post("/api/reservation")
                        .param("iduser", "1")
                        .param("livreId", livreId.toString())
                        .param("dateRecuperation", "2026-01-20")
                        .param("domicile", "true"))
                .andExpect(status().isOk());

        // 3. TEST : Vérifier que ça s'affiche dans "Mes Réservations"
        mockMvc.perform(get("/api/reservation/utilisateur/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idLivre", is(livreId.intValue())));

        // 4. TEST : Annuler la réservation
        // On récupère l'ID de la résa qui vient d'être créée
        Long resaId = reservationRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/api/reservation/" + resaId))
                .andExpect(status().isNoContent());

        // 5. TEST : Vérifier retour au catalogue + Livre redevenu disponible
        mockMvc.perform(get("/api/livres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].disponible", is(true)));
    }

    @Test
    public void testScenarioPlaceLibre() throws Exception {

        // 1. TEST : Réserver une place
        String jsonPlace = "{\"idUser\":1, \"nomComplet\":\"Dihia\", \"zone\":\"Salle de Groupe\", \"nbPersonnes\":4}";
        mockMvc.perform(post("/api/salles/reserver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPlace))
                .andExpect(status().isOk());

        // 2. TEST : La réservation s'affiche bien (GET utilisateur)
        mockMvc.perform(get("/api/salles/utilisateur/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].zone", is("Salle de Groupe")));

        // 3. TEST : Annuler la place
        Long placeId = salleRepository.findAll().get(0).getId();
        mockMvc.perform(delete("/api/salles/" + placeId))
                .andExpect(status().isNoContent());

        // 4. TEST : Vérifier qu'elle ne s'affiche plus
        mockMvc.perform(get("/api/salles/utilisateur/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    @Autowired
    private CatalogueRepository catalogueRepository; // Ajoute ça avec les autres Autowired

    @Test
    public void testRechercheLivres() throws Exception {
        // On nettoie avant de tester
        catalogueRepository.deleteAll();

        // On crée un livre spécifique au catalogue général
        CatalogueLivre livre = new CatalogueLivre();
        livre.setTitre("Maîtriser Jenkins");
        livre.setAuteur("Dihia");
        livre.setCategorie("DevOps");
        catalogueRepository.save(livre);

        // On teste si la loupe trouve bien "Jenkins"
        mockMvc.perform(get("/api/livres/search-global")
                        .param("query", "Jenkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre", is("Maîtriser Jenkins")));
    }
}