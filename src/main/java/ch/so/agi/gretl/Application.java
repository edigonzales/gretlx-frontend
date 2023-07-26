package ch.so.agi.gretl;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import static org.carlspring.cloud.storage.s3fs.S3Factory.ACCESS_KEY;
import static org.carlspring.cloud.storage.s3fs.S3Factory.SECRET_KEY;
import static org.carlspring.cloud.storage.s3fs.S3Factory.REGION;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@Theme(value = "grextl-frontend")
public class Application implements AppShellConfigurator {
    @Value("${app.awsAccessKey}")
    private String accessKey;

    @Value("${app.awsSecretKey}")
    private String secretKey;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @ConditionalOnProperty(name = "app.storageService", havingValue = "s3", matchIfMissing = false)
//    @Bean 
//    public StorageService s3StorageService() throws IOException {
//         return new S3StorageService(s3FileSystem());
//    }    
    
    @ConditionalOnProperty(name = "app.storageService", havingValue = "s3", matchIfMissing = false)
    @Bean
    FileSystem s3FileSystem() throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put(ACCESS_KEY, accessKey);
        env.put(SECRET_KEY, secretKey);
        env.put(REGION, "eu-central-1");

        FileSystem fileSystem = FileSystems.newFileSystem(URI.create("s3://s3-eu-central-1.amazonaws.com/"), env, Thread.currentThread().getContextClassLoader());
                
        return fileSystem;
    }

}
