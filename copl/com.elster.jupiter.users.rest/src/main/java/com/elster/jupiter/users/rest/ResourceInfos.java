package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.Privilege;
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

    public ResourceInfos(Resource resource) {
        add(resource);
    }

    public ResourceInfos(Iterable<? extends Resource> resources) {
        addAll(resources);
    }

    public ResourceInfo add(Resource resource) {
        ResourceInfo result = new ResourceInfo(resource);
        resources.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends Resource> resources) {
        for (Resource each : resources) {
            add(each);
        }
    }

}
