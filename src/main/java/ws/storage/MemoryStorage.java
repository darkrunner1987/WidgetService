package ws.storage;

import ws.Widget;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class MemoryStorage implements ConcurrentStorageInterface {
    private ConcurrentHashMap<UUID, Widget> widgets = new ConcurrentHashMap<>();
    private ConcurrentSkipListMap<Long, Widget> zIndexMap = new ConcurrentSkipListMap<>();
    private NavigableMap<Long, ReentrantReadWriteLock> zIndexLocks = new ConcurrentSkipListMap<>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public Widget get(UUID id) {
        r.lock();
        try {
            return widgets.get(id);
        } finally {
            r.unlock();
        }
    }

    public Widget save(Widget widget) {
        r.lock();
        try {
            if (widget.getZIndex() == 0) {
                r.unlock();
                w.lock();
                try {
                    widget.setZIndex((zIndexMap.isEmpty() ? 0 : zIndexMap.lastKey()) + 1);
                    widgets.put(widget.getId(), widget);
                    updateZIndexMap(widget);
                } finally {
                    w.unlock();
                }
                r.lock();
            } else {
                shiftUpperWidgets(widget);
            }
            return widget;
        } finally {
            r.unlock();
        }
    }

    private void updateZIndexMap(Widget widget) {
        Widget oldWidget = widgets.get(widget.getId());
        if (oldWidget != null && oldWidget.getZIndex() != widget.getZIndex()) {
            zIndexMap.remove(oldWidget.getZIndex());
        }
        zIndexMap.put(widget.getZIndex(), widget);
    }

    private void shiftUpperWidgets(Widget widget) {
        Consumer<NavigableMap<Long, Widget>> shiftWidgets = (NavigableMap<Long, Widget> map) -> {
            Iterator<Map.Entry<Long, Widget>> iterator = map.entrySet().iterator();
            Widget prevWidget = null;
            while (iterator.hasNext()) {
                Map.Entry<Long, Widget> entry = iterator.next();
                Widget currWidget = entry.getValue();
                if (prevWidget == null) {
                    iterator.remove();
                } else {
                    map.put(currWidget.getZIndex(), prevWidget);
                }
                prevWidget = currWidget;
                prevWidget.incrementZIndex();
            }
            if (prevWidget != null) {
                zIndexMap.put(prevWidget.getZIndex(), prevWidget);
            }
        };

        ReentrantReadWriteLock zIndexLock;
        // get current zIndex lock
        r.unlock();
        w.lock();
        try {
            zIndexLock = zIndexLocks.computeIfAbsent(widget.getZIndex(), (key) -> new ReentrantReadWriteLock(true));
            r.lock();
        } finally {
            w.unlock();
        }

        Map.Entry<Long, ReentrantReadWriteLock> lowerLockEntry = zIndexLocks.lowerEntry(widget.getZIndex());
        if (lowerLockEntry != null) {
            lowerLockEntry.getValue().readLock().lock();
        }
        zIndexLock.writeLock().lock();
        try {
            Long higherLockKey = zIndexLocks.ceilingKey(widget.getZIndex());
            if (higherLockKey != null) {
                shiftWidgets.accept(zIndexMap.subMap(widget.getZIndex(), true, higherLockKey, false));

                ReentrantReadWriteLock higherLock = zIndexLocks.get(higherLockKey);
//                System.out.printf("%d, %d - %b\n", widget.getZIndex(), higherLockKey, higherLock.isWriteLocked());

                higherLock.readLock().lock();
                try {
                    shiftWidgets.accept(zIndexMap.tailMap(higherLockKey, true));
                } finally {
                    higherLock.readLock().unlock();
                }
            } else {
                shiftWidgets.accept(zIndexMap.tailMap(widget.getZIndex(), true));
            }

            zIndexMap.put(widget.getZIndex(), widget);
        } finally {
            zIndexLock.writeLock().unlock();
            if (lowerLockEntry != null) {
                lowerLockEntry.getValue().readLock().unlock();
            }
        }

        r.unlock();
        w.lock();
        try {
            widgets.put(widget.getId(), widget);
            r.lock();
        } finally {
            w.unlock();
        }
    }

    public Widget remove(UUID id) {
        w.lock();
        try {
            Widget widget = widgets.remove(id);
            if (widget != null) {
                zIndexMap.remove(widget.getZIndex());
            }
            return widget;
        } finally {
            w.unlock();
        }
    }

    public Collection<Widget> getAll() {
        r.lock();
        zIndexLocks.forEach((i, lock) -> lock.readLock().lock());
        try {
            return zIndexMap.values();
        } finally {
            zIndexLocks.forEach((i, lock) -> lock.readLock().unlock());
            r.unlock();
        }
    }
}
