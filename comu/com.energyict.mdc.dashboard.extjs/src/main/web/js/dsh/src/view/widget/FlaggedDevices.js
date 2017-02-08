/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.FlaggedDevices', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.flagged-devices',
    buttonAlign: 'left',
    layout: 'fit',
    title: ' ',
    router: null,
    header: {
        ui: 'small'
    },

    tooltipTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.name', 'DSH', 'Name') + '</td>',
        '<td>{[Ext.htmlEncode(values.name)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.serialNumber', 'DSH', 'Serial number') + '</td>',
        '<td>{[Ext.htmlEncode(values.serialNumber)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.deviceTypeName', 'DSH', 'Device Type') + '</td>',
        '<td>{[Ext.htmlEncode(values.deviceTypeName)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.creationDate', 'DSH', 'Flagged date') + '</td>',
        '<td>{[Uni.DateTime.formatDateTimeLong(new Date(values.deviceLabelInfo.creationDate))]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.comment', 'DSH', 'Comment') + '</td>',
        '<td>{[Ext.htmlEncode(values.deviceLabelInfo.comment)]}</td>',
        '</tr>',
        '</table>'
    ),

    items: {
        xtype: 'dataview',
        store: 'Dsh.store.FlaggedDevices',
        itemId: 'devices-dataview',
        style: 'max-height: 207px',
        overflowY: 'auto',
        itemSelector: 'a.x-btn.flag-toggle',
        emptyText: Uni.I18n.translate('overview.widget.flaggedDevices.noDevicesFound', 'DSH', 'No flagged devices found'),

        tpl: new Ext.XTemplate(
            '<table  style="margin: 5px 0 10px 5px">',
            '<tpl for=".">',
                '<tr id="{name}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" class="device">',
                    '<td width="100%"><a href="{href}">{[Ext.htmlEncode(values.name)]}</a></td>',
                    '<tpl if="this.showButton()">',
                    '<td>',
                    '<a data-qtip="'+
                    Uni.I18n.translate('overview.widget.flaggedDevices.unflag', 'DSH', 'Click to remove from the list of flagged devices') +
                    '" class="flag-toggle x-btn x-btn-plain-small">',
                        '<span style="width: 16px; height: 16px; font-size: 16px" class="x-btn-button"><span class="x-btn-icon-el icon-star-full"></span></span></a>',
                    '</td>',
                    '</tpl>',
                '</tr>',
            '</tpl>',
            '</table>',
            {
                showButton: function () {
                    return Mdc.privileges.Device.canFlagDevice();
                }
            }
        ),

        listeners: {
            'itemclick': function (view, record, item) {
                var elm = new Ext.dom.Element(item);
                var icon = elm.down('.x-btn-icon-el');
                var pressed = icon.hasCls('icon-star-full');
                var flag = record.getLabel();
                flag.proxy.setUrl(record.getId());

                var callback = function(rec, operation) {
                    if (operation && !Ext.isEmpty(operation.response.responseText)) {
                        flag.set('creationDate', Ext.decode(operation.response.responseText).creationDate);
                    }
                    icon.toggleCls('icon-star-full');
                    icon.toggleCls('icon-star-empty');
                    elm.set({'data-qtip': pressed
                        ? Uni.I18n.translate('overview.widget.flaggedDevices.flag', 'DSH', 'Click to flag the device')
                        : Uni.I18n.translate('overview.widget.flaggedDevices.unflag', 'DSH', 'Click to remove from the list of flagged devices')
                    });
                };

                pressed ? view.unflag(flag, callback) : view.flag(flag, callback);
            }
        },

        flag: function(record, callback) {
            var clone = new record.self();
            var data = record.getWriteData(false, true);
            clone.set(data);
            clone.save({
                isNotEdit: true,
                callback: callback
            });
        },

        unflag: function(record, callback) {
            record.destroy({
                isNotEdit: true,
                callback: callback
            })
        }
    },

    reload: function () {
        var me = this,
            elm = me.down('#devices-dataview'),
            store = elm.getStore();

        me.setTitle('');
        store.load(function () {
            var title = '<h3>'
                + Ext.String.format(Uni.I18n.translate('overview.widget.flaggedDevices.header', 'DSH',  'My flagged devices ({0})'), store.count())
                + '</h3>';
            me.setTitle(title);

            store.each(function(item) {
                item.set('href', me.router.getRoute('devices/device').buildUrl({deviceId: encodeURIComponent(item.get('name'))}));
                item.set('tooltip', me.tooltipTpl.apply(item.getData(true)));
            });

            elm.refresh();
        });
    }
});