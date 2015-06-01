package com.elster.jupiter.issue.share;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.records.HasValidActionProperties;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

@HasValidActionProperties
public abstract class AbstractIssueAction extends HasTranslatableNameAndPropertiesImpl implements IssueAction {
    
    private final DataModel dataModel;
    protected Map<String, Object> properties = new HashMap<>();
    
    protected AbstractIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(thesaurus, propertySpecService);
        this.dataModel = dataModel;
    }
    
    @Override
    public boolean isApplicable(Issue issue) {
        return issue != null;
    }
    
    @Override
    public IssueAction initAndValidate(Map<String, Object> properties) {
        this.properties = properties;
        Save.CREATE.validate(dataModel, this);
        return this;
    }

    public Map<String, Object> getProps() {
        return Collections.unmodifiableMap(this.properties);
    }
}
