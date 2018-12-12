/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.form.widget.Fieldset', {
    extend: 'Ext.form.FieldSet',
    xtype: 'basicFieldset',

    title: 'Fieldset',
    bodyPadding: 10,
    width: 300,
    defaults: {
        anchor: '100%'
    },
    items: [
        { xtype: 'textField' },
        { xtype: 'comboBox' },
        { xtype: 'numberField' }
    ]
});