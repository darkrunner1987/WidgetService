package ws;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class WidgetClassTest {
    @Test
    public void unique() {
        Widget widget1 = new Widget(0, 0, 1, 1, 1);
        Widget widget2 = new Widget(0, 0, 1, 1, 2);
        assertThat(widget1, not(sameInstance(widget2)));
        assertThat(widget1.getId(), not(is(widget2.getId())));
    }

    @Test
    public void compare() {
        Widget widget1 = new Widget(0, 0, 1, 1, 1);
        Widget widget2 = new Widget(0, 0, 1, 1, 2);
        assertThat(widget1.compareTo(widget2), lessThan(0));
    }

    @Test
    public void update() {
        Widget widget = new Widget(0, 0, 1, 1, 1);
        LocalDateTime date = widget.getLastUpdateDate();

        // test X, Y
        widget.setXY(1, 1);
        assertThat(widget.getLastUpdateDate().compareTo(date), greaterThan(0));

        // test width
        date = widget.getLastUpdateDate();
        widget.setWidth(1);
        assertThat(widget.getLastUpdateDate().compareTo(date), greaterThan(0));

        // test height
        date = widget.getLastUpdateDate();
        widget.setHeight(1);
        assertThat(widget.getLastUpdateDate().compareTo(date), greaterThan(0));

        // test zIndex
        date = widget.getLastUpdateDate();
        widget.setZIndex(2);
        assertThat(widget.getLastUpdateDate().compareTo(date), greaterThan(0));

    }
}
