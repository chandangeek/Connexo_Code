/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.ServiceCallIssueState', {
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
                        itemId: 'service-call-state',
                        enabled: false,
                        disabled: true,
                        fieldLabel: Uni.I18n.translate('serviceCall.state.label', 'UNI', 'State (trigger)'),
                        queryMode: 'local',
                        name: 'serviceCallState',
                        labelWidth: 260,
                        allowBlank: false,
                        width: 595,
                        valueField: 'name',
                        displayField: 'name',
                        store: me.getServiceCallStateStore(),
                        editable: false,
                        multiSelect: true,
                        msgTarget: 'under',
                        required: true,
                        emptyText: Uni.I18n.translate('serviceCall.state.empty', 'UNI', 'Select a state...')
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
            serviceCallStateValue = me.down('#service-call-state').getValue();

        if (serviceCallStateValue) {
            return serviceCallStateValue;
        } else return null;
     },


    getServiceCallStateStore: function () {

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
