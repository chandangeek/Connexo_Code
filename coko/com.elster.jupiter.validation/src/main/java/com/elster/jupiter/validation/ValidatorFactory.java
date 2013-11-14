package com.elster.jupiter.validation;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 13/11/13
 * Time: 9:48
 * To change this template use File | Settings | File Templates.
 */
public interface ValidatorFactory {

    ThreadLocal<List<String>> available();

    Validator create(String implementation);
}
