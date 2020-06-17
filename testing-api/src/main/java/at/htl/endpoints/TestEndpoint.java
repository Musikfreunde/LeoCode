package at.htl.endpoints;

import io.quarkus.launcher.shaded.org.apache.commons.io.IOUtils;
import io.quarkus.launcher.shaded.org.slf4j.Logger;
import io.quarkus.launcher.shaded.org.slf4j.LoggerFactory;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/test")
public class TestEndpoint {

    private final Logger log = LoggerFactory.getLogger(TestEndpoint.class);

    @POST
    @Path("/upload")
    @Consumes("multipart/form-data")
    public Response uploadFile(MultipartFormDataInput input) {

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        // depends on form eg. name="uploadedFile"
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        log.info("Uploaded " + FileGenerator.uploadFile("../src/test/java/at/htl/examples/", inputParts));

        return Response.ok().build();
    }


}
