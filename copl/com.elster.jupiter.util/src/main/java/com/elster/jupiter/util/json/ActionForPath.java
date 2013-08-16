package com.elster.jupiter.util.json;

import java.util.List;

public interface ActionForPath {

    void action(List<String> path, String value);

}
