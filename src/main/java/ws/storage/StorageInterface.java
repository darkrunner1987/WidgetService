package ws.storage;

import ws.Widget;

import java.util.ArrayList;
import java.util.UUID;

public interface StorageInterface {

    public Widget get(UUID id);

    public boolean save(Widget widget);

    public boolean remove(UUID id);

    /**
     * Returns sorted array of widgets.
     * @return ArrayList<Widget>
     */
    public ArrayList<Widget> getAll();

}
