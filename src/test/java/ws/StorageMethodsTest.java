package ws;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ws.storage.StorageInterface;

import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StorageMethodsTest {

    @Autowired
    private StorageInterface storage;

    @Test
    public void save() {
        Widget widget = new Widget(0, 0, 1, 1, 1);
        assertThat(storage.save(widget), notNullValue());
        assertThat(widget, is(storage.get(widget.getId())));
    }

    @Test
    public void remove() {
        Widget widget = new Widget(0, 0, 1, 1, 1);
        UUID id = widget.getId();
        storage.save(widget);
        assertThat(storage.get(id), notNullValue());
        storage.remove(id);
        assertThat(storage.get(id), nullValue());
    }

    @Test
    public void getAll() {
        Widget widget1 = new Widget(0, 0, 1, 1, 1);
        Widget widget2 = new Widget(0, 0, 1, 1, 2);

        storage.save(widget1);
        storage.save(widget2);
        Collection<Widget> storedWidgets = storage.getAll();

        // check values
        assertThat(storedWidgets, hasItems(widget1));
        assertThat(storedWidgets, hasItems(widget2));

        // check size
        assertThat(storedWidgets, hasSize(2));

        // check order
        assertThat(storedWidgets, contains(widget1, widget2));
    }
}
