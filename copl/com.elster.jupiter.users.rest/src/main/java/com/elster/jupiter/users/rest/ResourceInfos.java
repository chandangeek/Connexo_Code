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
        resources.add(result);
        total++;
        return result;
    }

    public void addAll(NlsService nlsService, Iterable<? extends Resource> resources) {
        for (Resource each : resources) {
            add(nlsService, each);
        }
    }

}