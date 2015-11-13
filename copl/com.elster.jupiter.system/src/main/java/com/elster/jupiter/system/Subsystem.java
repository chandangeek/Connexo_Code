package com.elster.jupiter.system;

import java.util.List;

public interface Subsystem {
    String getId();
    String getName();
    String getVersion();
    List<Component> getComponents();
    void addComponents(List<Component> components);
}
