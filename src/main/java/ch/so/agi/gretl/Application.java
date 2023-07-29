package ch.so.agi.gretl;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    
    @Bean
    S3Client s3Client() {
        System.setProperty("aws.accessKeyId", accessKey);
        System.setProperty("aws.secretAccessKey", secretKey);
        
        Region region = Region.EU_CENTRAL_1;
        S3Client s3Client = S3Client.builder().region(region).build();
                
        return s3Client;
    }
}
