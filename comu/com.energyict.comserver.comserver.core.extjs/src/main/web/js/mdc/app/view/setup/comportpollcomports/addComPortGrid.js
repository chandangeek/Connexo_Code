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
    initComponent: function () {
        var comServerStore = Ext.data.StoreManager.get('ComServers');
        this.columns = [
            {
                header: Uni.I18n.translate('comPortPoolComPort.communicationServer', 'MDC', 'Communication port'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('comPortPoolComPort.communicationServer', 'MDC', 'Communication server'),
                dataIndex: 'comServer_id',
                flex: 2,
                renderer: function (value) {
                    var comServer = comServerStore.getById(value),
                        result = comServer ? comServer.get('name') : '';
                    return result;
                }

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
        ];
        this.callParent();
    }
});