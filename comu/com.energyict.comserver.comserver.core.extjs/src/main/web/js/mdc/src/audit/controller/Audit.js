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
        'Mdc.store.TimeUnits'
    ],

    refs: [
        {ref: 'auditPage', selector: 'auditSetup'}
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
                convertorFn: me.convertor,
                scope: me
            });

        timeUnitsStore.load({
            callback: function () {
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });

    },

    showPreview: function (selectionModel, record) {
        var me = this,
            page = me.getAuditPage(),
            auditPreviewGrid = page.down('#audit-preview-grid');

        Ext.suspendLayouts();
        Ext.each(auditPreviewGrid.columns, function (column) {
            if (column.dataIndex == 'previousValue') {
                column.setVisible(record[0].get('operation') == 'UPDATE');
                return;
            }
        });

        auditPreviewGrid.reconfigure(record[0].auditLogsStore);
        Ext.resumeLayouts(true);
    },

    convertor: function (value, record) {
        var me = this,
            timeUnitsStore = me.getStore('Mdc.store.TimeUnits'),
            propertyType = record.get('type'),
            displayValue = value;

        switch (propertyType) {
            case 'DURATION':
                displayValue = value.split(':')[1] + ' ' + timeUnitsStore.findRecord('timeUnit', value.split(':')[0]).get('localizedValue');
                break;
            case 'BOOLEAN':
                displayValue = value ?
                    Uni.I18n.translate('general.yes', 'MDC', 'Yes') :
                    Uni.I18n.translate('general.no', 'MDC', 'No');
                break;
        }
        return displayValue;
    }

});
