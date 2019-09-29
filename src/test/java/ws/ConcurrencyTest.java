package ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ws.storage.MemoryStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ConcurrencyTest {
    private int threadCount = 1000;
    private ExecutorService service;

    @Before
    public void setup() {
        service = Executors.newFixedThreadPool(threadCount);
    }

    @After
    public void shutdown() {
        service.shutdownNow();
    }

    @Test
    public void setXY() throws ExecutionException, InterruptedException {
        Widget widget = new Widget(1, 1, 1, 1);

        ArrayList<Future<long[]>> futures = new ArrayList<>(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            long newX = i;
            long newY = i;
            futures.add(service.submit(() -> {
                latch.await();
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                Thread.sleep(50L);
                long[] coords = widget.setXY(newX, newY);
                running.set(false);
                return coords;
            }));
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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            long newWidth = i;
            long newHeight = i;
            futures.add(service.submit(() -> {
                latch.await();
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                Thread.sleep(50L);
                long height = widget.setHeight(newHeight);
                long width = widget.setWidth(newWidth);
                running.set(false);
                return new Boolean[]{height == newHeight, width == newWidth};
            }));
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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            futures.add(service.submit(() -> {
                latch.await();
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                Thread.sleep(50L);
                long zIndex = widget.incrementZIndex();
                running.set(false);
                return zIndex;
            }));
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
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean running = new AtomicBoolean();
        AtomicInteger overlaps = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            futures.add(service.submit(() -> {
                latch.await();
                if (running.get()) {
                    overlaps.incrementAndGet();
                }
                running.set(true);
                Thread.sleep(50L);
                Widget widget = new Widget(1, 1, 1, 1);
                widgetDirector.put(widget);
                running.set(false);
                return widget;
            }));
        }
        latch.countDown();
        for (Future<Widget> f : futures) {
            f.get();
        }

        assertThat(overlaps.get(), greaterThan(0));

        ArrayList<Widget> widgets = widgetDirector.getAll();
        for (int i = 0; i < widgets.size() - 1; i++) {
            assertThat(widgets.get(i).getZIndex(), not(equalTo(widgets.get(i + 1).getZIndex())));
        }
    }
}
