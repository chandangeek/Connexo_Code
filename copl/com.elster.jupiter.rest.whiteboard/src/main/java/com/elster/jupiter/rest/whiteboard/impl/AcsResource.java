package com.elster.jupiter.rest.whiteboard.impl;

import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("security")
public class AcsResource {

    @POST
    @Path("acs")
    @Consumes(MediaType.APPLICATION_XML)
    public Response handleSAMLResponse(@Context ContainerRequest request) {

        /*
            TODO: handle SAML Response
                1. Create SAML Assertion object
                2. Validate SAML Assertion
                3. Create a JWT token based on SAML Assertion (including roles mapping from IDP spec. to SP spec.)
                4. Set JWT token to header
                5. Redirect to RelayState
         */

        /*
            JWSSigner signer = new RSASSASigner(privateKey);

            List<Group> userGroups = user.getGroups();
            List<RoleClaimInfo> roles = new ArrayList<>();
            List<String> privileges = new ArrayList<>();
            for (Group group : userGroups) {

                group.getPrivileges().entrySet().forEach(s->{
                    if (s.getKey().equals("BPM") || s.getKey().equals("YFN"))
                        s.getValue().stream().forEach(p->privileges.add(p.getName()));
                });
                privileges.add("privilege.public.api.rest");
                privileges.add("privilege.pulse.public.api.rest");
                privileges.add("privilege.view.userAndRole");

                roles.add(new RoleClaimInfo(group.getId(), group.getName()));
            }


            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setCustomClaim("username", user.getName());
            claimsSet.setSubject(Long.toString(user.getId()));
            claimsSet.setCustomClaim("roles", roles);
            claimsSet.setCustomClaim("privileges", privileges);
            claimsSet.setIssuer("Elster Connexo");
            claimsSet.setCustomClaim("cnt", count);
            claimsSet.setJWTID(base64Encode(String.valueOf(new SecureRandom().nextLong())));
            claimsSet.setIssueTime(new Date());
            claimsSet.setExpirationTime(tokenExpiration);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

            signedJWT.sign(signer);
            String token = signedJWT.serialize()
         */

        String token = "a jwt token";
        String relayState = "a relay state"; // redirect url

        return Response
                .seeOther(URI.create(relayState))
                .header(HttpHeaders.AUTHORIZATION, token)
                .build();
    }

}
