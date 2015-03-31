Ext.define('Fwc.view.firmware.SideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.firmware-side-filter',
    requires: [
        'Fwc.view.firmware.field.FirmwareType',
        'Fwc.view.firmware.field.FirmwareStatus'
    ],
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    cls: 'filter-form',
    itemId: 'filter-form',
    ui: 'filter',
    defaults: {
        labelAlign: 'top'
    },
    hydrator: 'Fwc.form.Hydrator',
    items: [
        {
            xtype: 'firmware-type'
        },
        {
            xtype: 'firmware-status'
        }
    ],

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            items: [
                {
                    text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DSH', 'Apply'),
                    ui: 'action',
                    action: 'applyfilter',
                    itemId: 'btn-apply-filter'
                },
                {
                    text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                    action: 'clearfilter',
                    itemId: 'btn-clear-filter'
                }
            ]
        }
    ]
});
