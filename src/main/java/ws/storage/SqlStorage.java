package ws.storage;

import ws.Widget;

import java.util.Collection;
import java.util.UUID;

public class SqlStorage implements ConcurrentStorageInterface {
    public Widget get(UUID id) {
        //TODO get from sql storage
        // SELECT * FROM widgets WHERE id = :id;
        return null;
    }

    public Widget save(Widget widget) {
        // INSERT INTO widgets (id, x, y, width, height, z_index) SELECT :id, :x, :y, :width, :height, max(z_index) + 1 FROM widgets;
        // OR
        // BEGIN;
        // UPDATE widgets SET z_index = z_index + 1 WHERE z_index >= :z_index;
        // INSERT INTO widgets (id, x, y, width, height, z_index) VALUES (:id, :x, :y, :width, :height, :z_index) RETURNING id, x, y, width, height, z_index;
        // COMMIT;
        return null;
    }

    public Widget remove(UUID id) {
        // DELETE FROM widgets WHERE id = :id RETURNING id, x, y, width, height, z_index;
        return null;
    }

    public Collection<Widget> getAll() {
        // SELECT * FROM widgets ORDER BY z_index;
        return null;
    }
}
