package DEVOPS2;

import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.service.SavedJobService;
import com.etudlife.model.Compte;
import com.etudlife.model.JobStatus;
import com.etudlife.model.SavedJob;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.SavedJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private CompteRepository compteRepository;

    @InjectMocks
    private SavedJobService savedJobService;

    private Compte mockCompte;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockCompte = new Compte();
        mockCompte.setId(1L);
        // On simule qu'il y a toujours un compte dispo
        when(compteRepository.findAll()).thenReturn(List.of(mockCompte));
    }

    @Test
    void testSaveOrUpdate_ShouldTruncateLongLinks() {
        // Arrange
        SavedJobRequestDTO dto = new SavedJobRequestDTO();
        dto.setExternalJobId("job_123");
        dto.setStatus(JobStatus.INTERESSE);

        // On crée un lien de 300 caractères
        String veryLongLink = "a".repeat(300);
        dto.setApplyLink(veryLongLink);

        when(savedJobRepository.findByCompteAndExternalJobId(mockCompte, "job_123"))
                .thenReturn(Optional.empty());

        // Act
        savedJobService.saveOrUpdate(dto);

        // Assert
        ArgumentCaptor<SavedJob> jobCaptor = ArgumentCaptor.forClass(SavedJob.class);
        verify(savedJobRepository).save(jobCaptor.capture());

        SavedJob savedJob = jobCaptor.getValue();
        assertEquals(250, savedJob.getApplyLink().length()); // Vérifie la coupure à 250 caractères
        assertEquals(JobStatus.INTERESSE, savedJob.getStatus());
    }
}
