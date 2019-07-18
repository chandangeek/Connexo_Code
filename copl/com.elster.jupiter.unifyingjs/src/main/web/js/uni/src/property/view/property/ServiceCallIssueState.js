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
                        valueField: 'id',
                        displayField: 'name',
                        store: me.getServiceCallStateStore(),
                        editable: false,
                        multiSelect: true,
                        msgTarget: 'under',
                        required: me.property.get('required'),
                        emptyText: Uni.I18n.translate('serviceCall.state.empty', 'UNI', 'Select a state...')
                    }
                ]
            }
        ];
    },

    setLocalizedName: function(name) {
    },

    getField: function () {
        return this.down('uni-grid-filtertop-combobox');
    },

    setValue: function (value) {
        if (this.isEdit) {
            if (this.getProperty().get('hasValue') && !this.userHasViewPrivilege && this.userHasEditPrivilege) {
                this.getField().emptyText = Uni.I18n.translate('general.valueProvided', 'UNI', 'Value provided - no rights to see the value.');
            } else {
                this.getField().emptyText = '';
            }
            this.getField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.id : value : null);
        } else {
            if (this.getProperty().get('hasValue')) {
                this.getDisplayField().setValue('********');
            } else {
                this.getDisplayField().setValue(!Ext.isEmpty(value) ? Ext.isObject(value) ? value.name : value : '');
            }
        }
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
