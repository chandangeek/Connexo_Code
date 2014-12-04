package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChannelsOnDeviceResource {
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ExceptionFactory exceptionFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public ChannelsOnDeviceResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Provider<ChannelResourceHelper> channelHelper) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.channelHelper = channelHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannels(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mrid, (d -> this.getFilteredChannels(d, filter)), queryParameters);
    }

    @GET
    @Path("/{channelid}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getChannel(@PathParam("mRID") String mrid, @PathParam("channelid") long channelId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Channel channel = device.getLoadProfiles().stream()
                .flatMap(lp -> lp.getChannels().stream())
                .filter(c -> c.getId() == channelId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CHANNEL_ON_DEVICE, mrid, channelId));
        return channelHelper.get().getChannel(() -> channel);
    }

    private List<Channel> getFilteredChannels(Device device, JsonQueryFilter filter){
        Predicate<String> filterByLoadProfileName = getFilterIfAvailable("loadProfileName", filter);
        Predicate<String> filterByChannelName = getFilterIfAvailable("channelName", filter);
        return device.getLoadProfiles().stream()
                .filter(l -> filterByLoadProfileName.test(l.getLoadProfileSpec().getLoadProfileType().getName()))
                .flatMap(l -> l.getChannels().stream())
                .filter(c -> filterByChannelName.test(c.getName()))
                .sorted(Comparator.comparing(Channel::getName))
                .collect(Collectors.toList());
    }

    private Predicate<String> getFilterIfAvailable(String name, JsonQueryFilter filter){
        if (filter.hasProperty(name)){
            Pattern pattern = getFilterPattern(filter.getString(name));
            if (pattern != null){
                return s -> pattern.matcher(s).matches();
            }
        }
        return s -> true;
    }

    private Pattern getFilterPattern(String filter){
        if (filter != null){
            filter = Pattern.quote(filter.replace('%', '*'));
            return Pattern.compile(filter.replaceAll("([*?])", "\\\\E\\.$1\\\\Q"));
        }
        return null;
    }
}
