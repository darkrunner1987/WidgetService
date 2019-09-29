package ws;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ws.exception.WidgetNotFoundException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/widgets")
public class WidgetController {
    private WidgetDirector widgetDirector;

    public WidgetController(WidgetDirector widgetDirector) {
        this.widgetDirector = widgetDirector;
    }

    @GetMapping
    public List<Widget> getAll() {
        return this.widgetDirector.getAll();
    }

    @RequestMapping("/{id}")
    public ResponseEntity<Widget> get(@PathVariable UUID id) throws WidgetNotFoundException {
        Widget widget = this.widgetDirector.get(id);
        if (widget == null) {
            throw new WidgetNotFoundException();
        }

        return ResponseEntity.ok(widget);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestParam(value="x") long x, @RequestParam(value="y") long y, @RequestParam(value="width") long width, @RequestParam(value="height") long height, @RequestParam(value="zIndex", defaultValue="0") long zIndex) {
        Widget widget = this.widgetDirector.put(new Widget(x, y, width, height, zIndex));
        return ResponseEntity.ok(widget);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Widget> update(@PathVariable UUID id, @RequestParam(value="x") long x, @RequestParam(value="y") long y, @RequestParam(value="width") long width, @RequestParam(value="height") long height, @RequestParam(value="zIndex") long zIndex) {
        Widget widget = this.widgetDirector.get(id);
        if (widget == null) {
            throw new WidgetNotFoundException();
        }
        widget.setXY(x, y);
        widget.setWidth(width);
        widget.setHeight(height);
        widget.setZIndex(zIndex);

        return ResponseEntity.ok(widget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity remove(@PathVariable UUID id) {
        this.widgetDirector.remove(id);
        return ResponseEntity.accepted().build();
    }
}
