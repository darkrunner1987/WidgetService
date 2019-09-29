package ws;

import org.springframework.stereotype.Component;
import ws.storage.StorageInterface;

import java.util.*;

@Component
public class WidgetDirector {

    private StorageInterface storage;

    WidgetDirector(StorageInterface storage) {
        this.storage = storage;
    }

    /**
     * Returns the widget by a given id
     * @param id Identifier of the widget
     * @return Widget
     */
    public Widget get(UUID id) {
        return this.storage.get(id);
    }

    /**
     * Appends a widget at the top of the stack if zIndex=0.
     * Or sets the given zIndex to the widget,
     * and increases zIndexes of all upper widgets.
     * @param widget Widget instance
     * @return Widget
     */
    public synchronized Widget put(Widget widget) {
        ArrayList<Widget> widgets = this.storage.getAll();

        if (widget.getZIndex() == 0) {
            long maxZIndex = 0;
            for (Widget w: widgets) {
                maxZIndex = Long.max(maxZIndex, w.getZIndex());
            }
            widget.setZIndex(maxZIndex + 1);
        } else {
            for (Widget w: widgets) {
                if (w.getZIndex() >= widget.getZIndex()) {
                    w.incrementZIndex();
                    this.storage.save(w);
                }
            }
        }

        if (!this.storage.save(widget)) {
            return null;
        }

        return widget;
    }

    /**
     * Removes the widget by a given id
     * @param id Identifier of the widget
     * @return boolean
     */
    public synchronized boolean remove(UUID id) {
        return this.storage.remove(id);
    }

    public ArrayList<Widget> getAll() {
        ArrayList<Widget> sortedList = this.storage.getAll();
        Collections.sort(sortedList);
        return sortedList;
    }
}
