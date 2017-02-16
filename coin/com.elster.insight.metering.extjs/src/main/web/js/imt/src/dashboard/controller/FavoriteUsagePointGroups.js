Ext.define('Imt.dashboard.controller.FavoriteUsagePointGroups', {
    extend: 'Ext.app.Controller',
    requires: [
        'Imt.dashboard.store.FavoriteUsagePointGroups',
        'Imt.dashboard.view.AddFavoriteUsagePointGroups',
        'Imt.dashboard.view.FavoriteUsagePointGroups'
    ],

    models: [],
    stores: [
        'Imt.dashboard.store.FavoriteUsagePointGroups'
    ],
    views: [
        'Imt.dashboard.view.AddFavoriteUsagePointGroups'
    ],

    refs: [
        {ref: 'favoriteUsagePointGroupsGrid', selector: '#usage-point-groups-grid'},

    ],

    init: function () {
        this.control({
            'favorite-usage-point-groups #btn-save-favorite': {
                click: this.saveFavofiteUsagePointGroups
            }
        });
    },

    showFavoriteUsagePointGroups: function () {
        var me = this,
            widget = Ext.widget('add-favorite-usage-point-groups'),
            allUsagePointGroupsGrid = widget.down('#usage-point-groups-grid'),
            allUsagePointGroupsSelModel = allUsagePointGroupsGrid.getSelectionModel(),
            allUsagePointGroupsStore = me.getStore('Imt.dashboard.store.FavoriteUsagePointGroups');

        allUsagePointGroupsStore.load({
            callback: function (records, operation, success) {
                var selectedRecords = [];

                me.getApplication().fireEvent('changecontentevent', widget);
                allUsagePointGroupsStore.each(function (usagePointGroup) {
                    if (usagePointGroup.get('favorite')) {
                        selectedRecords.push(usagePointGroup);
                    }
                });
                allUsagePointGroupsSelModel.suspendChanges();
                allUsagePointGroupsSelModel.select(selectedRecords, false, true);
                allUsagePointGroupsSelModel.resumeChanges();
                allUsagePointGroupsGrid.fireEvent('selectionchange');
            }
        });
    },

    saveFavofiteUsagePointGroups: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            usagePointGroupsGrid = me.getFavoriteUsagePointGroupsGrid(),
            usagePointGroupsStore = me.getStore('Imt.dashboard.store.FavoriteUsagePointGroups'),
            usagePointGroupsSelModel = usagePointGroupsGrid.getSelectionModel(),
            usagePointGroupsRecords = [];

        Ext.Array.each(usagePointGroupsStore.getRange(), function (usagePointGroup) {
            usagePointGroup.set('favorite', usagePointGroupsSelModel.isSelected(usagePointGroup));
            usagePointGroupsRecords.push(usagePointGroup.getRecordData());
        });
        usagePointGroupsGrid.setLoading(true);
        Ext.Ajax.request({
            url: '/api/udr/favorites/usagepointgroups',
            method: 'PUT',
            jsonData: {favoriteUsagePointGroups: usagePointGroupsRecords},
            success: function () {
                router.getRoute('dashboard').forward();
            },
            callback: function () {
                usagePointGroupsGrid.setLoading(false);
            }
        });

    }
});
