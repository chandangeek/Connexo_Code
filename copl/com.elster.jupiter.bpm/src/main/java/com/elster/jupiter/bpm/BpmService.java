package com.elster.jupiter.bpm;

public interface BpmService {
    String COMPONENTNAME = "BPM";

    BpmEngine createBpmDirectory(String name);

    BpmEngine findBpmDirectory(String name);
}
