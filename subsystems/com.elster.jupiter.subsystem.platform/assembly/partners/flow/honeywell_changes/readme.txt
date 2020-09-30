This folder contains flow files changed by Honeywell to update flow jars.

Changes:
1. jbpm-flow-6.4.0.Final.jar
    a. ThreadPoolSchedulerService.java
        - added retry mechanism (idea is from https://github.com/kiegroup/jbpm/pull/714/commits/ca783e9632158e16046b8bbce30938571600749f)

Instruction:
1. Clone code from https://github.com/kiegroup/jbpm.git (6.4.0.Final tag)
2. Apply changed files
3. Build necessary jars
4. Add replacing jar files in install.pl