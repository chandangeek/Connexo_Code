Ext.define('Fwc.view.firmware.SideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.firmware-side-filter',
    requires: [
        'Fwc.view.firmware.field.FirmwareType'
    ],
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    cls: 'filter-form',
    itemId: 'filter-form',
    ui: 'filter',
    defaults: {
        labelAlign: 'top'
    },
    items: [
        {
            xtype: 'firmware-type',
            defaultType: 'checkbox'
        },
        {
            xtype: 'checkboxgroup',
            fieldLabel: 'Status',
            columns: 1,
            vertical: true,
            items: [
                {
                    boxLabel: 'Deprecated',
                    name: 'status',
                    inputValue: 'deprecated',
                    dataIndex: 'deprecated'
                }, {
                    boxLabel: 'Final',
                    name: 'status',
                    inputValue: 'final'
                }, {
                    boxLabel: 'Ghost',
                    name: 'status',
                    inputValue: 'ghost'
                }, {
                    boxLabel: 'Test',
                    name: 'status',
                    inputValue: 'test'
                }
            ]
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
    ],

    updateRecord: function (record) {
        record = record || this.getRecord();

        record.beginEdit();
        record.set(this.getValues());
        record.endEdit();

        return this;
    }
});
