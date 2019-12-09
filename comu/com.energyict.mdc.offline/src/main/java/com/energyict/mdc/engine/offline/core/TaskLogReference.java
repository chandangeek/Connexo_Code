package com.energyict.mdc.engine.offline.core;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: gna
 * Date: 22/12/11
 * Time: 10:20
 * To change this template use File | Settings | File Templates.
 */
public class TaskLogReference {

    private final int protocolReaderId;
    private final String taskName;

    private static MultiMap allInstances = new MultiValueMap();

    private TaskLogReference(final String taskName, final int protocolReaderId) {
        this.taskName = taskName;
        this.protocolReaderId = protocolReaderId;
    }

    public int getProtocolReaderId() {
        return protocolReaderId;
    }

    public String getTaskName() {
        return taskName;
    }

    /**
     * Get a TaskLogReference based upon the given input parameters
     *
     * @param taskName         the name of the Task(Rtu)
     * @param protocolReaderId the ID of the ProtocolReader which will execute the Task
     * @return the requested TaskLogReference
     */
    public static TaskLogReference getLogReference(final String taskName, final int protocolReaderId) {
        List<TaskLogReference> taskLogReferences = (List<TaskLogReference>) allInstances.get(protocolReaderId);
        if (taskLogReferences != null) {
            for (TaskLogReference taskLogReference : taskLogReferences) {
                if (taskLogReference.getTaskName().equals(taskName)) {
                    return taskLogReference;
                }
            }
        }
        TaskLogReference taskLogReference = new TaskLogReference(taskName, protocolReaderId);
        synchronized (allInstances) {
            allInstances.put(protocolReaderId, taskLogReference);
        }
        return taskLogReference;
    }

    /**
     * Find or create a TaskLogReference from the given referenceString
     *
     * @param referenceString the given referenceString
     * @return the requested TaskLogReference
     */
    public static TaskLogReference fromReferenceString(String referenceString) {
        String[] parts = referenceString.split("_");
        int protocolReaderId;
        try {
            protocolReaderId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The given LogReferenceString (" + referenceString + ") does not contain a valid LogReference.");
        }

        // we don't use just the other part of the splitted String because the name of the task can contain underscores as well ...
        String taskName = referenceString.substring(referenceString.indexOf(parts[0]) + parts[0].length() + 1);
        return getLogReference(taskName, protocolReaderId);
    }

    /**
     * Remove the given TaskLogReference from the allInstance List
     *
     * @param taskLogReference the TaskLogReference to remove
     */
    public synchronized static void removeReference(TaskLogReference taskLogReference) {
        allInstances.remove(taskLogReference.getProtocolReaderId(), taskLogReference);
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return getProtocolReaderId() + "_" + getTaskName();
    }
}
