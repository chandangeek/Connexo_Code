/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.require([
    'Ext.form.*',
    'Ext.data.*'
]);

Ext.define('example.contact', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'first', mapping: 'name > first'},
        {name: 'last', mapping: 'name > last'},
        'company', 'email', 'state',
        {name: 'dob', type: 'date', dateFormat: 'm/d/Y'}
    ]
});
    
Ext.define('example.fielderror', {
    extend: 'Ext.data.Model',
    fields: ['id', 'msg']
});

Ext.onReady(function(){

    var formPanel = new Ext.form.Panel({
        renderTo: 'form-ct',
        frame: true,
        title:'XML Form',
        width: 340,
        bodyPadding: 5,
        waitMsgTarget: true,

        fieldDefaults: {
            labelAlign: 'right',
            labelWidth: 85,
            msgTarget: 'side'
        },

        // configure how to read the XML data, using an instance
        reader : new Ext.data.reader.Xml({
            model: 'example.contact',
            record : 'contact',
            successProperty: '@success'
        }),

        // configure how to read the XML error, using a config
        errorReader: {
            type: 'xml',
            model: 'example.fielderror',
            record : 'field',
            successProperty: '@success'
        },

        items: [{
            xtype: 'fieldset',
            title: 'Contact Information',
            defaultType: 'textfield',
            defaults: {
                width: 280
            },
            items: [{
                    fieldLabel: 'First Name',
                    emptyText: 'First Name',
                    name: 'first'
                }, {
                    fieldLabel: 'Last Name',
                    emptyText: 'Last Name',
                    name: 'last'
                }, {
                    fieldLabel: 'Company',
                    name: 'company'
                }, {
                    fieldLabel: 'Email',
                    name: 'email',
                    vtype:'email'
                }, {
                    xtype: 'combobox',
                    fieldLabel: 'State',
                    name: 'state',
                    store: Ext.create('Ext.data.ArrayStore', {
                        fields: ['abbr', 'state'],
                        data : Ext.example.states // from states.js
                    }),
                    valueField: 'abbr',
                    displayField: 'state',
                    typeAhead: true,
                    queryMode: 'local',
                    emptyText: 'Select a state...'
                }, {
                    xtype: 'datefield',
                    fieldLabel: 'Date of Birth',
                    name: 'dob',
                    allowBlank: false,
                    maxValue: new Date()
                }
            ]
        }],

        buttons: [{
            text: 'Load',
            handler: function(){
                formPanel.getForm().load({
                    url: 'xml-form-data.xml',
                    waitMsg: 'Loading...'
                });
            }
        }, {
            text: 'Submit',
            disabled: true,
            formBind: true,
            handler: function(){
                this.up('form').getForm().submit({
                    url: 'xml-form-errors.xml',
                    submitEmptyText: false,
                    waitMsg: 'Saving Data...'
                });
            }
        }]
    });

});
