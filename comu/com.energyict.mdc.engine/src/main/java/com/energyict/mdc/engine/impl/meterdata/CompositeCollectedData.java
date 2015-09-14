package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link CollectedData} interface
 * that acts as a composite for other CollectedData objects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (13:31)
 */
public abstract class CompositeCollectedData<T extends CollectedData> extends CollectedDeviceData {

    private List<T> elements = new ArrayList<>();

    protected void add (T newElement) {
        this.elements.add(newElement);
    }

    public List<T> getElements () {
        return new ArrayList<>(this.elements);
    }

    @Override
    public List<Issue> getIssues() {
        List<Issue> issues = new ArrayList<>(super.getIssues());
        for (T collectedData : this.getElements()) {
            issues.addAll(collectedData.getIssues());
        }
        return issues;
    }

    @Override
    public void injectComTaskExecution(ComTaskExecution comTaskExecution) {
        super.injectComTaskExecution(comTaskExecution);
        this.getElements()
                .stream()
                .filter(t -> t instanceof ServerCollectedData)
                .map(ServerCollectedData.class::cast)
                .forEach(t -> t.injectComTaskExecution(comTaskExecution));
    }

    @Override
    public ResultType getResultType() {
        ResultType resultType = super.getResultType();
        for (T collectedData : this.getElements()) {
            if (this.hasPriorityOver(collectedData, resultType)) {
                resultType = collectedData.getResultType();
            }
        }
        return resultType;
    }

    /**
     * Tests if the {@link CollectedData}'s {@link ResultType} has priority
     * over the already established ResultType.
     *
     * @param collectedData The CollectedData
     * @param resultType The ResultType
     * @return A flag that indicates if the CollectedData's ResultType has priority
     */
    private boolean hasPriorityOver (T collectedData, ResultType resultType) {
        CompletionCode collectedDataCompletionCode = CompletionCode.forResultType(collectedData.getResultType());
        return collectedDataCompletionCode.hasPriorityOver(CompletionCode.forResultType(resultType));
    }

}