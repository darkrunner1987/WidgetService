package ws;

import org.springframework.stereotype.Component;
import ws.exception.WidgetNotFoundException;
import ws.storage.ConcurrentStorageProxy;
import ws.storage.ConcurrentStorageInterface;
import ws.storage.StorageInterface;

import java.util.*;

@Component
public class WidgetDirector {

    private ConcurrentStorageInterface storage;

    WidgetDirector(StorageInterface storage) {
        if (storage instanceof ConcurrentStorageInterface) {
            this.storage = (ConcurrentStorageInterface) storage;
        } else {
            this.storage = new ConcurrentStorageProxy(storage);
        }
    }

    /**
     * Returns the widget by a given id
     * @param id Identifier of the widget
     * @return Widget
     */
    public Widget get(UUID id) throws WidgetNotFoundException {
        Widget widget = this.storage.get(id);
        if (widget == null) {
            throw new WidgetNotFoundException();
        }
        return widget;
    }

    /**
     * Appends a widget at the top of the stack if zIndex=0.
     * Or sets the given zIndex to the widget,
     * and increases zIndexes of all upper widgets.
     * @param widget Widget instance
     * @return Widget
     */
    public Widget put(Widget widget) {
        return this.storage.save(widget);
    }

    /**
     * Removes the widget by a given id
     * @param id Identifier of the widget
     * @return boolean
     */
    public boolean remove(UUID id) throws RuntimeException {
        Widget widget = this.storage.get(id);
        if (widget == null) {
            throw new WidgetNotFoundException();
        }
        if (this.storage.remove(id) == null) {
            throw new RuntimeException("Widget can not be deleted");
        }
        return true;
    }

    public Collection<Widget> getAll() {
        return this.storage.getAll();
    }
}
