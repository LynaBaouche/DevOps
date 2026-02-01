package DEVOPS1;

import com.etudlife.EtudlifeApp;
import com.etudlife.model.Document;
import com.etudlife.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(
        classes = EtudlifeApp.class,
        properties = {
                "RAPIDAPI_KEY=test_key_dummy",
                "OPENAI_API_KEY=test_key_dummy",
                "NAVITIA_TOKEN=test_key_dummy"
        }
)
@AutoConfigureMockMvc
public class DocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
    }

    @Test
    public void testAffichageListeDocuments() throws Exception {
        // GIVEN : On ajoute un document en base
        Document doc = new Document();
        doc.setNom("Cours_DevOps.pdf");
        doc.setType("application/pdf");
        documentRepository.save(doc);

        // WHEN & THEN : On vérifie que la liste JSON contient le document
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nom", is("Cours_DevOps.pdf")));
    }

    @Test
    public void testUploadDocument() throws Exception {
        // GIVEN : Un fichier simulé
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu binaire du fichier".getBytes()
        );

        // WHEN & THEN : Envoi au contrôleur avec les paramètres optionnels
        mockMvc.perform(multipart("/api/documents/upload")
                        .file(file)
                        .param("uploaderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom", is("test.txt")));
    }

    @Test
    public void testTelechargementDocument() throws Exception {
        // GIVEN : Un document avec des données binaires (Blob) déjà en base
        Document doc = new Document();
        doc.setNom("image.png");
        doc.setType("image/png");
        doc.setDonnees("FakeImageBytes".getBytes());
        doc = documentRepository.save(doc);

        // WHEN & THEN : On appelle l'URL de téléchargement par ID
        mockMvc.perform(get("/api/documents/" + doc.getId() + "/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().bytes("FakeImageBytes".getBytes()));
    }
}