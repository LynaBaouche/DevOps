import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

public class GroupeTest {

    static class Groupe {
        private Long id;
        private final String name;

        Groupe(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        Long getId() { return id; }
        void setId(Long id) { this.id = id; }
        String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Groupe)) return false;
            Groupe g = (Groupe) o;
            return (id != null ? id.equals(g.id) : g.id == null) && name.equals(g.name);
        }

        @Override
        public int hashCode() {
            int result = (id != null ? id.hashCode() : 0);
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    static class InMemoryGroupeRepository {
        private final Map<Long, Groupe> store = new ConcurrentHashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        Groupe save(Groupe g) {
            if (g.getId() == null) {
                g.setId(seq.getAndIncrement());
            }
            store.put(g.getId(), g);
            return g;
        }

        Optional<Groupe> findById(Long id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    @Test
    void testCreateAndRetrieveGroupe() {
        InMemoryGroupeRepository repo = new InMemoryGroupeRepository();

        Groupe groupe = new Groupe(null, "Developers");
        Groupe saved = repo.save(groupe);

        assertNotNull(saved.getId(), "L'id du groupe ne doit pas être null après sauvegarde");
        Optional<Groupe> fetched = repo.findById(saved.getId());
        assertTrue(fetched.isPresent(), "Le groupe sauvegardé doit être retrouvé");
        assertEquals("Developers", fetched.get().getName());
        assertEquals(saved, fetched.get(), "L'objet récupéré doit être égal à l'objet sauvegardé");
    }
}
