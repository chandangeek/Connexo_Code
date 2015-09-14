Ext.define('Dsh.view.MyFavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.my-favorite-device-groups',
    itemId: 'my-favorite-device-groups',
    title: Uni.I18n.translate('general.selectFavoriteDeviceGroups', 'DSH', 'Select favorite device groups'),
    ui: 'large',
    margin: '0 20',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    initComponent: function () {
        var me = this;
        me.callParent(arguments);
        me.add({
            xtype: 'emptygridcontainer',
            grid: {
                xtype: 'grid',
                itemId: 'my-favorite-device-groups-grid',
                extend: 'Uni.view.grid.SelectionGrid',
                store: 'Dsh.store.FavoriteDeviceGroups',
                margin: '0 40 0 0',
                disableSelection: true,
                overflowY: 'auto',
                maxHeight: 450,
                viewConfig: {
                    markDirty: false
                },
                tbar: [
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
                                itemId: 'selected-groups-summary',
                                margin: '0 20 0 0'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.uncheckAll','DSH','Uncheck all'),
                                action: 'uncheckall'
                            }
                        ]
                    }

                ],
                columns: [
                    {
                        xtype: 'checkcolumn',
                        dataIndex: 'favorite',
                        width: 35
                    },
                    {
                        header: Uni.I18n.translate('general.name', 'DSH', 'Name'),
                        dataIndex: 'name',
                        flex: 1
                    },
                    {
                        header: Uni.I18n.translate('general.type', 'DSH', 'Type'),
                        dataIndex: 'dynamic',
                        flex: 1,
                        renderer: function (value) {
                            return value ?
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.dynamic', 'DSH', 'Dynamic') :
                                Uni.I18n.translate('myFavoriteDeviceGroups.grid.type.static', 'DSH', 'Static');
                        }
                    }
                ],
                buttonAlign: 'left',
                buttons: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.save','DSH','Save'),
                        action: 'save',
                        ui: 'action'
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.cancel','DSH','Cancel'),
                        href: '#/dashboard',
                        ui: 'link'
                    }
                ]
            },
            emptyComponent: {
                xtype: 'no-items-found-panel',
                title: Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.title', 'DSH', 'No device groups found'),
                reasons: [
                    Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.list.reason1', 'DSH', 'No device groups have been defined yet.')
                ],
                stepItems: [
                    {
                        text: Uni.I18n.translate('workspace.myFavoriteDeviceGroups.empty.list.action1', 'DSH', 'Add device group'),
                        action: 'addItem'
                    }
                ]
            }
        });
    }
});