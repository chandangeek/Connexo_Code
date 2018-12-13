/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.form.widget.FieldsetForm', {
    extend: 'Ext.form.Panel',
    xtype: 'fieldsetForm',

    title: 'Form With Fieldset',
    bodyPadding: 10,
    defaults: {
        anchor: '100%'
    },
    items: [
        { xtype: 'basicFieldset' }
    ],

    buttons: [{
        text: 'Save'
    },{
        text: 'Cancel'
    }]
});