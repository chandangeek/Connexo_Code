package com.elster.jupiter.users.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.Resource;

@XmlRootElement
public class ResourceInfos {

    public int total;

    public List<ResourceInfo> resources = new ArrayList<>();

    public ResourceInfos() {
    }

    public ResourceInfos(Thesaurus thesaurus, Resource resource) {
        add(thesaurus, resource);
    }

    public ResourceInfos(Thesaurus thesaurus, Iterable<? extends Resource> resources) {
        addAll(thesaurus, resources);
    }

    public ResourceInfo add(Thesaurus thesaurus, Resource resource) {
        ResourceInfo result = new ResourceInfo(thesaurus, resource);
        resources.add(result);
        total++;
        return result;
    }

    public void addAll(Thesaurus thesaurus, Iterable<? extends Resource> resources) {
        for (Resource each : resources) {
            add(thesaurus, each);
        }
    }

}
