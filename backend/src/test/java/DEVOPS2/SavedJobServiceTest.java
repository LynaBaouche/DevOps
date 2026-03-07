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
    @Test
    void testGetJobsByStatus_WithSpecificStatus_ShouldFilterCorrectly() {
        // Arrange
        SavedJob job1 = new SavedJob();
        job1.setStatus(JobStatus.INTERESSE);

        when(savedJobRepository.findByCompteAndStatus(mockCompte, JobStatus.INTERESSE))
                .thenReturn(List.of(job1));

        // Act
        List<SavedJob> result = savedJobService.getJobsByStatus(JobStatus.INTERESSE);

        // Assert
        assertEquals(1, result.size());
        assertEquals(JobStatus.INTERESSE, result.get(0).getStatus());
        verify(savedJobRepository).findByCompteAndStatus(mockCompte, JobStatus.INTERESSE);
    }

    @Test
    void testSaveOrUpdate_WhenJobAlreadyExists_ShouldUpdateExistingRecord() {
        // Arrange : L'utilisateur a déjà "Liké" l'offre en base de données
        SavedJob existingJob = new SavedJob();
        existingJob.setExternalJobId("job_456");
        existingJob.setStatus(JobStatus.INTERESSE);

        when(savedJobRepository.findByCompteAndExternalJobId(mockCompte, "job_456"))
                .thenReturn(Optional.of(existingJob));

        // Nouvelle action : L'utilisateur clique sur "J'ai postulé"
        SavedJobRequestDTO updateRequest = new SavedJobRequestDTO();
        updateRequest.setExternalJobId("job_456");
        updateRequest.setStatus(JobStatus.POSTULE);

        // Act
        savedJobService.saveOrUpdate(updateRequest);

        // Assert : On vérifie que le statut a bien été modifié et sauvegardé
        ArgumentCaptor<SavedJob> jobCaptor = ArgumentCaptor.forClass(SavedJob.class);
        verify(savedJobRepository).save(jobCaptor.capture());

        assertEquals(JobStatus.POSTULE, jobCaptor.getValue().getStatus());
        // L'ID externe ne doit pas avoir changé
        assertEquals("job_456", jobCaptor.getValue().getExternalJobId());
    }
}
