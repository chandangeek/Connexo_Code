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
            preview.down('#tou-campaigns-detail-action-menu-button').setVisible(record.get('status') === 'Ongoing');
        }
    },

    onActionMenuClicked: function (menu, item) {
        switch (item.action) {
            case 'cancelCampaign': this.onCancelCampaign(menu.record);
                break;
            case 'editCampaign':
            case 'editCampaignAndReturnToOverview':
                this.returnToOverview = item.action === 'editCampaignAndReturnToOverview';
                location.href = '#/workspace/toucampaigns/' + encodeURIComponent(menu.record.get('name')) + '/edit';
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
            msg: 'Cancel time of use calendar upload for all scheduled devices. Ongoing uploads will not be terminated and successful uploads will not be reversed.',
            title: 'Cancel ToU campaign?'
        });
    },

    doCancelCampaign : function(record) {
        var me = this,
            store = this.getStore('Tou.store.TouCampaigns');

        //store.getProxy().url = '/api/tou/touCampaigns/' + record.id + '/cancel';
        //record.getProxy().url = '/api/tou/touCampaigns/' + record.id + '/cancel';
        //debugger;
        Ext.Ajax.request({
        	   	url: '/api/tou/touCampaigns/' + record.data.name + '/cancel',
        	   	method: 'PUT',
        	      	params: {
        			},
        	   	success: function(transport){
                    me.getApplication().fireEvent('acknowledge', 'Cancelling of the campaign has started and will continue in the background');
                    //record.set('status', {id: "CANCELLED", localizedValue: "Cancelled"});
                    //me.showPreview('', record);
        	   	},
        	   	failure: function(transport){
        	   	    console.log("Tou campaign not cancelled");
        	   		//record.reject();
        	   	}
        });
       /* record.save({
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
        });*/
    },

    editCampaign: function(campaignName) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('tou-campaigns-add', {
                itemId: 'tou-campaigns-edit',
                action: 'saveTouCampaign',
                returnLink: me.returnToOverview
                    ? router.getRoute('workspace/toucampaigns/toucampaign').buildUrl({firmwareCampaignId : campaignIdAsString})
                    : router.getRoute('workspace/toucampaigns').buildUrl()
            }),
            dependencies = ['Tou.store.DeviceTypes'],
            dependenciesCounter = dependencies.length,
            onDependenciesLoaded = function () {
                dependenciesCounter--;
                if (!dependenciesCounter) {
                    me.loadModelToEditForm(campaignIdAsString, widget);
                }
            };

        me.getApplication().fireEvent('changecontentevent', widget);
        widget.down('#tou-campaigns-add-form').setLoading(true);
        widget.down('#btn-add-tou-campaign').setText('Save');
        Ext.Array.each(dependencies, function (store) {
            me.getStore(store).load(onDependenciesLoaded);
        });
    },

    loadModelToEditForm: function(campaignIdAsString, widget) {

    }

});