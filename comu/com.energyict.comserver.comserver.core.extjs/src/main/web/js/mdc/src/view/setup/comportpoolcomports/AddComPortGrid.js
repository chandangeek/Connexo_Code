Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'addComportToComportPoolGrid',
    itemId: 'addComportToComportPoolGrid',
    store: 'ComPortPoolComports',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'comPortPoolComPorts.addPorts.counterText',
            count,
            'MDC',
            '{0} communication ports selected'
        );
    },

    allLabel: Uni.I18n.translate('comPortPoolComPort.allComPorts', 'MDC', 'All communication ports'),
    allDescription: Uni.I18n.translate('general.selectAllItems', 'MDC', 'Select all items (related to filters on previous screen)'),

    selectedLabel: Uni.I18n.translate('comPortPoolComPort.selectedComPorts', 'MDC', 'Selected communication ports'),
    selectedDescription: Uni.I18n.translate('general.selectItemsInTable', 'MDC', 'Select items in table'),

    columns: [
        {
            header: Uni.I18n.translate('general.comServer', 'MDC', 'Communication server'),
            dataIndex: 'comServerName',
            flex: 2
        },
        {
            header: Uni.I18n.translate('comPortPoolComPort.communicationPort', 'MDC', 'Communication port'),
            dataIndex: 'name',
            flex: 3
        },
        {
            header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            dataIndex: 'active',
            flex: 3,
            renderer: function (value) {
                if (value === true) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateCancelHref: function (comPortPoolId) {
        var me = this,
            href = '#/administration/comportpools/' + comPortPoolId + '/comports';

        if (me.rendered) {
            me.getCancelButton().setHref(href);
        } else {
            me.cancelHref = href;
        }
    }

});