package DEVOPS2;

import com.etudlife.batch.JobBatchScheduler;
import com.etudlife.model.Compte;
import com.etudlife.model.JobPreference;
import com.etudlife.repository.JobPreferenceRepository;
import com.etudlife.service.JobSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobBatchSchedulerTest {

    @Mock
    private JobPreferenceRepository preferenceRepo;
    @Mock
    private JobSearchService jobSearchService;
    @InjectMocks
    private JobBatchScheduler jobBatchScheduler;

    // Vérifie que le batch traite les préférences et appelle le service de recherche
    @Test
    void testRunNightlyBatch() {
        Compte mockCompte = new Compte();
        mockCompte.setId(1L);

        JobPreference mockPref = new JobPreference();
        mockPref.setCompte(mockCompte);
        mockPref.setMotsCles("Java");
        mockPref.setLocalisation("Paris");
        mockPref.setFrequence("QUOTIDIEN");

        when(preferenceRepo.findAll()).thenReturn(List.of(mockPref));
        when(jobSearchService.searchJobs(anyString())).thenReturn(Collections.emptyList());

        jobBatchScheduler.runNightlyBatch();

        verify(preferenceRepo, times(1)).findAll();
        verify(jobSearchService, atLeastOnce()).searchJobs(contains("Java"));
    }
}
