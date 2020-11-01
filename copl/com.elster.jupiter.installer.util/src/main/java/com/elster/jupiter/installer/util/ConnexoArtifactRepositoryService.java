package com.elster.jupiter.installer.util;

import org.guvnor.m2repo.backend.server.repositories.ArtifactRepository;
import org.guvnor.m2repo.backend.server.repositories.ArtifactRepositoryService;

import javax.enterprise.inject.Any;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConnexoArtifactRepositoryService extends ArtifactRepositoryService {

    private List<ArtifactRepository> repositories;

    public ConnexoArtifactRepositoryService(ArtifactRepository artifactRepository) {
        this.repositories = new ArrayList<>();
        this.repositories.add(artifactRepository);
    }

    @Override
    public List<? extends ArtifactRepository> getRepositories() {
        return (List)this.repositories.stream().filter(ArtifactRepository::isRepository).collect(Collectors.toList());
    }

    @Override
    public List<? extends ArtifactRepository> getPomRepositories() {
        return (List)this.repositories.stream().filter(ArtifactRepository::isPomRepository).collect(Collectors.toList());
    }
}