/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.controller.Overview', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Tou.view.Overview',
        'Tou.view.Add'
    ],

    stores: [
        'Tou.store.TouCampaigns',
        'Tou.store.DeviceTypes',
        'Fwc.store.DeviceGroups'
    ],

    refs: [
        {
            ref: 'preview',
            selector: 'tou-campaigns-detail-form'
        }
    ],
    returnToOverview : false,

    init: function () {
        this.control({
            'tou-campaigns-overview tou-campaigns-grid': {
                select: this.showPreview
            },
            '#tou-campaigns-action-menu': {
                click: this.onActionMenuClicked
            }
        });
    },

    showOverview: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('tou-campaigns-overview', {
                itemId: 'tou-campaigns-overview',
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var preview = this.getPreview();

        Ext.suspendLayouts();
        preview.loadRecord(record);
        preview.setTitle(Ext.String.htmlEncode(record.get('name')));
        Ext.resumeLayouts(true);
        if (preview.down('tou-campaigns-action-menu')) {
            preview.down('tou-campaigns-action-menu').record = record;
           // preview.down('#tou-campaigns-detail-action-menu-button').setVisible(record.get('status').id === 'ONGOING');
        }
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'cancelCampaign': this.onCancelCampaign(menu.record);
                break;
            case 'editCampaign':
            case 'editCampaignAndReturnToOverview':
                break;
        }
    },

    onCancelCampaign : function(record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                confirmText: 'Cancel campaign',
                confirmation: function () {
                    me.doCancelCampaign(record);
                    this.close();
                }
            });

        confirmationWindow.show({
            msg: 'Cancel the tou upload for all scheduled devices. Ongoing uploads will not be terminated and successful uploads will not be reversed.',
            title: 'Cancel tou campaign?'
        });
    },

    doCancelCampaign : function(record) {
        var me = this,
            form = this.getPreview().down('form'),
            store = this.getStore('Tou.store.TouCampaigns');

        store.getProxy().url = '/api/tou/touCampaigns/' + record.id + '/cancel';
        record.save({
            isNotEdit: true,
            success: function () {
                form.loadRecord(record);
                me.getApplication().fireEvent('acknowledge', 'Tou campaign cancelled');
                me.showPreview('', record);
            },
            callback: function () {
                store.getProxy().url = '/api/tou/touCampaigns';
            },
            failure: function () {
                record.reject();
            }
        });
    },

    editCampaign: function(campaignIdAsString) {
    },

    loadModelToEditForm: function(campaignIdAsString, widget) {

    }

});