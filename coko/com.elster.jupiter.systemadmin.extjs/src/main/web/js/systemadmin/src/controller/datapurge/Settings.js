/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sam.controller.datapurge.Settings', {
    extend: 'Ext.app.Controller',

    stores: [
        'Sam.store.DataPurgeSettings'
    ],

    views: [
        'Sam.view.datapurge.SettingsOverview'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'data-purge-settings-overview'
        }
    ],

    init: function () {
        this.control({
            'data-purge-settings-overview #data-purge-settings-grid': {
                edit: this.onCellEdit
            },
            'data-purge-settings-overview #data-purge-settings-save-button': {
                click: this.saveData
            }
        });
    },

    showOverview: function () {
        var me = this;

        me.getApplication().fireEvent('changecontentevent', Ext.widget('data-purge-settings-overview', {
            router: me.getController('Uni.controller.history.Router')
        }));
        me.getStore('Sam.store.DataPurgeSettings').load();
    },

    onCellEdit: function (editor, e) {
        !e.value && e.record.set('retainedPartitionCount', 999);
        e.record.set('retention', e.record.get('retainedPartitionCount') * 30);
    },

    saveData: function () {
        var me = this,
            page = me.getPage(),
            store = me.getStore('Sam.store.DataPurgeSettings'),
            updatedRecords = store.getUpdatedRecords(),
            updatedData = {data: []};

        if (!updatedRecords.length) {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('datapurge.settings.nothingToSaveMsg', 'SAM', 'Nothing to save'));
            return;
        }

        Ext.Array.each(updatedRecords, function (record) {
            updatedData.data.push(record.getRecordData());
        });

        page.setLoading(Uni.I18n.translate('general.saving', 'SAM', 'Saving...'));
        Ext.Ajax.request({
            url: store.getProxy().url,
            jsonData: updatedData,
            method: 'PUT',
            callback: function () {
                page.setLoading(false);
            },
            success: function () {
                store.reload();
               // store.commitChanges();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('datapurge.settings.success.msg', 'SAM', 'Data purge settings saved'));
            }
        });
    }
});