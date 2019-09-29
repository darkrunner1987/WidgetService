package ws.storage;

import ws.Widget;

import java.util.*;

public class MemoryStorage implements StorageInterface {
    private TreeMap<UUID, Widget> widgets = new TreeMap<>();

    public Widget get(UUID id) {
        return this.widgets.get(id);
    }

    public boolean save(Widget widget) {
        this.widgets.put(widget.getId(), widget);
        return true;
    }

    public boolean remove(UUID id) {
        return this.widgets.remove(id) != null;
    }

    public ArrayList<Widget> getAll() {
        return new ArrayList<>(widgets.values());
    }
}
