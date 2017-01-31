/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.datapurge.Log', {
    extend: 'Ext.app.Controller',

    models: [
        'Sam.model.DataPurgeHistory',
        'Uni.component.sort.model.Sort'
    ],

    stores: [
        'Sam.store.DataPurgeLog'
    ],

    views: [
        'Sam.view.datapurge.LogOverview'
    ],

    refs: [
        {
            ref: 'sortingToolbar',
            selector: 'data-purge-log-overview #data-purge-log-sorting-toolbar'
        },
        {
            ref: 'logForm',
            selector: 'data-purge-log-overview #data-purge-log-form'
        }
    ],

    showOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        me.getStore('Sam.store.DataPurgeLog').getProxy().setUrl(router.arguments.historyId);
        me.getApplication().fireEvent('changecontentevent', Ext.widget('data-purge-log-overview'));
        me.getModel('Sam.model.DataPurgeHistory').load(router.arguments.historyId, {
            success: function (record) {
                if (!me.getLogForm().isDestroyed) {
                    me.getLogForm().loadRecord(record);
                }
            }
        });
        me.getSortingToolbar().getContainer().add({
            xtype: 'button',
            ui: 'tag',
            text: Uni.I18n.translate('datapurge.log.timestamp', 'SAM', 'Timestamp'),
            iconCls: 'x-btn-sort-item-desc'
        });
        me.getSortingToolbar().getClearButton().disable();
        me.getSortingToolbar().on('afterlayout', function () {
            this.getClearButton().disable();
        });
    }
});