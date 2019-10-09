package ws.storage;

import ws.Widget;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

public class MemoryStorage implements ConcurrentStorageInterface {
    private ConcurrentHashMap<UUID, Widget> widgets = new ConcurrentHashMap<>();
    private ConcurrentSkipListMap<Long, Widget> zIndexMap = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<Long, ReentrantReadWriteLock> zIndexLocks = new ConcurrentSkipListMap<>();
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
        BiConsumer<Long, Widget> shiftWidget = (Long i, Widget w) -> {
            zIndexMap.remove(i);
            w.incrementZIndex();
            zIndexMap.put(w.getZIndex(), w);
        };

        ReentrantReadWriteLock zIndexLock;
        r.unlock();
        w.lock();
        try {
            // get current zIndex lock
            zIndexLock = zIndexLocks.computeIfAbsent(widget.getZIndex(), (key) -> new ReentrantReadWriteLock(true));
            r.lock();
        } finally {
            w.unlock();
        }

        zIndexLock.writeLock().lock();
        try {
            Long higherLockKey = zIndexLocks.higherKey(widget.getZIndex());
            if (higherLockKey != null) {
                zIndexMap.subMap(widget.getZIndex(), true, higherLockKey, false).descendingMap().forEach(shiftWidget);

                ReentrantReadWriteLock higherLock = zIndexLocks.get(higherLockKey);
                System.out.printf("%d, %d - %b\n", widget.getZIndex(), higherLockKey, higherLock.isWriteLocked());

                zIndexLock.writeLock().unlock();
                higherLock.writeLock().lock();
                try {
                    zIndexMap.tailMap(higherLockKey, true).descendingMap().forEach(shiftWidget);
                } finally {
                    higherLock.writeLock().unlock();
                }
                zIndexLock.writeLock().lock();
            } else {
                zIndexMap.tailMap(widget.getZIndex(), true).descendingMap().forEach(shiftWidget);
            }

            widgets.put(widget.getId(), widget);
            zIndexMap.put(widget.getZIndex(), widget);
        } finally {
            zIndexLock.writeLock().unlock();
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
            r.unlock();
            zIndexLocks.forEach((i, lock) -> lock.readLock().unlock());
        }
    }
}
