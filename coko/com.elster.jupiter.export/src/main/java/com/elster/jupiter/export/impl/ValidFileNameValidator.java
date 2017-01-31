/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by igh on 11/09/2015.
 */
public class ValidFileNameValidator implements ConstraintValidator<ValidFileName, String> {

    @Override
    public void initialize(ValidFileName constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allowed = new ArrayList<String>();
        allowed.add("<date>");
        allowed.add("<time>");
        allowed.add("<sec>");
        allowed.add("<millisec>");
        allowed.add("<dateyear>");
        allowed.add("<datemonth>");
        allowed.add("<dateday>");
        allowed.add("<datadate>");
        allowed.add("<datatime>");
        allowed.add("<dataenddate>");
        allowed.add("<dataendtime>");
        allowed.add("<seqnrwithinday>");
        allowed.add("<datayearandmonth>");
        allowed.add("<dateformat:[^#\\<\\>$\\+%\\!`\\&\\*'\\|\\{\\}\\?\"\\=\\/:\\\\@\\s]+\\>");
        allowed.add("<identifier>");
        for (int i = 0; i< allowed.size(); i++) {
            Pattern p = Pattern.compile(allowed.get(i));
            Matcher m = p.matcher(value);
            value = m.replaceAll("");
            //value = value.replace(allowed.et(i), "");
        }
        Pattern p = Pattern.compile("[#\\<\\>$\\+%\\!`\\&\\*'\\|\\{\\}\\?\"\\=\\/:\\\\@\\s]");
        Matcher m = p.matcher(value);
        return !m.find();
    }

}

