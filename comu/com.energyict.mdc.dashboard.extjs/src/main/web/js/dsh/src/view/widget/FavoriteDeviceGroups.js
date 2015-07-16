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
                        style: {
                            margin: '0 0 10px 0'
                        },
                        html: store.count() > 0 ?
                            '<h3>' + Ext.String.format(Uni.I18n.translate('overview.widget.favoriteDeviceGroups.header', 'DSH', 'My favorite device groups ({0})'), store.count()) + '</h3>' :
                            '<h3>' + Uni.I18n.translate('overview.widget.favoriteDeviceGroups.headerNoItemsFound', 'DSH', 'My favorite device groups') + '</h3>'
                    },
                    {
                        xtype: 'dataview',
                        store: me.store,
                        itemSelector: 'p a',
                        emptyText: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.notFound', 'DSH', 'No favorite device groups found'),
                        overflowY: 'auto',
                        style: 'max-height: 120px',
                        tpl: new Ext.XTemplate(
                            '<table style="margin-top: 5px">',
                                '<tpl for=".">',
                                    '<tr>',
                                        '<td style="height: 20px">',
                                            Mdc.privileges.DeviceGroup.canAdministrateOrViewDetails()
                                                ? '<a href="#/devices/devicegroups/{id}">{name:htmlEncode}</a>' :
                                                (
                                                    Mdc.privileges.DeviceGroup.canAdministrateDeviceOfEnumeratedGroup()?
                                                        (
                                                            '<tpl if="dynamic==true"{dynamic}>{name:htmlEncode}<tpl else><a href="#/devices/devicegroups/{id}">{name:htmlEncode}</a></tpl>'
                                                        )
                                                         :
                                                        '{name}'
                                                ),
                                        '</td>',
                                    '</tr>',
                                '</tpl>',
                            '</table>'
                        )
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.selectBtn', 'DSH', 'Select'),
                        //hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceGroup'),
                        style: 'margin-top: 15px',
                        href: '#/dashboard/selectfavoritedevicegroups'
                    }
                ]);
            }
        });
    }
});


