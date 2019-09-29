package ws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import ws.storage.MemoryStorage;
import ws.storage.SqlStorage;
import ws.storage.StorageInterface;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Validated
public class StorageConfig {
    private String driver;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Bean("storage")
    public StorageInterface getStorage() throws Exception {
        switch (this.driver) {
            case "memory": return new MemoryStorage();
            case "sql": return new SqlStorage();
            default: throw new Exception("Storage driver not found");
        }
    }
}
