Ext.define('Mdc.view.setup.comportpollcomports.addComPortGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.addComportToComportPoolGrid',
    itemId: 'addComportToComportPoolGrid',
    store: 'ComPortPoolComports',
    scroll: false,
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' }
    },
    selType: 'checkboxmodel',
    selModel: {
        checkOnly: true,
        enableKeyNav: false,
        showHeaderCheckbox: false
    },
    columns: [
        {
            header: Uni.I18n.translate('comPortPoolComPort.communicationServer', 'MDC', 'Communication port'),
            dataIndex: 'name',
            flex: 3
        },
        {
            header: Uni.I18n.translate('comPortPoolComPort.communicationServer', 'MDC', 'Communication server'),
            dataIndex: 'comServerName',
            flex: 2
        },
        {
            header: Uni.I18n.translate('comPortPoolComPort.portStatus', 'MDC', 'Port status'),
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
    ]

});