package com.elster.jupiter.issue.impl.actions.parameters;
//
//import com.elster.jupiter.issue.impl.module.MessageSeeds;
//import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
//import com.elster.jupiter.issue.share.cep.ParameterConstraint;
//import com.elster.jupiter.issue.share.cep.ParameterControl;
//import com.elster.jupiter.issue.share.cep.ParameterViolation;
//import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
//import com.elster.jupiter.issue.share.entity.IssueStatus;
//import com.elster.jupiter.issue.share.service.IssueService;
//import com.elster.jupiter.nls.Thesaurus;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.elster.jupiter.util.conditions.Where.where;
//
//public class CloseStatusParameter extends AbstractParameterDefinition {
//    private final Thesaurus thesaurus;
//    private final IssueService issueService;
//
//    public CloseStatusParameter(IssueService issueService, Thesaurus thesaurus) {
//        this.issueService = issueService;
//        this.thesaurus = thesaurus;
//    }
//
//    @Override
//    public String getKey() {
//        return Parameter.CLOSE_STATUS.getKey();
//    }
//
//    @Override
//    public ParameterControl getControl() {
//        return ComboBoxControl.COMBOBOX;
//    }
//
//    @Override
//    public List<Object> getDefaultValues() {
//        List<IssueStatus> statuses = issueService.query(IssueStatus.class).select(where("isHistorical").isEqualTo(Boolean.TRUE));
//        List<Object> values = new ArrayList<>(statuses.size());
//        for (IssueStatus status : statuses) {
//            ComboBoxControl.Values value = new ComboBoxControl.Values();
//            value.id = status.getKey();
//            value.title = status.getName();
//            values.add(value);
//        }
//        return values;
//    }
//
//    @Override
//    public ParameterConstraint getConstraint() {
//        return new ParameterConstraint() {
//
//            @Override
//            public String getRegexp() {
//                return null;
//            }
//
//            @Override
//            public List<ParameterViolation> validate(String value, String paramKey) {
//                List<ParameterViolation> errors = new ArrayList<>();
//                IssueStatus closeStatus = issueService.findStatus(value).orElse(null);
//                if(closeStatus == null || !closeStatus.isHistorical()) {
//                    errors.add(new ParameterViolation(paramKey, MessageSeeds.ACTION_WRONG_STATUS.getTranslated(thesaurus)));
//                }
//                return errors;
//            }
//
//            @Override
//            public boolean isOptional() {
//                return false;
//            }
//
//            @Override
//            public Integer getMin() {
//                return null;
//            }
//
//            @Override
//            public Integer getMax() {
//                return null;
//            }
//        };
//    }
//
//    @Override
//    public String getLabel() {
//        return MessageSeeds.PARAMETER_CLOSE_STATUS.getTranslated(thesaurus);
//    }
//}
