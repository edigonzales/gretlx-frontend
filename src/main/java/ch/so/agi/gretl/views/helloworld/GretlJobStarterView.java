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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;

import org.carlspring.cloud.storage.s3fs.S3FileSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PageTitle("GRETL-Job-Starter")
@Route(value = "")
@RouteAlias(value = "")
@Component
public class GretlJobStarterView extends VerticalLayout {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String workDirectory;
    private String platformOwner;
    private String platformToken;
    private String platformBaseUrl;

    private FileSystem fileSystem;
    
    private HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
    
    private HorizontalLayout layoutRow = new HorizontalLayout();

    private VerticalLayout layoutColumnLeft = new VerticalLayout();
    private VerticalLayout layoutColumnMiddle = new VerticalLayout();
    private VerticalLayout layoutColumnRight = new VerticalLayout();
    private VerticalLayout layoutColumnSuperRight = new VerticalLayout();
    
    private H3 h3 = new H3();
    private PasswordField tokenField = new PasswordField();
    private ComboBox<String> comboBox = new ComboBox<>("GRETL-Job");
    private Upload fileUpload;
    private Element responseElement;
   
    public GretlJobStarterView(FileSystem fileSystem, Environment env) {
        this.workDirectory = env.getProperty("app.workDirectory");
        this.platformOwner = env.getProperty("platform.owner");
        this.platformToken = env.getProperty("platform.token");
        this.platformBaseUrl = env.getProperty("platform.baseUrl");
        
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
        layoutRow.setFlexGrow(1.0, layoutColumnSuperRight);
        layoutColumnSuperRight.setWidth(null);

        h3.setText("GRETL-Job-Starter");        
        layoutColumnMiddle.add(h3);
        
        tokenField.setWidthFull();
        tokenField.setLabel("Token");

        if (platformToken != null) {
            tokenField.setValue("A server side token is used.");
            tokenField.setReadOnly(true);            
        } else {
            tokenField.setRequired(true);
            tokenField.setPlaceholder("Your personal Github token.");
        }
        layoutColumnMiddle.add(tokenField);
        
        List<String> availableGretlJobs = List.of("gretljobs-naturgefahren");
        comboBox.setWidthFull();
        comboBox.setItems(availableGretlJobs);
        comboBox.setRequired(true);
        comboBox.setValue(availableGretlJobs.get(0));
        //comboBox.setItemLabelGenerator(Country::getName);
        layoutColumnMiddle.add(comboBox);
        
        handleFileUpload();
        
        layoutRow.add(layoutColumnLeft);
        layoutRow.add(layoutColumnMiddle);
        layoutRow.add(layoutColumnRight);
        layoutRow.add(layoutColumnSuperRight);

        add(layoutRow);
    }
       
    private void handleFileUpload() {
        MemoryBuffer memoryBuffer = new MemoryBuffer();
        fileUpload = new Upload(memoryBuffer);
        fileUpload.setWidthFull();
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
          
            String payload = """
                    {"ref":"main","inputs":{"directory":"%s", "fileName":"%s"}}
                    """.formatted(key, fileName);
            
            String gretlJobName = comboBox.getValue();

            if (platformToken == null) {
                platformToken = tokenField.getValue();
            }
                        
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(createPipelineDispatchUrl(gretlJobName)))
                    .timeout(Duration.ofMinutes(1))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Authorization", "Bearer " + platformToken)
                    .POST(BodyPublishers.ofString(payload))
                    .build();
            
            try {
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                
                if (response.statusCode() != 204) {
                    responseElement = ElementFactory.createDiv();
                    layoutColumnMiddle.getElement().appendChild(responseElement);
                    
                    responseElement.appendChild(ElementFactory.createDiv("Could not start GRETL job:"));                     
                    responseElement.appendChild(ElementFactory.createDiv(response.body())); 
                    
//                    Anchor githubActionLink = new Anchor(createPipelineGuiUrl("gretljobs-naturgefahren"), "GRETL job");
//                    githubActionLink.setTarget("_blank");
//                    responseElement.appendChild(githubActionLink.getElement());
                } else {
                    getUI().get().getPage().setLocation(createPipelineGuiUrl(gretlJobName));
                }
                            
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                
                // TODO
            }
        });
        
        fileUpload
            .getElement()
            .addEventListener(
                    "file-remove",
                    event -> {
                        elemental.json.JsonObject eventData = event.getEventData();
                        //String fileName = eventData.getString("event.detail.file.name");
                        if (responseElement != null) {
                            responseElement.getParent().removeChild(responseElement);   
                        }                        
                    }).addEventData("event.detail.file.name");
    }
    
    private String createPipelineDispatchUrl(String repoName) {
        return "https://api." + platformBaseUrl + "/repos/" + platformOwner + "/" + repoName + "/actions/workflows/main.yaml/dispatches";
    }
    
    private String createPipelineGuiUrl(String repoName) {
        return "https://" + platformBaseUrl + "/" + platformOwner + "/" + repoName + "/actions";
    }

}
