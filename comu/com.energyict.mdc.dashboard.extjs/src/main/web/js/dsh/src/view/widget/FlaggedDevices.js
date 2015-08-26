Ext.define('Dsh.view.widget.FlaggedDevices', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.flagged-devices',
    buttonAlign: 'left',
    layout: 'fit',
    router: null,
    header: {
        ui: 'small'
    },

    tooltipTpl: new Ext.XTemplate(
        '<table>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.mrid', 'DSH', 'MRID') + '</td>',
        '<td>{mRID}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.serialNumber', 'DSH', 'Serial number') + '</td>',
        '<td>{serialNumber}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.deviceTypeName', 'DSH', 'Device Type') + '</td>',
        '<td>{deviceTypeName}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.creationDate', 'DSH', 'Flagged date') + '</td>',
        '<td>{[Uni.DateTime.formatDateTimeLong(values.deviceLabelInfo.creationDate)]}</td>',
        '</tr>',
        '<tr>',
        '<td style="text-align: right; padding-right: 10px; white-space: nowrap">' + Uni.I18n.translate('overview.widget.flaggedDevices.device.comment', 'DSH', 'Comment') + '</td>',
        '<td>{[values.deviceLabelInfo.comment]}</td>',
        '</tr>',
        '</table>'
    ),

    items: {
        xtype: 'dataview',
        store: 'Dsh.store.FlaggedDevices',
        itemId: 'devices-dataview',
        style: 'max-height: 160px',
        overflowY: 'auto',
        itemSelector: 'a.x-btn.flag-toggle',
        emptyText: Uni.I18n.translate('overview.widget.flaggedDevices.noDevicesFound', 'DSH', 'No flagged devices found'),

        tpl: new Ext.XTemplate(
            '<table  style="margin: 5px 0 10px 0">',
            '<tpl for=".">',
                '<tr id="{mRID}" data-qtip="{[Ext.htmlEncode(values.tooltip)]}" class="device">',
                    '<td width="100%"><a href="{href}">{mRID}</a></td>',
                    '<td>',
                    '<a data-qtip="'+
                    Uni.I18n.translate('overview.widget.flaggedDevices.unflag', 'DSH', 'Click to remove from the list of flagged devices') +
                    '" class="flag-toggle x-btn x-btn-plain-small">',
                        '<span style="width: 16px; height: 16px; font-size: 16px" class="x-btn-button"><span class="x-btn-icon-el icon-star6"></span></span></a>',
                    '</td>',
                '</tr>',
            '</tpl>',
            '</table>'
        ),

        listeners: {
            'itemclick': function (view, record, item) {
                var elm = new Ext.dom.Element(item);
                var icon = elm.down('.x-btn-icon-el');
                var pressed = icon.hasCls('icon-star6');
                var flag = record.getLabel();
                flag.proxy.setUrl(record.getId());

                var callback = function() {
                    icon.toggleCls('icon-star6');
                    icon.toggleCls('icon-star4');
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
            clone.save({callback: callback});
        },

        unflag: function(record, callback) {
            record.destroy({callback: callback});
        }
    },

    reload: function () {
        var me = this,
            elm = me.down('#devices-dataview'),
            store = elm.getStore();

        store.load(function () {
            var title = '<h3>'
                + Ext.String.format(Uni.I18n.translate('overview.widget.flaggedDevices.header', 'DSH',  'My flagged devices ({0})'), store.count())
                + '</h3>';
            me.setTitle(title);

            store.each(function(item) {
                item.set('href', me.router.getRoute('devices/device').buildUrl({mRID: item.getId()}));
                item.set('tooltip', me.tooltipTpl.apply(item.getData(true)));
            });

            elm.bindStore(store);
        });
    }
});