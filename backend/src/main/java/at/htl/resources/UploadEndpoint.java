package at.htl.resources;

import at.htl.control.FileHandler;
import at.htl.control.MultipartService;
import at.htl.entities.Example;
import at.htl.entities.File;
import at.htl.entities.FileType;
import at.htl.entities.MultipartBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("upload")
public class UploadEndpoint {

    public static final String zip = "../project-under-test.zip";
    public static boolean uploadIsFromStudent;
    public static Example example;
    @Inject
    @RestClient
    MultipartService service;
    @Inject
    Logger log;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response handleUploads(MultipartFormDataInput input) {
        Response res;

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        uploadIsFromStudent = (uploadForm.size() < 5);

        if (!uploadIsFromStudent) {
            log.info("Creating Example");
            example = new Example();

            uploadForm.forEach((k, v) -> {
                FileHandler.uploadFile(k, v);
            });

            example.persist();
            res = Response.ok("Created Example").build();
        } else {
            try {
                String username = uploadForm.get("username").get(0).getBodyAsString();
                String exampleId = uploadForm.get("example").get(0).getBodyAsString();

                List<InputPart> codeFiles = uploadForm.get("code");
                List<File> files = new LinkedList<>();

                Example example = Example.findById(Long.parseLong(exampleId));

                //search for files in db
                File pom = File.find("select f from File f where type = ?1 and example = ?2", FileType.POM, example).firstResult();
                File jenkinsfile = File.find("select f from File f where type = ?1 and example = ?2", FileType.JENKINSFILE, example).firstResult();
                List<File> tests = File.find("select f from File f where type = ?1 and example = ?2", FileType.TEST, example).list();

                files.add(pom);
                files.add(jenkinsfile);
                tests.forEach(files::add);

                //add code from student
                for (InputPart inputPart : codeFiles) {
                    try {
                        MultivaluedMap<String, String> header = inputPart.getHeaders();
                        String name = FileHandler.getFileName(header);

                        try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
                            byte[] bytes = inputStream.readAllBytes();
                            files.add(new File(name, FileType.CODE, bytes));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FileHandler.zipFiles(files);
                sendFile();

                log.info("Running Tests");
                String testResult = service.runTests();

                log.info(testResult);
                res = Response.ok(testResult).build();
            } catch (Exception e) {
                res = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
                e.printStackTrace();
            }
        }
        return res;
    }

    public void sendFile() {
        log.info("Sending Files to Testing API");

        try (InputStream inputStream = new FileInputStream(zip)) {
            service.sendMultipartData(new MultipartBody(zip, inputStream));

            new java.io.File(zip).delete();
        } catch (FileNotFoundException e) { //TODO: print Error to User
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
