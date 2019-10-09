package ws.storage;

import ws.Widget;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

public class NonConcurrentMemoryStorage implements StorageInterface {
    private HashMap<UUID, Widget> widgets = new HashMap<>();
    private TreeMap<Long, Widget> zIndexMap = new TreeMap<>();

    public Widget get(UUID id) {
        return this.widgets.get(id);
    }

    public Widget save(Widget widget) {
        this.widgets.put(widget.getId(), widget);
        this.zIndexMap.put(widget.getZIndex(), widget);
        return widget;
    }

    public Widget remove(UUID id) {
        Widget widget = this.widgets.remove(id);
        if (widget != null) {
            this.zIndexMap.remove(widget.getZIndex());
        }
        return widget;
    }

    public Collection<Widget> getAll() {
        return this.zIndexMap.values();
    }
}
