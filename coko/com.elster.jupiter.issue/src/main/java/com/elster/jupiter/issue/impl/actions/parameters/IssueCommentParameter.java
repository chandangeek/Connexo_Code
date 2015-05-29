package com.elster.jupiter.issue.impl.actions.parameters;
//
//import com.elster.jupiter.issue.impl.module.MessageSeeds;
//import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
//import com.elster.jupiter.issue.share.cep.ParameterConstraint;
//import com.elster.jupiter.issue.share.cep.ParameterControl;
//import com.elster.jupiter.issue.share.cep.ParameterViolation;
//import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
//import com.elster.jupiter.nls.Thesaurus;
//
//import java.util.Collections;
//import java.util.List;
//
//public class IssueCommentParameter extends AbstractParameterDefinition {
//
//    private final Thesaurus thesaurus;
//
//    public IssueCommentParameter(Thesaurus thesaurus) {
//        this.thesaurus = thesaurus;
//    }
//
//    @Override
//    public String getKey() {
//        return Parameter.COMMENT.getKey();
//    }
//
//    @Override
//    public ParameterControl getControl() {
//        return SimpleControl.TEXT_AREA;
//    }
//
//    @Override
//    public ParameterConstraint getConstraint() {
//        return new ParameterConstraint() {
//            @Override
//            public boolean isOptional() {
//                return true;
//            }
//
//            @Override
//            public String getRegexp() {
//                return null;
//            }
//
//            @Override
//            public Integer getMin() {
//                return 1;
//            }
//
//            @Override
//            public Integer getMax() {
//                return null;
//            }
//
//            @Override
//            public List<ParameterViolation> validate(String value, String paramKey) {
//                /*
//                List<ParameterViolation> errors = new ArrayList<>();
//                if(is(value).emptyOrOnlyWhiteSpace()) {
//                    errors.add(new ParameterViolation(paramKey, MessageSeeds.ACTION_WRONG_COMMENT.getTranslated(thesaurus)));
//                }
//                */
//                return Collections.emptyList();
//            }
//        };
//    }
//
//    @Override
//    public String getLabel() {
//        return MessageSeeds.PARAMETER_COMMENT.getTranslated(thesaurus);
//    }
//}
