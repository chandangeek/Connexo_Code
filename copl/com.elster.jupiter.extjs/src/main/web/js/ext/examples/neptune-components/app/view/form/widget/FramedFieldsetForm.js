/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.form.widget.FramedFieldsetForm', {
    extend: 'Neptune.view.form.widget.FieldsetForm',
    xtype: 'framedFieldsetForm',

    title: 'Framed Form With Fieldset',
    frame: true,
    items: [
        { xtype: 'collapsibleFieldset' }
    ]
});