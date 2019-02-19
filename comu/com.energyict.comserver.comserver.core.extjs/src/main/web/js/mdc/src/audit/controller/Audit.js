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
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits');

        timeUnitsStore.load({
            callback: function () {
                var widget = Ext.widget('auditSetup', {
                    convertorFn: me.valueConvertor,
                    domainConvertorFn: me.domainConvertor,
                    contextConvertorFn: me.contextConvertor,
                    scopeFn: me
                });
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

        Ext.each(auditPreviewGrid.columns, function (column) {
            if (column.dataIndex === 'previousValue') {
                column.setVisible(record[0].get('operationType') == 'UPDATE');
            }
            if (column.dataIndex === 'value') {
                column.setText(record[0].get('operationType') == 'UPDATE' ? Uni.I18n.translate('audit.preview.changedTo', 'MDC', 'Changed to') :
                    Uni.I18n.translate('audit.preview.value', 'MDC', 'Value'));
            }

        });

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
            isRemoved = record.get('auditReference').removed,
            rendererLink;

        switch (domainType) {
            case 'DEVICE':
                rendererLink = isRemoved == true ? record.get('auditReference').name : '<a href="#/devices/' + record.get('auditReference').name + '">' + record.get('auditReference').name + '</a>';
                break;
            default:
                rendererLink = record.get('auditReference').name;
        }
        return ((rendererLink != null) && (rendererLink.length == 0)) ? '-' : rendererLink;
    },

    contextConvertor: function (value, record) {
        var me = this,
            contextType = record.get('contextType'),
            isRemoved = record.get('auditReference').removed,
            formatValue,
            rendererLink;

        switch (contextType) {
            case 'GENERAL_ATTRIBUTES':
                rendererLink = isRemoved == true ? value : '<a href="#/devices/' + record.get('auditReference').name + '/generalattributes">' + value + '</a>';
                break;
            case 'DEVICE_ATTRIBUTES':
                rendererLink = isRemoved == true ? value : '<a href="#/devices/' + record.get('auditReference').name + '/attributes">' + value + '</a>';
                break;
            case 'DEVICE_CUSTOM_ATTRIBUTES':
                rendererLink = isRemoved == true ? me.formatDeviceCustomAttributeContext(record, value) : '<a href="#/devices/' + record.get('auditReference').name + '/attributes">' + me.formatDeviceCustomAttributeContext(record, value) + '</a>';
                break;
            case 'DEVICE_CHANNEL_CUSTOM_ATTRIBUTES':
                rendererLink = isRemoved == true ? me.formatChannelCustomAttributeContext(record, value) : me.formatChannelHRef(record) + me.formatChannelCustomAttributeContext(record, value) + '</a>';
                break;
            case 'DEVICE_REGISTER_CUSTOM_ATTRIBUTES':
                rendererLink = isRemoved == true ? me.formatChannelCustomAttributeContext(record, value) : me.formatRegisterHRef(record) + me.formatChannelCustomAttributeContext(record, value) + '</a>';
                break;
            default:
                rendererLink = value;
        }
        return ((rendererLink != null) && (rendererLink.length == 0)) ? '-' : rendererLink;
    },

    formatDeviceCustomAttributeContext: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference,
            periodStr = '';

        if (me.isEmptyOrNull(contextReference)) {
            return value;
        }

        if (contextReference.isVersioned !== true) {
            return Ext.String.format("{0} -> {1}", value, record.get('auditReference').contextReference.name);
        }

        if (contextReference.startTime) {
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'MDC', 'From'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.startTime)));
        }
        if (contextReference.startTime && contextReference.endTime) {
            periodStr += ' - ';
        }
        if (contextReference.endTime) {
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'MDC', 'Until'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.endTime)));
        }
        if (!contextReference.endTime && !contextReference.startTime) {
            periodStr += Uni.I18n.translate('general.infinite', 'MDC', 'Infinite');
        }
        return Ext.String.format("{0} -> {1} ({2})", value, record.get('auditReference').contextReference.name, periodStr);
    },

    formatChannelCustomAttributeContext: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference,
            periodStr = '';

        if (me.isEmptyOrNull(contextReference)) {
            return value;
        }

        if (contextReference.isVersioned !== true) {
            return Ext.String.format("{0} -> {1}", value, record.get('auditReference').contextReference.name);
        }

        if (contextReference.startTime) {
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'MDC', 'From'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.startTime)));
        }
        if (contextReference.startTime && contextReference.endTime) {
            periodStr += ' - ';
        }
        if (contextReference.endTime) {
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'MDC', 'Until'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.endTime)));
        }
        if (!contextReference.endTime && !contextReference.startTime) {
            periodStr += Uni.I18n.translate('general.infinite', 'MDC', 'Infinite');
        }
        return Ext.String.format("{0} -> {1} -> {2} ({3})", value, record.get('auditReference').contextReference.sourceName, record.get('auditReference').contextReference.name, periodStr);
    },

    formatChannelHRef: function (record) {
        var me = this,
            contextReference = record.get('auditReference').contextReference;

        return '<a href="#/devices/' + record.get('auditReference').name + '/channels' + '/'+ contextReference.sourceId +  '">'
    },

    formatRegisterHRef: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference;

        return '<a href="#/devices/' + record.get('auditReference').name + '/registers' + '/'+ contextReference.sourceId +  '">'
    },

    isEmptyOrNull: function (value) {
        return ((value != null) && (value.length == 0))
    }
});
