/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dashboard.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.dashboard.store.FlaggedUsagePoints',
        'Imt.dashboard.store.FlaggedUsagePointGroups'
    ],

    models: [],
    stores: [
        'Imt.dashboard.store.FlaggedUsagePoints',
        'Imt.dashboard.store.FlaggedUsagePointGroups'
    ],
    views: [
        'Imt.dashboard.view.OperatorDashboard',
        'Imt.dashboard.view.widget.FlaggedItems',
        'Imt.dashboard.view.widget.FlaggedUsagePoints',
        'Imt.dashboard.view.widget.FlaggedUsagePointGroups'
    ],

    refs: [
        {ref: 'dashboard', selector: '#operator-dashboard'},
        {ref: 'header', selector: 'operator-dashboard #header-section'}
    ],

    init: function () {
        this.control({
            '#operator-dashboard #refresh-btn': {
                click: this.loadData
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('operator-dashboard', {
            router: router,
            itemId: 'operator-dashboard'
        }));
        this.loadData();
    },

    loadData: function () {
        var me = this,
            dashboard = me.getDashboard(),
            lastUpdateField = dashboard.down('#last-updated-field'),
            myWorkList = dashboard.down('#my-work-list'),
            flaggedUsagePoints = dashboard.down('#flagged-usage-points'),
            flaggedUsagePointGroups = dashboard.down('#flagged-usage-point-groups');

        if (myWorkList) {
            myWorkList.reload();
        }

        if (flaggedUsagePoints) {
            flaggedUsagePoints.reload();
        }

        if (flaggedUsagePointGroups) {
            flaggedUsagePointGroups.reload();
        }
        lastUpdateField.update(Uni.I18n.translate('general.lastUpdatedAt', 'IMT', 'Last updated at {0}', [Uni.DateTime.formatTimeShort(new Date())]));
    }
});
