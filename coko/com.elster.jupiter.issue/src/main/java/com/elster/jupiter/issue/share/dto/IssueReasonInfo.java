package com.elster.jupiter.issue.share.dto;

import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.properties.HasIdAndName;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IssueReasonInfo extends HasIdAndName {

    private transient IssueReason issueReason;

    public IssueReasonInfo(IssueReason issueReason) {
        this.issueReason = issueReason;
    }

    @Override
    public String getId() {
        return issueReason.getKey();
    }

    @Override
    public String getName() {
        return issueReason.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        IssueReasonInfo that = (IssueReasonInfo) o;

        return issueReason.getId() == that.issueReason.getId();

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + issueReason.getKey().hashCode();
        return result;
    }
}
