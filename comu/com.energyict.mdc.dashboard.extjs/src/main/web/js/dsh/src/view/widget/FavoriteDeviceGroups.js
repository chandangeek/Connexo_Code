Ext.define('Dsh.view.widget.FavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.favorite-device-groups',
    ui: 'tile',
    store: 'Dsh.store.FavoriteDeviceGroups',
    initComponent: function () {
        this.callParent(arguments);
        var me = this,
            store = Ext.getStore(me.store);
        store.load({
            callback: function () {
                me.add([
                    {
                        xtype: 'container',
                        html: store.count() > 0 ?
                            '<h3>' + Ext.String.format(Uni.I18n.translate('overview.widget.favoriteDeviceGroups.header', 'DSH', 'My favorite device groups ({0})'), store.count()) + '</h3>' :
                            '<h3>' + Uni.I18n.translate('overview.widget.favoriteDeviceGroups.headerNoItemsFound', 'DSH', 'My favorite device groups') + '</h3>'
                    },
                    {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.noFavoriteDeviceGroupsMsg', 'DSH', 'No favorite device groups'),
                        reasons: [
                            Uni.I18n.translate('overview.widget.favoriteDeviceGroups.noFavoriteDeviceGroupsReason', 'DSH', 'You have not added any group to favorites')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.selectBtn', 'DSH', 'Select'),
                                ui: 'action',
                                href: '#/dashboard/selectfavoritedevicegroups'
                            }
                        ],
                        hidden: store.count() > 0,
                        listeners: {
                            render: function (panel) {
                                panel.el.down('.uni-panel-no-items-found').dom.style.border = 'none';
                            }
                        }
                    },
                    {
                        xtype: 'dataview',
                        store: me.store,
                        itemSelector: 'p a',
                        hidden: store.count() < 1,
                        tpl: new Ext.XTemplate(
                            '<tpl for=".">',
                                '<p>',
                                    '<a href="#/devices/devicegroups/{id}">{name}</a>',
                                '</p>',
                            '</tpl>'
                        )
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.selectBtn', 'DSH', 'Select'),
                        ui: 'action',
                        href: '#/dashboard/selectfavoritedevicegroups',
                        hidden: store.count() < 1
                    }
                ]);
            }
        });
    }
});