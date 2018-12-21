/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.controller.Zones',{
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],
    views: [
        'Cfg.zones.view.Overview',
        'Cfg.zones.view.AddForm',
        'Cfg.zones.view.Grid',
        'Cfg.zones.view.ZonePreview',
        'Cfg.zones.view.ActionMenu'
    ],
    refs: [
        {ref: 'zoneGrid', selector: '#grd-zones'},
        {ref: 'zonePreviewForm', selector: '#zone-preview-form'},
        {ref: 'zonePreview', selector: '#zone-preview'},
        {ref: 'zoneAdd', selector: '#zones-add-form'},
        {ref: 'zonesOverview', selector: '#panel-zones-overview'}
    ],
    stores: [
        'Cfg.zones.store.Zones',
        'Cfg.zones.store.ZoneTypes'
    ],
    models: [
        'Cfg.zones.model.Zone'
    ],

    init: function () {
        this.control({
            'zones-grid': {
                selectionchange: this.showPreview
            },
            'zones-grid #zones-add-button': {
                click: this.showAddZone
            },
            'zones-overview #empty-zones-add-button': {
                click: this.showAddZone
            },
            '#zones-add-form #btn-add-zone': {
                click: this.addZone
            },
            '#zones-action-menu': {
                click: this.chooseAction
            },
            '#zones-preview-action-menu': {
                click: this.chooseAction
            }
        });
    },

    showOverview: function () {
         var me = this,
             widget = Ext.widget('zones-overview', {
                router: me.getController('Uni.controller.history.Router')
            });
       me.getApplication().fireEvent('changecontentevent', widget);
    },

    showAddZone: function () {
        window.location.href = '#/administration/zones/add';
        var me = this,
            widget = Ext.widget('zone-add',{
                edit: false,
                title: Uni.I18n.translate('zones.addZone', 'CFG', 'Add zone'),
                router: me.getController('Uni.controller.history.Router'),
                cancelLink : '#/administration/zones'
            });

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function() {
        var zones = this.getZoneGrid().getSelectionModel().getSelection();
        if (zones.length == 1) {
            this.getZonePreviewForm().loadRecord(zones[0]);
            this.getZonePreview().setTitle(Ext.String.htmlEncode(zones[0].get('name')));
            Ext.resumeLayouts(true);
        }
    },

    editZone: function (zoneId) {
        var me = this;

        me.getModel('Cfg.zones.model.Zone').load(zoneId, {

            success: function (record) {
                widget =  Ext.create('Cfg.zones.view.AddForm', {
                    edit: true,
                    title: Uni.I18n.translate('cfg.zones.editZone', 'CFG', 'Edit {0}', record.get('name'), false),
                    router: me.getController('Uni.controller.history.Router'),
                    cancelLink : '#/administration/zones'
                });
                widget.down('#zones-add-form').loadRecord(record);
                me.getApplication().fireEvent('changecontentevent', widget);
            }
        });
    },

    removeZone: function (record) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            router = me.getController('Uni.controller.history.Router'),
            form =  this.getZonesOverview();

        form.setLoading(true);
        confirmationWindow.show({
            msg: Uni.I18n.translate('zone.general.remove.msg', 'CFG', 'This zone will no longer be available.'),
            title: Uni.I18n.translate('zone.remove', 'CFG', "Remove '{0}'?", [record.data.name]),
            config: {},
            fn: function (state) {
                if (state === 'confirm') {
                    record.destroy({
                        success: function () {
                            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('zone.remove.success.msg', 'CFG', 'Zone removed'));
                            router.getRoute('administration/zones').forward();
                        },
                        callback: function () {
                            form.setLoading(false);
                        }
                    });
                } else if (state === 'cancel') {
                    this.close();
                }
            }
        });
    },

    addZone: function () {
        var me = this,
            form = this.getZoneAdd(),
            formErrorsPanel = form.down('#form-errors');

        var record = form.getRecord() || new Cfg.zones.model.Zone;

        form.getForm().clearInvalid();
        record.beginEdit();
        if (!formErrorsPanel.isHidden()) {
            formErrorsPanel.hide();
        }
        record.set('name', form.down('#zone-name').getValue());
        record.set('zoneTypeName', form.down('#zone-type').getValue());

       form.setLoading(true);
       record.endEdit();
       record.save({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute('administration/zones').forward();
            },
            failure: function (record, operation) {
                if (operation.response.status == 400) {
                    var json = Ext.decode(operation.response.responseText, true);
                    if (json && json.errors) {
                        form.getForm().markInvalid(json.errors);
                        formErrorsPanel.show();
                    }
                }
            },
            callback: function () {
               form.setLoading(false);
            }
        })

    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        var zones = this.getZoneGrid().getSelectionModel().getSelection();
        router.arguments.id = zones[0].get('id');

        switch (item.action) {
            case 'editZone':
                router.getRoute('administration/zones/edit').forward({zoneId: router.arguments.id});
                break;
            case 'deleteZone':
                me.removeZone(zones[0]);
                break;
        }
    },

});