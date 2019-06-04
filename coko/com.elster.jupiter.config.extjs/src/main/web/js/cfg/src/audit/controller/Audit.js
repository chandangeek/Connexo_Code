/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.controller.Audit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Cfg.audit.view.AuditSetup',
        'Uni.property.store.TimeUnits'
    ],

    views: [
        'Cfg.audit.view.AuditSetup'
    ],

    stores: [
        'Cfg.audit.store.Audit',
        'Cfg.audit.store.AuditDetails',
        'Uni.property.store.TimeUnits'
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

    getAuditTrailView: function (store) {
        var me = this;
        return  {
            xtype: 'audit-setup-view',
            convertorFn: me.valueConvertor,
            domainConvertorFn: me.domainConvertor,
            contextConvertorFn: me.contextConvertor,
            store: store,
            scopeFn: me
        };
    },

    loadDependencies: function(scope, callbackFn){
        var me = this,
            timeUnitsStore = me.getStore('Uni.property.store.TimeUnits');

        timeUnitsStore.load({
            callback: function () {
                callbackFn.call(scope);
            }
        });
    },

    showOverview: function () {
        var me = this,
            dependenciesLoaded = function () {
                var widget = Ext.widget('auditSetup', {
                    convertorFn: me.valueConvertor,
                    domainConvertorFn: me.domainConvertor,
                    contextConvertorFn: me.contextConvertor,
                    store: 'Cfg.audit.store.Audit',
                    scopeFn: me
                });
                me.getApplication().fireEvent('changecontentevent', widget);
        }

        me.loadDependencies(me, dependenciesLoaded);
    },

    showOverview_old: function () {
        var me = this,
            timeUnitsStore = me.getStore('Uni.property.store.TimeUnits');

        timeUnitsStore.load({
            callback: function () {
                var widget = Ext.widget('auditSetup', {
                    convertorFn: me.valueConvertor,
                    domainConvertorFn: me.domainConvertor,
                    contextConvertorFn: me.contextConvertor,
                    store: 'Cfg.audit.store.Audit',
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
            auditPreviewNoItems = me.getAuditPreviewNoItem(),
            isUpdateOperation = record[0].get('operationType') === 'UPDATE';

        auditPreview.suspendLayouts();
        if (record[0].auditLogsStore.getCount() > 0) {
            auditPreviewGrid.setVisible(true);
            auditPreviewGrid.getStore().loadRawData(record[0].raw['auditLogs']);
            auditPreviewGrid.getView() && auditPreviewGrid.getView().getEl() && auditPreviewGrid.getSelectionModel().select(0);
            auditPreviewNoItems.setVisible(false);

            Ext.each(auditPreviewGrid.columns, function (column) {
                if ((column.dataIndex === 'previousValue') && ((auditPreviewGrid.getView().getEl() == undefined || column.isVisible() != isUpdateOperation))){
                     column.setVisible(isUpdateOperation);
                }
                if (column.dataIndex === 'value') {
                    column.setText(isUpdateOperation ? Uni.I18n.translate('audit.preview.changedTo', 'CFG', 'Changed to') :
                        Uni.I18n.translate('audit.preview.value', 'CFG', 'Value'));
                }

            });
        }
        else {
            auditPreviewGrid.setVisible(false);
            auditPreviewNoItems.setVisible(true);
        }
        auditPreview.resumeLayouts();
        auditPreviewGrid.doLayout();

        auditGrid.getView().focus();
        auditPreview.setLoading(false);
        auditGrid.getView().focus();
        auditPreview.setLoading(false);
    },

    valueConvertor: function (value, record) {
        var me = this,
            timeUnitsStore = me.getStore('Uni.property.store.TimeUnits'),
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
                    Uni.I18n.translate('general.yes', 'CFG', 'Yes') :
                    Uni.I18n.translate('general.no', 'CFG', 'No');
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
            case 'DEVICE_DATA_SOURCE_SPECIFICATIONS':
                rendererLink = isRemoved == true ? me.formatDeviceDataSourceContext(record, value) : me.formatDeviceDataSourceHRef(record) + me.formatDeviceDataSourceContext(record, value) + '</a>';
                break;
            case 'DEVICE_PROTOCOL_DIALECTS_PROPS':
                rendererLink = isRemoved == true ? me.formatOnlyEntityContext(record, value) : me.formatProtocolDialectsHRef(record, value) + '</a>';
                break;
            case 'DEVICE_CONNECTION_METHODS':
                rendererLink = isRemoved == true ? me.formatEntityWithNameContext(record, value) : me.formatConnectionMethodsHRef(record, value) + '</a>';
                break;
            case 'DEVICE_COMTASKS':
                rendererLink = isRemoved == true ? me.formatEntityWithNameContext(record, value) : me.formatComTasksHRef(record, value) + '</a>';;
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
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'CFG', 'From'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.startTime)));
        }
        if (contextReference.startTime && contextReference.endTime) {
            periodStr += ' - ';
        }
        if (contextReference.endTime) {
            periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'CFG', 'Until'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.endTime)));
        }
        if (!contextReference.endTime && !contextReference.startTime) {
            periodStr += Uni.I18n.translate('general.infinite', 'CFG', 'Infinite');
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
        periodStr =  this.extractPeriod(contextReference);
        return Ext.String.format("{0} -> {1} -> {2} ({3})", value, record.get('auditReference').contextReference.sourceName, record.get('auditReference').contextReference.name, periodStr);
    },

    formatDeviceDataSourceContext: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference;

        if (!me.isEmptyOrNull(record.get('auditReference').contextReference.sourceTypeName) && !me.isEmptyOrNull(record.get('auditReference').contextReference.sourceName)){
            return Ext.String.format("{0} -> {1}", record.get('auditReference').contextReference.sourceTypeName, record.get('auditReference').contextReference.sourceName);
        }
        else if (!me.isEmptyOrNull(record.get('auditReference').contextReference.sourceTypeName)){
            return record.get('auditReference').contextReference.sourceTypeName;
        }
        else if (!me.isEmptyOrNull(record.get('auditReference').contextReference.sourceName)){
            return record.get('auditReference').contextReference.sourceName;
        }
        return '';
    },

    formatOnlyEntityContext: function (record, value) {
        return Ext.String.format("{0}", value);
    },

    formatEntityWithNameContext: function (record, value) {
        if(this.isEmptyOrNull(record.get('auditReference').contextReference.name))
            return Ext.String.format("{0}", value);
        return Ext.String.format("{0} -> {1}", value, record.get('auditReference').contextReference.name);
    },

    formatDeviceDataSourceHRef: function (record) {
        var me = this,
            contextReference = record.get('auditReference').contextReference,
            sourceType = record.get('auditReference').contextReference.sourceType;

        if (me.isEmptyOrNull(record.get('auditReference').name) || me.isEmptyOrNull(contextReference.sourceId)){
            return '';
        }
        if (sourceType === 'CHANNEL'){
            return '<a href="#/devices/' + record.get('auditReference').name + '/channels' + '/'+ contextReference.sourceId +  '">'
        }
        else if (sourceType === 'REGISTER'){
            return '<a href="#/devices/' + record.get('auditReference').name + '/registers' + '/'+ contextReference.sourceId +  '">'
        }
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

    formatProtocolDialectsHRef: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference,
            periodStr = this.extractPeriod(contextReference);

        return '<a href="#/devices/' + record.get('auditReference').name + '/protocols">' +  Ext.String.format("{0} -> {1} ({2})", value, contextReference.name, periodStr);
    },

    formatConnectionMethodsHRef: function (record, value) {
        var me = this,
            contextReference = record.get('auditReference').contextReference;

        return '<a href="#/devices/' + record.get('auditReference').name + '/connectionmethods">' + me.formatEntityWithNameContext(record, value);
    },

    formatComTasksHRef: function (record, value) {
        var me = this;

        return '<a href="#/devices/' + record.get('auditReference').name + '/communicationtasks">' +  me.formatEntityWithNameContext(record, value);
    },

    extractPeriod: function(contextReference){
            var periodStr = '';

            if (contextReference.startTime) {
                periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.from', 'CFG', 'From'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.startTime)));
            }
            if (contextReference.startTime && contextReference.endTime) {
                periodStr += ' - ';
            }
            if (contextReference.endTime) {
                periodStr += Ext.String.format("{0} {1}", Uni.I18n.translate('general.until', 'CFG', 'Until'), Uni.DateTime.formatDateTimeShort(new Date(contextReference.endTime)));
            }
            if (!contextReference.endTime && !contextReference.startTime) {
                periodStr += Uni.I18n.translate('general.infinite', 'CFG', 'Infinite');
            }
            return periodStr;
    },

    isEmptyOrNull: function (value) {
        return (value == undefined) ||
            (value == null) ||
            ((value != null) && (value.length == 0));
    },

    prepareForSpecificObject: function(view){
        var me = this;

        view.down('#audit-trail-content').setTitle('');
        view.down('#audit-filter').down('#audit-filter-category-combo').setVisible(false);
        Ext.each(view.down('#audit-grid').columns, function (column) {
            if ((column.dataIndex === 'domain') || (column.dataIndex === 'auditReference')) {
                column.setVisible(false);
            }
        });
    }
});
