Ext.define('Dsh.controller.OperatorDashboard', {
    extend: 'Ext.app.Controller',
    models: [
        'Dsh.model.connection.Overview',
        'Dsh.model.connection.OverviewDashboard',
        'Dsh.model.communication.Overview',
        'Dsh.model.communication.OverviewDashboard',
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
            },
            '#my-favorite-device-groups [action=addItem]': {
                click: function () {
                    var router = this.getController('Uni.controller.history.Router');
                    router.getRoute('devices/devicegroups/add').forward();
                }
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
        var me = this,
            dashboard = me.getDashboard(),
            lastUpdateField = dashboard.down('#last-updated-field');

        if (Mdc.privileges.Device.canOperateDevice() ||
                Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
            var connectionModel = me.getModel('Dsh.model.connection.OverviewDashboard'),
                communicationModel = me.getModel('Dsh.model.communication.OverviewDashboard'),
                myOpenIssuesModel = me.getModel('Dsh.model.opendatacollectionissues.Overview'),
                issuesWidget = me.getIssuesWidget(),
                router = this.getController('Uni.controller.history.Router');
            if (Mdc.privileges.Device.canView()) {
                me.getFlaggedDevices().reload();
            }

            communicationModel.getProxy().url = '/api/dsr/communicationoverview/widget';
            connectionModel.getProxy().url = '/api/dsr/connectionoverview/widget';

            if (Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
                connectionModel.setFilter(router.filter);
                communicationModel.setFilter(router.filter);
                dashboard.setLoading();
                me.getCommunicationServers().reload();
                connectionModel.load(null, {
                    success: function (connections) {
                        if (me.getConnectionSummary()) {
                            me.getConnectionSummary().setRecord(connections.getSummary());
                        }
                    },
                    callback: function () {
                        communicationModel.load(null, {
                            success: function (communications) {
                                if (me.getCommunicationSummary()) {
                                    me.getCommunicationSummary().setRecord(communications.getSummary());
                                }
                            },
                            callback: function () {
                                if (lastUpdateField) {
                                    lastUpdateField.update('Last updated at ' + Uni.DateTime.formatTimeShort(new Date()));
                                }
                                dashboard.setLoading(false);
                            }
                        });
                    }
                });
            }

            if (Uni.Auth.hasAnyPrivilege(['privilege.view.issue', 'privilege.comment.issue', 'privilege.close.issue', 'privilege.assign.issue', 'privilege.action.issue'])) {
                issuesWidget.setLoading();
                myOpenIssuesModel.load(null, {
                    success: function (issues) {
                        issuesWidget.setRecord(issues);
                        issuesWidget.setLoading(false);
                    }
                });
            }
        }
    }
});