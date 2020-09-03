package com.elster.partners.jbpm.extension;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.cdi.Selectable;
import org.jbpm.services.cdi.producer.UserGroupInfoProducer;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.task.TaskService;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.drools.RulesExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@Path("/tasks")
public class CustomJbpmResource {

    private static final Logger logger = LoggerFactory.getLogger(JbpmTaskResource.class);

    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private KieServerRegistry registry;


    private static final String PROPERTY = "property";
    private static final String DEFAULT_SORTING = " order by p.START_DATE DESC";

    @Inject
    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;

    @Inject
    TaskService taskService;

//    @Inject
//    protected ProcessRequestBean processRequestBean;

    @Inject
    RuntimeDataService runtimeDataService;

    @Inject
    DeploymentService deploymentService;

    @Inject
    FormManagerService formManagerService;

    @Inject
    @Selectable
    private Instance<UserGroupInfoProducer> userGroupInfoProducers;


    public CustomJbpmResource() {}

    public CustomJbpmResource(KieServerRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Path("/hello")
    @Produces("application/json")
    public Response test(@Context UriInfo uriInfo){
        String content = "{\"Hello world\":[]}";
        return Response.ok().entity(content).build();
    }


//    // Supported HTTP method, path parameters, and data formats:
//    @POST
//    @Path("/server/containers/instances/{containerId}/ksession/{ksessionId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response insertFireReturn(@Context HttpHeaders headers,
//                                     @PathParam("containerId") String id,
//                                     @PathParam("ksessionId") String ksessionId,
//                                     String cmdPayload) {
//
////        Variant v = getVariant(headers);
//        String contentType = headers.getMediaType().toString();
//
//        // Marshalling behavior and supported actions:
//        MarshallingFormat format = MarshallingFormat.fromType(contentType);
//        if (format == null) {
//            format = MarshallingFormat.valueOf(contentType);
//        }
//        try {
//            KieContainerInstance kci = registry.getContainer(id);
//
//            Marshaller marshaller = kci.getMarshaller(format);
//
//            List<?> listOfFacts = marshaller.unmarshall(cmdPayload, List.class);
//
//            List<Command<?>> commands = new ArrayList<Command<?>>();
//            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, ksessionId);
//
//            for (Object fact : listOfFacts) {
//                commands.add(commandsFactory.newInsert(fact, fact.toString()));
//            }
//            commands.add(commandsFactory.newFireAllRules());
//            commands.add(commandsFactory.newGetObjects());
//
//            ExecutionResults results = rulesExecutionService.call(kci, executionCommand);
//
//            String result = marshaller.marshall(results);
//
//
//            logger.debug("Returning OK response with content '{}'", result);
//            return Response.ok().entity(result).build();
//        } catch (Exception e) {
//            // If marshalling fails, return the `call-container` response to maintain backward compatibility:
//            String response = "Execution failed with error : " + e.getMessage();
//            logger.debug("Returning Failure response with content '{}'", response);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
//        }
//    }
}
