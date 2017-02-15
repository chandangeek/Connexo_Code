/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.controller.DataQualityKpiOverview', {
    extend: 'Ext.app.Controller',

    stores: [
        'Cfg.insight.dataqualitykpi.store.DataQualityKpis'
    ],

    models: [
        'Cfg.insight.dataqualitykpi.model.DataQualityKpi'
    ],

    views: [
        'Cfg.insight.dataqualitykpi.view.Overview',
        'Uni.view.window.Confirmation'
    ],

    refs: [
        {ref: 'preview', selector: '#ins-data-quality-kpi-overview #data-quality-kpi-preview'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#ins-data-quality-kpi-overview #data-quality-kpis-grid': {
                select: me.showPreview
            },
            '#ins-data-quality-kpi-overview #data-quality-kpis-grid uni-actioncolumn-remove': {
                remove: me.onRemoveDataQualityKpi
            },
            '#ins-data-quality-kpi-overview #ins-data-quality-kpi-action-menu': {
                click: me.chooseAction
            }
        });
    },

    showDataQualityKPIs: function () {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router');

        app.fireEvent('changecontentevent', Ext.widget('ins-data-quality-kpi-overview', {
            itemId: 'ins-data-quality-kpi-overview',
            router: router
        }));
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview(),
            menu = preview.down('#ins-data-quality-kpi-action-menu');

        Ext.suspendLayouts();
        preview.setTitle(record.get('usagePointGroup'));
        preview.loadRecord(record);
        Ext.resumeLayouts(true);

        if (menu) {
            menu.record = record;
        }
    },

    chooseAction: function (menu, menuItem) {
        var me = this;

        switch (menuItem.action) {
            case 'remove':
                me.onRemoveDataQualityKpi(menu.record);
                break;
        }
    },

    onRemoveDataQualityKpi: function (record) {
        var me = this;

        Ext.create('Uni.view.window.Confirmation').show({
            title: Uni.I18n.translate('general.removex.kpi', 'CFG', "Remove '{0}'?", record.get('usagePointGroup')),
            msg: Uni.I18n.translate('dataqualitykpis.deleteConfirmation.msg', 'CFG', 'This data quality KPI will no longer be available in the system. Already calculated data will not be removed.'),
            fn: confirm
        });

        function confirm(state) {
            if (state === 'confirm') {
                me.removeDataQualityKpi(record);
            }
        }
    },

    removeDataQualityKpi: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        record.destroy({
            isNotEdit: true,
            success: onSuccessRemove,
            callback: removeCallback
        });

        function onSuccessRemove() {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('dataqualitykpis.kpiRemoved', 'CFG', 'Data quality KPI scheduled for removal'));
            router.getRoute().forward();
        }

        function removeCallback() {
            mainView.setLoading(false);
        }
    }
});