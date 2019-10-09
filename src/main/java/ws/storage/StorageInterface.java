package ws.storage;

import ws.Widget;

import java.util.Collection;
import java.util.UUID;

public interface StorageInterface {

    Widget get(UUID id);

    Widget save(Widget widget);

    Widget remove(UUID id);

    /**
     * Returns ordered array of widgets.
     * @return ArrayList<Widget>
     */
    Collection<Widget> getAll();

}
