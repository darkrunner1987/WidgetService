package ws;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ws.config.StorageConfig;

public class StorageCreateTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createException() throws Exception {
        StorageConfig config = new StorageConfig();
        config.setDriver("null");
        thrown.expect(Exception.class);
        thrown.expectMessage(CoreMatchers.containsString("not found"));
        config.getStorage();
    }
}
