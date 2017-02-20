Ext.define('Imt.dashboard.view.FavoriteUsagePointGroups', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.favorite-usage-point-groups',
    store: 'Imt.dashboard.store.FavoriteUsagePointGroups',
    // checkAllButtonPresent: false,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('favoriteUsagePointGroups.multiselect.selected', count, 'IMT',
            'No usage point groups selected', '{0} usage point group selected', '{0} usage point groups selected'
        );
    },

    buttonAlign: 'left',
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'IMT', 'Name'),
            dataIndex: 'name',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.type', 'IMT', 'Type'),
            dataIndex: 'dynamic',
            flex: 1,
            renderer: function (value) {
                return value ?
                    Uni.I18n.translate('favoriteUsagePointGroups.grid.type.dynamic', 'IMT', 'Dynamic') :
                    Uni.I18n.translate('favoriteUsagePointGroups.grid.type.static', 'IMT', 'Static');
            }
        }
    ],

    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.save', 'IMT', 'Save'),
            itemId: 'btn-save-favorite',
            action: 'saveFavorites',
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-favorite',
            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
            href: '#/dashboard',
            ui: 'link'
        }
    ],

});

