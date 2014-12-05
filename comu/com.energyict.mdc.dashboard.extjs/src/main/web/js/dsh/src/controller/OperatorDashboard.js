Ext.define('Dsh.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview',
        'Dsh.model.opendatacollectionissues.Overview'
    ],
    stores: [
        'CommunicationServerInfos',
        'Dsh.store.CombineStore',
        'Dsh.store.ConnectionResultsStore',
        'Dsh.store.FavoriteDeviceGroups',
        'Dsh.store.FlaggedDevices'
    ],
    views: [ 'Dsh.view.OperatorDashboard' ],

    refs: [
        { ref: 'dashboard', selector: '#operator-dashboard' },
        { ref: 'header', selector: 'operator-dashboard #header-section' },
        { ref: 'connectionSummary', selector: 'operator-dashboard #connection-summary' },
        { ref: 'communicationSummary', selector: 'operator-dashboard #communication-summary' },
        { ref: 'summary', selector: ' operator-dashboard #summary' },
        { ref: 'communicationServers', selector: 'operator-dashboard #communication-servers' },
        { ref: 'flaggedDevices', selector: 'operator-dashboard #flagged-devices' },
        { ref: 'issuesWidget', selector: 'operator-dashboard #open-data-collection-issues'},
        { ref: 'favoriteDeviceGroupsView', selector: 'operator-dashboard #favorite-device-groups dataview'},
        { ref: 'favoriteDeviceGroups', selector: '#my-favorite-device-groups'},
        { ref: 'favoriteDeviceGroupsGrid', selector: '#my-favorite-device-groups-grid'},
        { ref: 'summaryOfSelected', selector: '#selected-groups-summary'},
        { ref: 'uncheckAllBtn', selector: '#my-favorite-device-groups button[action=uncheckall]'}
    ],

    init: function () {
        this.control({
            '#operator-dashboard #refresh-btn': {
                click: this.loadData
            },
            '#my-favorite-device-groups-grid': {
                render: this.afterFavoriteDeviceGroupsGridRender
            },
            '#my-favorite-device-groups-grid checkcolumn': {
                checkchange: this.onFavoriteGroupsGridSelectionChange
            },
            '#my-favorite-device-groups button[action=uncheckall]': {
                click: this.uncheckAllSelectedGroups
            },
            '#my-favorite-device-groups button[action=save]': {
                click: this.saveFavoriteGroups
            }
        });
    },

    showMyFavorieDeviceGroups: function () {
        this.getApplication().fireEvent('changecontentevent', Ext.widget('my-favorite-device-groups'));
    },

    afterFavoriteDeviceGroupsGridRender: function () {
        var me = this;
        me.getFavoriteDeviceGroupsGrid().getStore().load({
            params: {
                includeAllGroups: true
            },
            callback: function () {
                me.onFavoriteGroupsGridSelectionChange();
            }
        });
    },

    onFavoriteGroupsGridSelectionChange: function () {
        var summaryOfSelectedField = this.getSummaryOfSelected(),
            store = this.getFavoriteDeviceGroupsGrid().getStore(),
            selectedGroups = store.queryBy(function (record) {
                return record.get('favorite') === true;
            }), selectedGroupsQty = selectedGroups.items.length;

        summaryOfSelectedField.setValue(
            selectedGroupsQty > 0 ?
                Uni.I18n.translatePlural('myFavoriteDeviceGroups.summarySelectedTpl', selectedGroupsQty, 'DSH', '{0} device groups selected') :
                Uni.I18n.translate('myFavoriteDeviceGroups.summaryTplNoSelected', 'DSH', 'No device groups selected')
        );

        this.getUncheckAllBtn().setDisabled(selectedGroupsQty < 1);
    },

    uncheckAllSelectedGroups: function () {
        this.getFavoriteDeviceGroupsGrid().getStore().each(function (record) {
            record.set('favorite', false);
        });

        this.onFavoriteGroupsGridSelectionChange();
    },

    saveFavoriteGroups: function () {
        var me = this, ids = [],
            router = me.getController('Uni.controller.history.Router'),
            store = me.getFavoriteDeviceGroupsGrid().getStore(),
            selectedGroups = store.queryBy(function (record) {
                return record.get('favorite') === true;
            });

        selectedGroups.each(function (group) {
            ids.push(group.get('id'));
        });

        me.getFavoriteDeviceGroups().setLoading(true);
        Ext.Ajax.request({
            url: store.proxy.url,
            method: 'PUT',
            jsonData: {ids: ids},
            success: function () {
                router.getRoute('dashboard').forward();
            },
            callback: function () {
                me.getFavoriteDeviceGroups().setLoading(false);
            }
        });
    },

    showOverview: function () {
        var router = this.getController('Uni.controller.history.Router');
        this.getApplication().fireEvent('changecontentevent', Ext.widget('operator-dashboard', {router: router}));
        this.loadData();
    },

    loadData: function () {
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure', 'privilege.view.communicationInfrastructure',
            'privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            var me = this,
                connectionModel = me.getModel('Dsh.model.connection.Overview'),
                communicationModel = me.getModel('Dsh.model.communication.Overview'),
                myOpenIssuesModel = me.getModel('Dsh.model.opendatacollectionissues.Overview'),
                issuesWidget = me.getIssuesWidget(),
                router = this.getController('Uni.controller.history.Router');
            if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.communicationInfrastructure', 'privilege.view.communicationInfrastructure'])) {
                connectionModel.setFilter(router.filter);
                communicationModel.setFilter(router.filter);
                me.getDashboard().setLoading();
                me.getFlaggedDevices().reload();
                Ext.getStore('Dsh.store.FavoriteDeviceGroups').load();
                me.getCommunicationServers().reload();
                connectionModel.load(null, {
                        success: function (connections) {
                            me.getConnectionSummary().setRecord(connections.getSummary());
                            me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));

                            communicationModel.load(null, {
                                success: function (communications) {
                                    me.getCommunicationSummary().setRecord(communications.getSummary());
                                    me.getHeader().down('#last-updated-field').setValue('Last updated at ' + Ext.util.Format.date(new Date(), 'H:i'));
                                },
                                callback: function () {
                                    me.getDashboard().setLoading(false);
                                }
                            });
                        }
                    }
                );
            }
            if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
                issuesWidget.setLoading();
                myOpenIssuesModel.load(null, {

                        success: function (issues) {
                            issuesWidget.setRecord(issues);
                            issuesWidget.setLoading(false);
                        }
                    }
                );
            }
        }
    }
});