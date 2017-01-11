package com.elster.jupiter.users.rest;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.Resource;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ResourceInfos {

    public int total;
    public List<ResourceInfo> resources = new ArrayList<>();

    public ResourceInfos() {
    }

    public ResourceInfos(NlsService nlsService, Resource resource) {
        this();
        add(nlsService, resource);
    }

    public ResourceInfos(NlsService nlsService, Iterable<? extends Resource> resources) {
        this();
        addAll(nlsService, resources);
    }

    public ResourceInfo add(NlsService nlsService, Resource resource) {
        ResourceInfo result = new ResourceInfo(nlsService, resource);
        //don't add the resource if it has no privileges (possible if it only has privileges different than the default category
        //temporary solution, contact me if this needs to change and you need info
        if(!result.privileges.isEmpty()) {
            resources.add(result);
            total++;
            return result;
        }
        return null;
    }

    public void addAll(NlsService nlsService, Iterable<? extends Resource> resources) {
        for (Resource each : resources) {
            add(nlsService, each);
        }
    }

}