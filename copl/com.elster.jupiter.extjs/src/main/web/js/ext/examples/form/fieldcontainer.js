/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.require([
    'Ext.form.*',
    'Ext.data.*',
    'Ext.tip.QuickTipManager'
]);

Ext.onReady(function() {
    Ext.QuickTips.init();

    Ext.define('Employee', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'email',     type: 'string'},
            {name: 'title',     type: 'string'},
            {name: 'firstName', type: 'string'},
            {name: 'lastName',  type: 'string'},
            {name: 'phone-1',   type: 'string'},
            {name: 'phone-2',   type: 'string'},
            {name: 'phone-3',   type: 'string'},
            {name: 'hours',     type: 'number'},
            {name: 'minutes',   type: 'number'},
            {name: 'startDate', type: 'date'},
            {name: 'endDate',   type: 'date'}
        ]
    });

    var form = Ext.create('Ext.form.Panel', {
        renderTo: 'docbody',
        title   : 'FieldContainers',
        autoHeight: true,
        width   : 600,
        bodyPadding: 10,
        defaults: {
            anchor: '100%',
            labelWidth: 100
        },
        items   : [
            {
                xtype     : 'textfield',
                name      : 'email',
                fieldLabel: 'Email Address',
                vtype: 'email',
                msgTarget: 'side',
                allowBlank: false
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: 'Date Range',
                combineErrors: true,
                msgTarget : 'side',
                layout: 'hbox',
                defaults: {
                    flex: 1,
                    hideLabel: true
                },
                items: [
                    {
                        xtype     : 'datefield',
                        name      : 'startDate',
                        fieldLabel: 'Start',
                        margin: '0 5 0 0',
                        allowBlank: false
                    },
                    {
                        xtype     : 'datefield',
                        name      : 'endDate',
                        fieldLabel: 'End',
                        allowBlank: false
                    }
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Details',
                collapsible: true,
                defaults: {
                    labelWidth: 89,
                    anchor: '100%',
                    layout: {
                        type: 'hbox',
                        defaultMargins: {top: 0, right: 5, bottom: 0, left: 0}
                    }
                },
                items: [
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: 'Phone',
                        combineErrors: true,
                        msgTarget: 'under',
                        defaults: {
                            hideLabel: true
                        },
                        items: [
                            {xtype: 'displayfield', value: '('},
                            {xtype: 'textfield',    fieldLabel: 'Phone 1', name: 'phone-1', width: 35, allowBlank: false},
                            {xtype: 'displayfield', value: ')'},
                            {xtype: 'textfield',    fieldLabel: 'Phone 2', name: 'phone-2', width: 35, allowBlank: false, margins: '0 5 0 0'},
                            {xtype: 'displayfield', value: '-'},
                            {xtype: 'textfield',    fieldLabel: 'Phone 3', name: 'phone-3', width: 48, allowBlank: false}
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: 'Time worked',
                        combineErrors: false,
                        defaults: {
                            hideLabel: true
                        },
                        items: [
                           {
                               name : 'hours',
                               xtype: 'numberfield',
                               width: 50,
                               allowBlank: false
                           },
                           {
                               xtype: 'displayfield',
                               value: 'hours'
                           },
                           {
                               name : 'minutes',
                               xtype: 'numberfield',
                               width: 50,
                               allowBlank: false
                           },
                           {
                               xtype: 'displayfield',
                               value: 'mins'
                           }
                        ]
                    },
                    {
                        xtype : 'fieldcontainer',
                        combineErrors: true,
                        msgTarget: 'side',
                        fieldLabel: 'Full Name',
                        defaults: {
                            hideLabel: true
                        },
                        items : [
                            {
                                //the width of this field in the HBox layout is set directly
                                //the other 2 items are given flex: 1, so will share the rest of the space
                                width:          65,

                                xtype:          'combo',
                                queryMode:      'local',
                                value:          'mrs',
                                triggerAction:  'all',
                                forceSelection: true,
                                editable:       false,
                                fieldLabel:     'Title',
                                name:           'title',
                                displayField:   'name',
                                valueField:     'value',
                                store:          Ext.create('Ext.data.Store', {
                                    fields : ['name', 'value'],
                                    data   : [
                                        {name : 'Mr',   value: 'mr'},
                                        {name : 'Mrs',  value: 'mrs'},
                                        {name : 'Miss', value: 'miss'}
                                    ]
                                })
                            },
                            {
                                xtype: 'textfield',
                                flex : 1,
                                name : 'firstName',
                                fieldLabel: 'First',
                                allowBlank: false
                            },
                            {
                                xtype: 'textfield',
                                flex : 1,
                                name : 'lastName',
                                fieldLabel: 'Last',
                                allowBlank: false
                            }
                        ]
                    }
                ]
            }
        ],
        buttons: [
            {
                text   : 'Load test data',
                handler: function() {
                    this.up('form').getForm().loadRecord(Ext.create('Employee', {
                        'email'    : 'abe@sencha.com',
                        'title'    : 'mr',
                        'firstName': 'Abraham',
                        'lastName' : 'Elias',
                        'startDate': '01/10/2003',
                        'endDate'  : '12/11/2009',
                        'phone-1'  : '555',
                        'phone-2'  : '123',
                        'phone-3'  : '4567',
                        'hours'    : 7,
                        'minutes'  : 15
                    }));
                }
            },
            {
                text   : 'Save',
                handler: function() {
                    var form   = this.up('form').getForm(),
                        encode = Ext.String.htmlEncode,
                        s      = '';

                    if (form.isValid()) {
                        Ext.iterate(form.getValues(), function(key, value) {
                            value = encode(value);
                            
                            s += Ext.util.Format.format("{0} = {1}<br />", key, value);
                        }, this);

                        Ext.Msg.alert('Form Values', s);
                    }
                }
            },

            {
                text   : 'Reset',
                handler: function() {
                    this.up('form').getForm().reset();
                }
            }
        ]
    });
});
