package com.elster.partners.jbpm.extension;

import com.google.common.base.Strings;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContext;
import org.kie.server.services.*;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.command.KieCommands;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import javax.inject.Inject;

@Path("/tasks")
public class CustomJbpmResource {

    private static final Logger logger = LoggerFactory.getLogger(JbpmTaskResource.class);

    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private KieServerRegistry registry;

    FormManagerService formManagerService;

    RuntimeDataService runtimeDataService;

    public CustomJbpmResource() {}

    public CustomJbpmResource(KieServerRegistry registry,RuntimeDataService runtimeDataService,FormManagerService formManagerService) {
        this.registry = registry;
        this.runtimeDataService=runtimeDataService;
        this.formManagerService=formManagerService;
    }


    @GET
    @Path("/hello")
    @Produces("application/json")
    public Response test(@Context UriInfo uriInfo){
        String content = "{\"Hello world KIE-SERVER\":[]}";
        return Response.ok().entity(content).build();
    }

    @GET
    @Produces("application/json")
    @Path("/process/{deploymentId}/content/{processId}")
    public Response getProcessForm(@PathParam("processId") String processId, @PathParam("deploymentId") String deploymentId) {
        if (runtimeDataService.getProcessesById(processId).size() == 0) {
            return Response.ok().entity("Undeployed").build();
        }
        if (formManagerService != null) {
            String template = formManagerService.getFormByKey(deploymentId, processId);
            if (Strings.isNullOrEmpty(template)) {
                template = formManagerService.getFormByKey(deploymentId, processId + "-taskform");
            }
            if (Strings.isNullOrEmpty(template)) {
                template = formManagerService.getFormByKey(deploymentId, processId + "-taskform.form");
            }

            if (!Strings.isNullOrEmpty(template)) {
                try {
                    JAXBContext jc = JAXBContext.newInstance(ConnexoForm.class, ConnexoFormField.class, ConnexoProperty.class);
                    Unmarshaller unmarshaller = jc.createUnmarshaller();

                    StringReader reader = new StringReader(template);
                    ConnexoForm form = (ConnexoForm) unmarshaller.unmarshal(reader);

                    return Response.ok().entity(form).build();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO throw new WebApplicationException(null, Response.serverError().entity("Cannot inject entity manager factory!").build());
        return null;
    }

}
