package com.elster.jupiter.cps;

/**
 * Defines the privileges of the custom properties bundles.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-20 (15:45)
 */
public interface Privileges {

    String ADMINISTER_PRIVILEGES = "cps.privilege.administer.privileges";
    String VIEW_PRIVILEGES = "cps.privilege.view.privileges";

    String VIEW_CUSTOM_PROPERTIES_1 = "view.custom.properties.level1";
    String VIEW_CUSTOM_PROPERTIES_2 = "view.custom.properties.level2";
    String VIEW_CUSTOM_PROPERTIES_3 = "view.custom.properties.level3";
    String VIEW_CUSTOM_PROPERTIES_4 = "view.custom.properties.level4";

    String EDIT_CUSTOM_PROPERTIES_1 = "edit.custom.properties.level1";
    String EDIT_CUSTOM_PROPERTIES_2 = "edit.custom.properties.level2";
    String EDIT_CUSTOM_PROPERTIES_3 = "edit.custom.properties.level3";
    String EDIT_CUSTOM_PROPERTIES_4 = "edit.custom.properties.level4";

}