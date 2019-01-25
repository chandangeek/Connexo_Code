/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.controller.Audit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.audit.view.AuditSetup',
        'Mdc.store.TimeUnits'
    ],

    views: [
        'Mdc.audit.view.AuditSetup'
    ],

    stores: [
        'Mdc.audit.store.Audit',
        'Mdc.audit.store.AuditDetails',
        'Mdc.store.TimeUnits'
    ],

    refs: [
        {ref: 'auditGrid', selector: 'auditSetup #audit-grid'},
        {ref: 'auditPreview', selector: 'auditSetup #audit-preview'},
        {ref: 'auditPreviewGrid', selector: '#audit-preview #audit-preview-grid'},
        {ref: 'auditPreviewNoItem', selector: '#audit-preview #audit-preview-no-items'}
    ],

    init: function () {
        this.control({
            'auditSetup #audit-grid': {
                selectionchange: this.showPreview
            }
        })
    },

    showOverview: function () {
        var me = this,
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            widget = Ext.widget('auditSetup', {
                convertorFn: me.valueConvertor,
                domainConvertorFn: me.domainConvertor,
                contextConvertorFn: me.contextConvertor,
                scopeFn: me
            });

        timeUnitsStore.load({
            callback: function () {
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            auditGrid = me.getAuditGrid(),
            auditPreview = me.getAuditPreview(),
            auditPreviewGrid = me.getAuditPreviewGrid(),
            auditPreviewNoItems = me.getAuditPreviewNoItem();

        if (record[0].auditLogsStore.getCount() > 0) {
            auditPreviewGrid.setVisible(true);
            auditPreviewGrid.getStore().loadRawData(record[0].raw['auditLogs']);
            auditPreviewGrid.getSelectionModel().select(0);
            auditPreviewNoItems.setVisible(false);
        }
        else {
            auditPreviewGrid.setVisible(false);
            auditPreviewNoItems.setVisible(true);
        }
        auditGrid.getView().focus();
        auditPreview.setLoading(false);
    },

    valueConvertor: function (value, record) {
        var me = this,
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            propertyType = record.get('type'),
            displayValue = value;

        switch (propertyType) {
            case 'DURATION':
                displayValue = value.split(':')[1] + ' ' + timeUnitsStore.findRecord('timeUnit', value.split(':')[0]).get('localizedValue');
                break;
            case 'LOCATION':
                displayValue = Ext.isEmpty(value) == false ? Ext.String.htmlEncode(value).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>') : '';
                break;
            case 'TIMESTAMP':
                displayValue = value ? Uni.DateTime.formatDateTimeShort(value) : '';
                break;
            case 'BOOLEAN':
                displayValue = value ?
                    Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                    Uni.I18n.translate('general.no', 'MDC', 'No');
                break;
        }
        return ((displayValue != null) && (displayValue.length == 0)) ? '-' : displayValue;
    },

    domainConvertor: function (value, record) {
        var me = this,
            domainType = record.get('domainType'),
            rendererLink;

        switch (domainType) {
            case 'DEVICE':
                rendererLink = '<a href="#/devices/' + record.get('auditReference').name + '">' + record.get('auditReference').name + '</a>';
                break;
            default:
                rendererLink = record.get('auditReference').name;
        }
        return rendererLink;
    },

    contextConvertor: function (value, record) {
        var me = this,
            contextType = record.get('contextType'),
            rendererLink;

        switch (contextType) {
            case 'GENERAL_ATTRIBUTES':
                rendererLink = '<a href="#/devices/' + record.get('auditReference').name + '/generalattributes">' + value + '</a>';
                break;
            case 'DEVICE_ATTRIBUTES':
                rendererLink = '<a href="#/devices/' + record.get('auditReference').name + '/attributes">' + value + '</a>';
                break;
            default:
                rendererLink = record.get('auditReference').name;
        }
        return rendererLink;
    }
});
