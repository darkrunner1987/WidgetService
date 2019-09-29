package ws.storage;

import ws.Widget;

import java.util.ArrayList;
import java.util.UUID;

public class SqlStorage implements StorageInterface {
    public Widget get(UUID id) {
        //TODO get from sql storage
        return null;
    }

    public boolean save(Widget widget) {
        return false;
    }

    public boolean remove(UUID id) {
        return false;
    }

    public ArrayList<Widget> getAll() {
        return null;
    }
}
