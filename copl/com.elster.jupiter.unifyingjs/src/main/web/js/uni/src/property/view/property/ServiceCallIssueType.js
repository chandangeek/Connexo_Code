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
                        name: this.getName(),
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
                        required: me.property.get('required')
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
