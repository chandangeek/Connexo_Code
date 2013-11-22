package com.energyict.mdc.common;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9-dec-2010
 * Time: 13:22:36
 */
public interface SystemParameterSpec {

    /**
     * @return the name of the Spec
     *         The name is used as key to couple the 'spec' to the SystemParameter
     */
    String getName();

    /**
     * @return the (translation)key (in the resourceBundle) for a readable name
     */
    String getDisplayname();

    /**
     * @return the (translation)key (in the resourceBundle) for the description
     */
    String getDescription();

    /**
     * @return the default value for the SystemParameterSpec
     */
    String getDefaultValue();

    /**
     * @return the module the parameter is used in
     */
    ApplicationModule getModule();

    /**
     * @returns the category of the SystemParameter
     */
    String getCategory();

    /**
     * @return the object type of the <Code>SystemParameter</Code>
     */
    Class getType();

    /**
     * @return true if a Boolean is stored as an integer: 0 = false /1 = true
     *         false if a Boolean is stored as "false"/true
     */
    boolean storeBooleanAsInt();

    /**
     * @return a List of possible values
     */
    List<String> getPossibleValues();

    /**
     * @return false if once set the Parameter Value cannot be changed any more/ true if the parameter can always be changed
     */
    boolean isMutable();

}
