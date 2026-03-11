package DEVOPS2;

import com.etudlife.controller.JobController;
import com.etudlife.model.JobStatus;
import com.etudlife.model.SavedJob;
import com.etudlife.service.JobSearchService;
import com.etudlife.service.SavedJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = com.etudlife.EtudlifeApp.class)
@WebMvcTest(JobController.class)
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobSearchService jobSearchService;

    @MockBean
    private SavedJobService savedJobService;

    @Test
    void testSearch_ShouldAppendFranceToQuery() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("query", "Développeur")
                        .param("location", "Paris"))
                .andExpect(status().isOk());

        verify(jobSearchService).searchJobs("Développeur Paris France");
    }

    @Test
    void testGetJobStats_ShouldReturnCorrectKPIs() throws Exception {
        SavedJob job1 = new SavedJob(); job1.setStatus(JobStatus.INTERESSE);
        SavedJob job2 = new SavedJob(); job2.setStatus(JobStatus.POSTULE);
        SavedJob job3 = new SavedJob(); job3.setStatus(JobStatus.POSTULE);

        // CORRECTION : Ajout de 1L
        when(savedJobService.getJobsByStatus(null, 1L)).thenReturn(List.of(job1, job2, job3));

        mockMvc.perform(get("/api/jobs/stats")
                        .param("compteId", "1") // CORRECTION : Le contrôleur exige maintenant ce paramètre
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interesse").value(1))
                .andExpect(jsonPath("$.postule").value(2))
                .andExpect(jsonPath("$.refuse").value(0));
    }

    @Test
    void testSaveJobStatus_ShouldReturnHttp200() throws Exception {
        // CORRECTION : Ajout de compteId: 1 dans le payload
        String jsonPayload = """
                {
                    "compteId": 1,
                    "externalJobId": "job_999",
                    "title": "Développeur Fullstack",
                    "company": "Tech Corp",
                    "location": "Paris",
                    "applyLink": "https://example.com/apply",
                    "status": "INTERESSE"
                }
                """;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/jobs/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyJobs_WithStatusFilter_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/jobs/my-jobs")
                        .param("status", "POSTULE")
                        .param("compteId", "1")) // CORRECTION : Le contrôleur exige ce paramètre
                .andExpect(status().isOk());

        // CORRECTION : Ajout de 1L
        verify(savedJobService).getJobsByStatus(JobStatus.POSTULE, 1L);
    }
}