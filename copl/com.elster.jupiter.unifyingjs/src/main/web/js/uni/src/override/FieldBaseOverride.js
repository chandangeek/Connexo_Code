/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.FieldBaseOverride
 */
Ext.define('Uni.override.FieldBaseOverride', {
    override: 'Ext.form.field.Base',

    /**
     * Changes the default value ':'.
     */
    labelSeparator: '',

    /**
     * Changes the default value 'qtip'.
     */
    msgTarget: 'under'

});