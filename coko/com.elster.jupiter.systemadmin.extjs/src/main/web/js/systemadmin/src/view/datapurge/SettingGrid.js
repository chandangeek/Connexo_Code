Ext.define('Sam.view.datapurge.SettingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.data-purge-settings-grid',
    store: 'Sam.store.DataPurgeSettings',
    plugins: [
        'showConditionalToolTip',
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],
    forceFit: true,
    showButtons: true,
    router: null,

    columns: [
        {
            itemId: 'data-purge-settings-grid-name-column',
            header: Uni.I18n.translate('datapurge.settings.name', 'SAM', 'Category'),
            dataIndex: 'name'
        },
        {
            itemId: 'data-purge-settings-grid-retained-partition-count-column',
            header: Uni.I18n.translate('datapurge.settings.retainedPartitionCount', 'SAM', 'Purge data older than (x 30 days)'),
            dataIndex: 'retainedPartitionCount',
            align: 'right',
            editor: {
                xtype: 'numberfield',
                minValue: 1,
                maxValue: 999
            }
        },
        {
            itemId: 'data-purge-settings-grid-retention-column',
            header: Uni.I18n.translate('datapurge.settings.retention', 'SAM', 'Retention period (days)'),
            dataIndex: 'retention',
            align: 'right',
            renderer: function (value) {
                return Uni.Number.formatNumber(value, 0);
            }
        }
    ],

    initComponent: function () {
        var me = this;

        if (me.showButtons) {
            me.bbar = [
                {
                    itemId: 'data-purge-settings-save-button',
                    text: Uni.I18n.translate('general.save', 'SAM', 'Save'),
                    ui: 'action'
                },
                {
                    itemId: 'data-purge-settings-cancel-button',
                    text: Uni.I18n.translate('general.cancel', 'SAM', 'Cancel'),
                    ui: 'link',
                    href: me.router.getRoute('administration').buildUrl()
                }
            ];
        }

        me.callParent(arguments);
    }
});