package DEVOPS2;

import com.etudlife.dto.SavedJobRequestDTO;
import com.etudlife.model.Compte;
import com.etudlife.model.JobStatus;
import com.etudlife.model.SavedJob;
import com.etudlife.repository.CompteRepository;
import com.etudlife.repository.SavedJobRepository;
import com.etudlife.service.SavedJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        mockCompte = new Compte();
        mockCompte.setId(1L);
        mockCompte.setEmail("test@etudiant.univ-nanterre.fr");

        // On configure le mock pour que chaque test trouve le compte
        lenient().when(compteRepository.findById(1L)).thenReturn(Optional.of(mockCompte));
    }

    @Test
    void testSaveOrUpdate_ShouldTruncateLongLinks() {
        // GIVEN
        SavedJobRequestDTO dto = new SavedJobRequestDTO();
        dto.setCompteId(1L); // ✅ Obligatoire maintenant
        dto.setExternalJobId("job123");
        dto.setApplyLink("https://very-long-link..." + "a".repeat(300));
        dto.setStatus(JobStatus.INTERESSE);

        when(savedJobRepository.findByCompteAndExternalJobId(any(), any())).thenReturn(Optional.empty());

        // WHEN
        savedJobService.saveOrUpdate(dto);

        // THEN
        verify(savedJobRepository).save(argThat(job ->
                job.getApplyLink().length() <= 250
        ));
    }

    @Test
    void testGetJobsByStatus_WithSpecificStatus_ShouldFilterCorrectly() {
        // GIVEN
        SavedJob job = new SavedJob();
        job.setStatus(JobStatus.INTERESSE);
        job.setCompte(mockCompte);

        when(savedJobRepository.findByCompteAndStatus(mockCompte, JobStatus.INTERESSE))
                .thenReturn(List.of(job));

        // WHEN
        List<SavedJob> results = savedJobService.getJobsByStatus(JobStatus.INTERESSE, 1L); // ✅ Ajout ID

        // THEN
        assertEquals(1, results.size());
        assertEquals(JobStatus.INTERESSE, results.get(0).getStatus());
    }

    @Test
    void testSaveOrUpdate_WhenJobAlreadyExists_ShouldUpdateExistingRecord() {
        // GIVEN
        SavedJob existingJob = new SavedJob();
        existingJob.setExternalJobId("job123");
        existingJob.setStatus(JobStatus.INTERESSE);
        existingJob.setCompte(mockCompte);

        SavedJobRequestDTO dto = new SavedJobRequestDTO();
        dto.setCompteId(1L); // ✅ Obligatoire
        dto.setExternalJobId("job123");
        dto.setStatus(JobStatus.POSTULE);

        when(savedJobRepository.findByCompteAndExternalJobId(mockCompte, "job123"))
                .thenReturn(Optional.of(existingJob));

        // WHEN
        savedJobService.saveOrUpdate(dto);

        // THEN
        verify(savedJobRepository).save(argThat(job ->
                job.getStatus() == JobStatus.POSTULE
        ));
    }
}