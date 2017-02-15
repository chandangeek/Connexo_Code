/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.FavoriteDeviceGroups', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.favorite-device-groups',
    ui: 'tile',
    store: 'Dsh.store.FavoriteDeviceGroups',
    title: ' ',
    header: {
        ui: 'small'
    },

    initComponent: function () {
        this.callParent(arguments);
        var me = this,
            store = Ext.getStore(me.store);

        me.setTitle(' ');
        store.load({
            callback: function () {
                me.add([
                    {
                        xtype: 'dataview',
                        store: me.store,
                        itemSelector: 'p a',
                        emptyText: Uni.I18n.translate('overview.widget.favoriteDeviceGroups.notFound', 'DSH', 'No favorite device groups found'),
                        overflowY: 'auto',
                        style: 'max-height: 160px',
                        tpl: new Ext.XTemplate(
                            '<table style="margin-top:5px; margin-left:5px">',
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
                        style: 'margin-top: 15px; margin-left:4px',
                        href: '#/dashboard/selectfavoritedevicegroups'
                    }
                ]);
                me.setTitle(
                    '<h3>' +
                    ( store.count() > 0
                        ? Uni.I18n.translate('overview.widget.favoriteDeviceGroups.header', 'DSH', 'My favorite device groups ({0})', store.count())
                        : Uni.I18n.translate('overview.widget.favoriteDeviceGroups.headerNoItemsFound', 'DSH', 'My favorite device groups')
                    )
                    + '</h3>'
                );
            }
        });
    }
});


