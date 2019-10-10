package ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ws.storage.MemoryStorage;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ConcurrencyTest {
    private int threadCount = 32;
    private ExecutorService service;

    private CountDownLatch latch;
    private AtomicBoolean running = new AtomicBoolean();
    private AtomicInteger overlaps = new AtomicInteger();

    private class Task implements Callable {
        private Callable work;

        public Task(Callable work) {
            this.work = work;
        }

        @Override
        public Object call() throws Exception {
            latch.await();
            if (running.get()) {
                overlaps.incrementAndGet();
            }
            running.set(true);
            Thread.sleep(50L);
            Object result = this.work.call();
            running.set(false);
            return result;
        }
    }

    @Before
    public void setup() {
        service = Executors.newFixedThreadPool(threadCount);
        this.latch = new CountDownLatch(1);
        this.running.set(false);
        this.overlaps.set(0);
    }

    @After
    public void shutdown() {
        service.shutdownNow();
    }

    @Test
    public void setXY() throws ExecutionException, InterruptedException {
        Widget widget = new Widget(1, 1, 1, 1);

        ArrayList<Future<long[]>> futures = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long newX = i;
            long newY = i;
            futures.add(service.submit(new Task(() -> widget.setXY(newX, newY))));
        }
        latch.countDown();
        for (Future<long[]> f : futures) {
            long[] coords = f.get();
//            System.out.printf("%d, %d\n", coords[0], coords[1]);
            assertThat(coords[0], equalTo(coords[1]));
        }

//        System.out.println(overlaps.get());
//        System.out.printf("%d, %d\n", widget.getX(), widget.getY());
        assertThat(overlaps.get(), greaterThan(0));
    }

    @Test
    public void setWidthHeight() throws ExecutionException, InterruptedException {
        Widget widget = new Widget(1, 1, 1, 1);

        ArrayList<Future<Boolean[]>> futures = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long newWidth = i;
            long newHeight = i;
            futures.add(service.submit(new Task(() -> {
                long height = widget.setHeight(newHeight);
                long width = widget.setWidth(newWidth);
                return new Boolean[]{height == newHeight, width == newWidth};
            })));
        }
        latch.countDown();
        for (Future<Boolean[]> f : futures) {
            assertThat(Arrays.asList(f.get()), everyItem(is(true)));
        }

        assertThat(overlaps.get(), greaterThan(0));
    }

    @Test
    public void incrementZIndex() throws ExecutionException, InterruptedException {
        Widget widget = new Widget(1, 1, 1, 1);

        ArrayList<Future<Long>> futures = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            futures.add(service.submit(new Task(widget::incrementZIndex)));
        }
        latch.countDown();
        for (Future<Long> f : futures) {
            f.get();
        }

        assertThat(overlaps.get(), greaterThan(0));
        assertThat(widget.getZIndex(), is((long) threadCount));
    }

    @Test
    public void insertWidgets() throws ExecutionException, InterruptedException {
        WidgetDirector widgetDirector = new WidgetDirector(new MemoryStorage());

        ArrayList<Future<Widget>> futures = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            futures.add(service.submit(new Task(() -> {
                Widget widget = new Widget(1, 1, 1, 1);
                widgetDirector.put(widget);
                return widget;
            })));
        }
        latch.countDown();
        for (Future<Widget> f : futures) {
            f.get();
        }

        assertThat(overlaps.get(), greaterThan(0));

        Widget[] widgets = widgetDirector.getAll().toArray(new Widget[]{});
        for (int i = 0; i < widgets.length - 1; i++) {
            assertThat(widgets[i].getZIndex(), not(equalTo(widgets[i + 1].getZIndex())));
        }
    }

    @Test
    public void readAndModifyWidgets() throws ExecutionException, InterruptedException {
        WidgetDirector widgetDirector = new WidgetDirector(new MemoryStorage());
        for (int i = 0; i < threadCount; i++) {
            widgetDirector.put(new Widget(1, 1, 1, 1));
        }

        ArrayList<Future<AtomicReference>> futures = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            AtomicReference<UUID> removedId = new AtomicReference<>();
            futures.add(service.submit(new Task(() -> {
                for (Widget w : widgetDirector.getAll()) {
                    removedId.set(w.getId());
                    try {
                        widgetDirector.remove(w.getId());
                    } catch (RuntimeException ignored) {

                    }
                    break;
                }
                return removedId;
            })));
        }
        latch.countDown();
        for (Future<AtomicReference> f : futures) {
            f.get();
        }

        assertThat(overlaps.get(), greaterThan(0));
    }

    @Test
    public void consistentReadWidgets() throws ExecutionException, InterruptedException {
        WidgetDirector widgetDirector = new WidgetDirector(new MemoryStorage());
        int widgetCount = threadCount;
        for (int i = 0; i < widgetCount; i++) {
            widgetDirector.put(new Widget(1, 1, 1, 1));
        }

        ArrayList<Future<ArrayList<Widget>>> futures = new ArrayList<>(threadCount);

        AtomicBoolean writing = new AtomicBoolean();
        AtomicInteger writeOverlaps = new AtomicInteger();
        for (int i = 1; i < threadCount + 1; i++) {
            int zIndex = i;
            futures.add(service.submit(new Task(() -> {
                if (writing.get()) {
                    writeOverlaps.incrementAndGet();
                }
                writing.set(true);
                widgetDirector.put(new Widget(1, 1, 1, 1, zIndex));
                writing.set(false);

                ArrayList<Object> zIndexes = new ArrayList<>();
                for (Widget w : widgetDirector.getAll()) {
                    zIndexes.add(w);
                }
                return zIndexes;
            })));
        }
        latch.countDown();
        for (Future<ArrayList<Widget>> f : futures) {
            f.get();
            ArrayList<Widget> zIndexes = f.get();
//            System.out.println(zIndexes.size());
            for (int i = 0; i < zIndexes.size() - 1; i++) {
//                System.out.printf("%s,", zIndexes.get(i).getZIndex());
//                System.out.printf("%s,", zIndexes.get(i + 1).getId());
                assertThat(zIndexes.get(i).getZIndex(), lessThan(zIndexes.get(i + 1).getZIndex()));
            }
//            System.out.println();
        }

        assertThat(overlaps.get(), greaterThan(0));

        System.out.printf("Parallel threads: %d\n", overlaps.get());
        System.out.printf("Parallel writes: %d\n", threadCount - writeOverlaps.get());
    }
}
