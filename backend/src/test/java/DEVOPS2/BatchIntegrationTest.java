package DEVOPS2;

import com.etudlife.batch.JobBatchScheduler;
import com.etudlife.dto.JobDTO;
import com.etudlife.model.*;
import com.etudlife.repository.*;
import com.etudlife.service.JobSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * On force Spring à charger la configuration de l'application.
 */
@SpringBootTest(classes = com.etudlife.EtudlifeApp.class)
class BatchIntegrationTest {

    @Autowired
    private JobBatchScheduler batchScheduler;

    @Autowired
    private JobPreferenceRepository preferenceRepo;

    @Autowired
    private SavedJobRepository savedJobRepo;

    @Autowired
    private CompteRepository compteRepository;

    @MockBean
    private JobSearchService jobSearchService;

    @Test
    void shouldSaveJavaStage() {
        Compte compte = compteRepository.save(new Compte());

        JobPreference pref = new JobPreference();
        pref.setCompte(compte);
        pref.setMotsCles("Java");
        pref.setLocalisation("Paris");
        pref.setFrequence("QUOTIDIEN");
        preferenceRepo.save(pref);

        JobDTO fakeJob = new JobDTO();
        fakeJob.setJobId("ID_H2_001");
        fakeJob.setTitle("Stage Java");
        fakeJob.setCompany("Entreprise Test");
        when(jobSearchService.searchJobs(anyString())).thenReturn(List.of(fakeJob));

        batchScheduler.runNightlyBatch();

        List<SavedJob> savedJobs = savedJobRepo.findByCompte(compte);

        assertThat(savedJobs).hasSize(1);
        assertThat(savedJobs.get(0).getExternalJobId()).isEqualTo("ID_H2_001");
        assertThat(savedJobs.get(0).getStatus()).isEqualTo(JobStatus.SUGGESTION);

        System.out.println("--- CONTENU DE LA TABLE SAVED_JOB DANS H2 ---");
        savedJobs.forEach(job -> {
            System.out.println("Job Sauvegardé : " + job.getTitle() + " | Entreprise : " + job.getCompany());
        });
    }
}