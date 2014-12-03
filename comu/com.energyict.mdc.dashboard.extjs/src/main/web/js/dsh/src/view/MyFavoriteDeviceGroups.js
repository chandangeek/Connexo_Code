Ext.define('Dsh.view.MyFavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.my-favorite-device-groups',
    itemId: 'my-favorite-device-groups',
    title: Uni.I18n.translate('myFavoriteDeviceGroups.pageTitle', 'DSH', 'Select favorite device groups'),
    ui: 'large',
    style: {
        margin: '0 20px'
    },
    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.add([
            {
                xtype: 'panel',
                layout: 'hbox',
                defaults: {
                    style: {
                        marginRight: '20px'
                    }
                },
                items: [
                    {
                        xtype: 'displayfield',
                        itemId: 'selected-groups-summary'
                    },
                    {
                        xtype: 'button',
                        text: 'Uncheck all',
                        action: 'uncheckall'
                    }
                ]
            },
            {
                xtype: 'grid',
                itemId: 'my-favorite-device-groups-grid',
                store: 'Dsh.store.FavoriteDeviceGroups',
                disableSelection: true,
                viewConfig: {
                    markDirty: false
                },
                columns: [
                    {
                        xtype: 'checkcolumn',
                        dataIndex: 'favorite',
                        width: 35
                    },
                    {
                        header: Uni.I18n.translate('myFavoriteDeviceGroups.grid.column.name', 'DSH', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('myFavoriteDeviceGroups.grid.column.type', 'DSH', 'Type'),
                        dataIndex: 'dynamic',
                        flex: 1,
                        renderer: function (value) {
                            return value ?
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.dynamic', 'DSH', 'Dynamic') :
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.static', 'DSH', 'Static');
                        }
                    }
                ]
            },
            {
                xtype: 'button',
                text: 'Save',
                action: 'save',
                ui: 'action'
            },
            {
                xtype: 'button',
                text: 'Cancel',
                href: '#/dashboard',
                ui: 'link'
            }
        ]);
    }
});