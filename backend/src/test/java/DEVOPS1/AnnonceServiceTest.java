package DEVOPS1;

import com.etudlife.model.Annonce;
import com.etudlife.repository.AnnonceRepository;
import com.etudlife.service.AnnonceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnnonceServiceTest {

    @Mock
    private AnnonceRepository repository;

    @InjectMocks
    private AnnonceService service;

    @Test
    public void testFindAll() {
        when(repository.findAll()).thenReturn(List.of(new Annonce(), new Annonce()));
        assertEquals(2, service.findAll().size());
    }

    @Test
    public void testFindById() {
        Annonce a = new Annonce();
        when(repository.findById(1L)).thenReturn(Optional.of(a));
        assertEquals(a, service.findById(1L));
    }

    @Test
    public void testSave() {
        Annonce a = new Annonce();
        service.save(a);
        verify(repository).save(a);
    }

    @Test
    public void testDelete() {
        service.delete(1L);
        verify(repository).deleteById(1L);
    }
}