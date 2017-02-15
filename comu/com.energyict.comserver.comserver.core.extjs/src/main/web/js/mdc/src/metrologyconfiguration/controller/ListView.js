/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.metrologyconfiguration.controller.ListView', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    stores: [
        'Mdc.metrologyconfiguration.store.MetrologyConfigurations'
    ],

    views: [
        'Mdc.metrologyconfiguration.view.ListView',
        'Uni.view.window.Confirmation'
    ],

    refs: [
        {ref: 'page', selector: '#metrology-configurations-list-view'},
        {ref: 'preview', selector: '#metrology-configurations-list-view #metrology-configuration-preview'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#metrology-configurations-list-view #metrology-configurations-grid': {
                select: me.showPreview
            },
            '#metrology-configuration-actions-menu': {
                click: me.chooseAction
            }
        });
    },

    showList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('metrology-configurations-list-view', {
                itemId: 'metrology-configurations-list-view',
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            menu = preview.down('#metrology-configuration-actions-menu');

        Ext.suspendLayouts();
        preview.setTitle(Ext.htmlEncode(record.get('name')));
        preview.loadRecord(record);
        if (menu) {
            menu.record = record;
        }
        Ext.resumeLayouts(true);
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            record = menu.record;

        switch (item.action) {
            case 'remove':
                Ext.create('Uni.view.window.Confirmation').show({
                    msg: Uni.I18n.translate('metrologyconfiguration.remove.confirmation', 'MDC', 'You will not be able to configure usage points with this metrology configuration.'),
                    title: Uni.I18n.translate('general.removeConfirmation', 'MDC', 'Remove \'{0}\'?', [record.get('name')]),
                    closeAction: 'destroy',
                    fn: function (action) {
                        if (action === 'confirm') {
                            me.remove(record);
                        }
                    }
                });
                break;
            case 'edit':
                router.getRoute('administration/metrologyconfiguration/edit').forward({metrologyConfigurationId: record.getId()});
                break;
            case 'toggleActivation':
                me.toggleActivation(record);
                break;
        }
    },

    remove: function (record) {
        var me = this,
            page = me.getPage();

        page.setLoading();
        record.destroy({
            success: function () {
                me.getController('Uni.controller.history.Router').getRoute().forward();
                me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.remove.acknowlegment', 'MDC', 'Metrology configuration removed'));
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    },

    toggleActivation: function (record) {
        var me = this,
            page = me.getPage(),
            isActive = record.get('status').id === 'active';

        record.set('status', isActive
            ? {id: 'inactive', name: Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')}
            : {id: 'active', name: Uni.I18n.translate('general.active', 'MDC', 'Active')});
        page.setLoading();
        record.save({
            isNotEdit: true,
            success: function () {
                me.getApplication().fireEvent('acknowledge', isActive
                    ? Uni.I18n.translate('metrologyconfiguration.deactivateMetrologyConfigurationSuccess', 'MDC', 'Metrology configuration deactivated')
                    : Uni.I18n.translate('metrologyconfiguration.activateMetrologyConfigurationSuccess', 'MDC', 'Metrology configuration activated'));
            },
            failure: function () {
                record.reject();
            },
            callback: function () {
                page.setLoading(false);
            }
        });
    }
});