package com.elster.jupiter.export;

import java.util.List;
import java.util.Optional;

public interface StructureMarker {

    List<String> getStructurePath();

    Optional<StructureMarker> getParent();
}
