package ws;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@JsonDeserialize(using = WidgetDeserializer.class)
public class Widget implements Comparable<Widget>{
    private final UUID id;
    private volatile long x;
    private volatile long y;
    private AtomicLong zIndex = new AtomicLong(0);
    private volatile long height;
    private volatile long width;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private volatile LocalDateTime lastUpdateDate;

    public Widget() {
        this.id = UUID.randomUUID();
        this.lastUpdateDate = LocalDateTime.now();
    }

    public Widget(long x, long y, long width, long height) {
        this();
        this.setXY(x, y);
        this.setWidth(width);
        this.setHeight(height);
    }

    public Widget(long x, long y, long width, long height, long zIndex) {
        this(x, y, width, height);
        this.setZIndex(zIndex);
    }

    public UUID getId() {
        return this.id;
    }

    public long getX() {
        return this.x;
    }

    public long getY() {
        return this.y;
    }

    @JsonProperty(value = "zIndex")
    public long getZIndex() {
        return this.zIndex.get();
    }

    public long getHeight() {
        return this.height;
    }

    public long getWidth() {
        return this.width;
    }

    public LocalDateTime getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public synchronized long[] setXY(long x, long y) {
        this.x = x;
        this.y = y;
        this.lastUpdateDate = LocalDateTime.now();
        return new long[]{this.x, this.y};
    }

    public synchronized long setZIndex(long zIndex) {
        this.zIndex.set(zIndex);
        this.lastUpdateDate = LocalDateTime.now();
        return this.zIndex.get();
    }

    public synchronized long incrementZIndex() {
        this.zIndex.incrementAndGet();
//        this.zIndex.set(this.zIndex.get() + 1); // fail test
        this.lastUpdateDate = LocalDateTime.now();
        return this.zIndex.get();
    }

    public synchronized long setHeight(long height) throws IllegalArgumentException {
        if (height < 0) {
            throw new IllegalArgumentException("Height cannot be negative");
        }
        this.height = height;
        this.lastUpdateDate = LocalDateTime.now();
        return this.height;
    }

    public synchronized long setWidth(long width) throws IllegalArgumentException {
        if (width < 0) {
            throw new IllegalArgumentException("Width cannot be negative");
        }
        this.width = width;
        this.lastUpdateDate = LocalDateTime.now();
        return this.width;
    }

    public int compareTo(Widget widget) {
        return Long.compare(this.zIndex.get(), widget.getZIndex());
    }

}
