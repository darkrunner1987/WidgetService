package ws.storage;

import ws.Widget;

import java.util.Collection;
import java.util.UUID;

public class ConcurrentStorageProxy implements ConcurrentStorageInterface {
    private StorageInterface storage;

    public ConcurrentStorageProxy(StorageInterface storage) {
        this.storage = storage;
    }

    public Widget get(UUID id) {
        return this.storage.get(id);
    }

    public synchronized Widget save(Widget widget) {
        return this.storage.save(widget);
    }

    public synchronized Widget remove(UUID id) {
        return this.storage.remove(id);
    }

    public Collection<Widget> getAll() {
        return this.storage.getAll();
    }
}
