package ws;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class WidgetDeserializer extends StdDeserializer<Widget> {
    protected WidgetDeserializer() {
        this(null);
    }

    private WidgetDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Widget deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        UUID id = UUID.fromString(node.get("id").asText());
        long x = node.get("x").asLong();
        long y = node.get("y").asLong();
        long zIndex = node.get("zIndex").asLong();
        long height = node.get("height").asLong();
        long width = node.get("width").asLong();
        LocalDateTime lastUpdateDate = LocalDateTime.parse(node.get("lastUpdateDate").asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Widget widget = new Widget(x, y, width, height, zIndex);
        try {
            Field idField = widget.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(widget, id);

            Field lastUpdateDateField = widget.getClass().getDeclaredField("lastUpdateDate");
            lastUpdateDateField.setAccessible(true);
            lastUpdateDateField.set(widget, lastUpdateDate);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }

        return widget;
    }
}
