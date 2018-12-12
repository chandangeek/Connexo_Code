/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.CheckboxOverride
 */
Ext.define('Uni.override.CheckboxOverride', {
    override: 'Ext.form.field.Checkbox',

    /**
     * Changes the default value from 'on' to 'true'.
     */
    inputValue: true

});