package com.elster.jupiter.issue.share;


import com.elster.jupiter.util.HasName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PriorityInfo implements HasName {

    private static final String PRIORITY = "priority";
    private transient Priority priority;

    public PriorityInfo(Priority priority) {
        this.priority = priority;
    }

    public Priority getPriority(){
        return priority;
    }


    public Integer getUrgency() {
        return priority.getUrgency();
    }

    public Integer getImpact() {
        return priority.getImpact();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PriorityInfo)) {
            return false;
        }

        PriorityInfo that = (PriorityInfo) o;

        return priority.equals(that.priority);

    }

    @Override
    public int hashCode() {
        return priority.hashCode();
    }

    @Override
    public String getName() {
        return PRIORITY;
    }
}
