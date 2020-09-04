package com.elster.partners.jbpm.extension;

import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/tasks")
public class CustomJbpmResource {

    private static final Logger logger = LoggerFactory.getLogger(JbpmTaskResource.class);

    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private KieServerRegistry registry;

    public CustomJbpmResource() {}

    public CustomJbpmResource(KieServerRegistry registry) {
        this.registry = registry;
    }

    @GET
    @Path("/hello")
    @Produces("application/json")
    public Response test(@Context UriInfo uriInfo){
        String content = "{\"Hello world KIE-SERVER\":[]}";
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
