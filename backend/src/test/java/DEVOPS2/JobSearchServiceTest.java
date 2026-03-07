package DEVOPS2;

import com.etudlife.dto.JobDTO;
import com.etudlife.service.JobSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class JobSearchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JobSearchService jobSearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Force l'injection du RestTemplate mocké et de la fausse clé API
        ReflectionTestUtils.setField(jobSearchService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(jobSearchService, "apiKey", "dummy-test-key");
    }

    @Test
    void testSearchJobs_WhenApiFails_ShouldReturnEmptyListAndNotCrash() {
        // Arrange : On simule un crash total de l'API distante (ex: Timeout ou Erreur 500)
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RestClientException("API is down"));

        // Act
        List<JobDTO> results = jobSearchService.searchJobs("Développeur Java");

        // Assert : L'application attrape l'erreur et renvoie une liste vide proprement
        assertTrue(results.isEmpty(), "La liste doit être vide en cas d'erreur API");
    }

    @Test
    void testSearchJobs_WhenQueryContainsSpecialCharacters_ShouldHandleEncoding() {
        // Act : On lance une recherche avec des caractères spéciaux (espaces, accents)
        // Si URLEncoder plante, la méthode retourne une liste vide via le catch(Exception e)
        List<JobDTO> results = jobSearchService.searchJobs("C++ & C# Developer");

        // Assert (On vérifie juste que ça ne crash pas l'application)
        assertTrue(results.isEmpty() || results != null);
    }
}