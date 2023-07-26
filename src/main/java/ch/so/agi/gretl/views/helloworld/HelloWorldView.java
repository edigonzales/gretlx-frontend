package ch.so.agi.gretl.views.helloworld;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PageTitle("GRETL-Job-Starter")
@Route(value = "")
@RouteAlias(value = "")
@Component
public class HelloWorldView extends VerticalLayout {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.workDirectory}")
    private String workDirectory;

    @Value("${app.githubToken}")
    private String githubToken;

    @Value("${app.githubRepoApiUrl}")
    private String githubRepoApiUrl;

    private FileSystem fileSystem;
    
    private HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();

    @Autowired
    private ObjectMapper objectMapper;
    
    private HorizontalLayout layoutRow = new HorizontalLayout();

    private VerticalLayout layoutColumnLeft = new VerticalLayout();
    private VerticalLayout layoutColumnMiddle = new VerticalLayout();
    private VerticalLayout layoutColumnRight = new VerticalLayout();
    
    private H3 h3 = new H3();
    
    private Upload fileUpload;
    

    public HelloWorldView(FileSystem fileSystem) {
        this.fileSystem = (S3FileSystem) fileSystem;
        
        addClassName(Padding.XLARGE);
        
        layoutRow.setWidthFull();
        layoutRow.setFlexGrow(1.0, layoutColumnLeft);
        layoutColumnLeft.setWidth(null);
        layoutRow.setFlexGrow(1.0, layoutColumnMiddle);
        layoutColumnMiddle.setWidth(null);
        layoutColumnMiddle.addClassName(Gap.XLARGE);
        layoutRow.setFlexGrow(1.0, layoutColumnRight);
        layoutColumnRight.setWidth(null);
        
        h3.setText("GRETL-Jobs-Uploader");        
        layoutColumnMiddle.add(h3);

        handleFileUpload();
        
        layoutRow.add(layoutColumnLeft);
        layoutRow.add(layoutColumnMiddle);
        layoutRow.add(layoutColumnRight);

        add(layoutRow);
        
        
    }
    
    private void handleFileUpload() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        fileUpload = new Upload(memoryBuffer);
        layoutColumnMiddle.add(fileUpload);

        fileUpload.setAutoUpload(false);
        fileUpload.setAcceptedFileTypes(".xtf");

        int maxFileSizeInBytes = 50 * 1024 * 1024; // 50MB. Muss kleiner sein als in application.properties.
        fileUpload.setMaxFileSize(maxFileSizeInBytes);

        
        fileUpload.addSucceededListener(event -> {
            InputStream fileData = memoryBuffer.getInputStream();
            String fileName = event.getFileName();
            
            UUID uuid = UUID.randomUUID();
            String key = uuid.toString();

            Path workDirectoryPath = fileSystem.getPath(workDirectory);
            Path targetPath = workDirectoryPath.resolve(fileSystem.getPath(key, fileName));

            try {
                Files.copy(fileData, targetPath);
            } catch (IOException e) {
                e.printStackTrace();
                
                // TODO
            }
            
            
            Element response2 = ElementFactory
                    .createDiv("Hello!");
            layoutColumnMiddle.getElement().appendChild(response2);

            String payload = """
                    {"ref":"main","inputs":{"dataFileUrl":"Mona the Octocat"}}
                    """;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(githubRepoApiUrl + "/main.yaml/dispatches"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Authorization", "Bearer " + githubToken)
                    .POST(BodyPublishers.ofString(payload))
                    .build();

            try {
                System.out.println(request);
                System.out.println(request.uri());

                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                System.out.println(response.body());

                Map<String, Object> result = objectMapper.readValue(response.body(), HashMap.class);

                
                System.out.println(result);
            
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                
                // TODO
            }
            
//            curl -L \
//            -X POST \
//            -H "Accept: application/vnd.github+json" \
//            -H "Authorization: Bearer <YOUR-TOKEN>" \
//            -H "X-GitHub-Api-Version: 2022-11-28" \
//            https://api.github.com/repos/OWNER/REPO/actions/workflows/WORKFLOW_ID/dispatches \
//            -d '{"ref":"topic-branch","inputs":{"name":"Mona the Octocat","home":"San Francisco, CA"}}'


//            curl -L \
//            -H "Accept: application/vnd.github+json" \
//            -H "Authorization: Bearer <YOUR-TOKEN>" \
//            -H "X-GitHub-Api-Version: 2022-11-28" \
//            https://api.github.com/repos/OWNER/REPO/actions/workflows

            
            
            
            Anchor githubActionLink = new Anchor("https://github.com/sogis/grextl-natgef", "Natgef GRETL");
            githubActionLink.setTarget("_blank");
            layoutColumnMiddle.add(githubActionLink);

        });
        
        
        fileUpload
        .getElement()
        .addEventListener(
          "file-remove",
          event -> {
             elemental.json.JsonObject eventData = event.getEventData();
            String fileName = eventData.getString("event.detail.file.name");
            
            System.out.println("clear ui");
            // ...
          }).addEventData("event.detail.file.name");

        
    }

}
