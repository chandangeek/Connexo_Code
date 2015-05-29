//package com.elster.jupiter.issue.impl.actions.parameters;
//
//import com.elster.jupiter.issue.impl.module.MessageSeeds;
//import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
//import com.elster.jupiter.issue.share.cep.ParameterConstraint;
//import com.elster.jupiter.issue.share.cep.ParameterControl;
//import com.elster.jupiter.issue.share.cep.ParameterViolation;
//import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
//import com.elster.jupiter.nls.Thesaurus;
//import com.elster.jupiter.users.User;
//import com.elster.jupiter.users.UserService;
//import com.elster.jupiter.util.conditions.Condition;
//import com.elster.jupiter.util.conditions.Order;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class AssigneeUserParameter extends AbstractParameterDefinition {
//
//    private final Thesaurus thesaurus;
//    private final UserService userService;
//
//    public AssigneeUserParameter(Thesaurus thesaurus, UserService userService) {
//        this.thesaurus = thesaurus;
//        this.userService = userService;
//    }
//
//    @Override
//    public String getKey() {
//        return Parameter.ASSIGNEE_USER.getKey();
//    }
//
//    @Override
//    public ParameterControl getControl() {
//        return ComboBoxControl.COMBOBOX;
//    }
//
//    @Override
//    public List<Object> getDefaultValues() {
//        List<User> users = userService.getUserQuery().select(Condition.TRUE, Order.ascending("authenticationName"));
//        List<Object> result = new ArrayList<>(users.size());
//        for (User user : users) {
//            ComboBoxControl.Values value = new ComboBoxControl.Values();
//            value.id = user.getId();
//            value.title = user.getName();
//            result.add(value);
//        }
//        return result;
//    }
//
//    @Override
//    public ParameterConstraint getConstraint() {
//        return new ParameterConstraint() {
//            @Override
//            public boolean isOptional() {
//                return false;
//            }
//
//            @Override
//            public String getRegexp() {
//                return null;
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
//
//            @Override
//            public List<ParameterViolation> validate(String value, String paramKey) {
//                return Collections.emptyList();
//            }
//        };
//    }
//
//    @Override
//    public String getLabel() {
//        return MessageSeeds.PARAMETER_ASSIGNEE_USER.getTranslated(thesaurus);
//    }
//}
