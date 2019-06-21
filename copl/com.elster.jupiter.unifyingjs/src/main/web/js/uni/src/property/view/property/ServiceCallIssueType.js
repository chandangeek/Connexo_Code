/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ServiceCallIssueType', {
    extend: 'Uni.property.view.property.Base',

    selType: 'checkboxmodel',

    getEditCmp: function() {
        var me = this;
        me.layout = 'vbox';

        return [
            {
                items: [
                    {
                        xtype: 'uni-grid-filtertop-combobox',
                        itemId: 'service-call-type',
                        fieldLabel: Uni.I18n.translate('serviceCall.type', 'UNI', 'Service call type'),
                        queryMode: 'local',
                        name: 'serviceCallType',
                        labelWidth: 260,
                        width: 595,
                        emptyText: Uni.I18n.translate('general.serviceCall.type.empty', 'UNI', 'Select a service call type...'),
                        valueField: 'id',
                        displayField: 'name',
                        allowBlank: false,
                        multiSelect: true,
                        store: me.getServiceCallStore(),
                        msgTarget: 'under',
                        editable: false,
                        matchFieldWidth: false,
                        required: me.property.get('required'),
                        listeners: {
                            change: function (combo, newValue, oldValue) {
                                var serviceCallStateCombo = Ext.ComponentQuery.query('#service-call-state')[0];
                                
                                if (newValue.length !== 0) {
                                    serviceCallStateCombo.setDisabled(false);
                                } else {
                                    serviceCallStateCombo.clearValue();
                                    serviceCallStateCombo.setDisabled(true);
                                }
                            }
                        }
                    }
                ]
            }
        ];
    },

    setLocalizedName: function(name) {
    },

    setValue: function(value) {
    },

    getValue: function() {
        var me = this,
            serviceCallTypeValue = me.down('#service-call-type').getValue();

        if (serviceCallTypeValue) {
            return serviceCallTypeValue;
        } else return null;
     },

    getServiceCallStore: function() {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if (possibleValues) {
            for (var i = 0 ; i < possibleValues.length; i++){
                var object = {};
                object.id = possibleValues[i].id;
                object.name = possibleValues[i].name;
                elements.push(object);
            }
        }
        return Ext.create('Ext.data.JsonStore', {
            fields: [
                { name: 'id' },
                { name: 'name' },
            ],
            data: elements
        });

    }
});
