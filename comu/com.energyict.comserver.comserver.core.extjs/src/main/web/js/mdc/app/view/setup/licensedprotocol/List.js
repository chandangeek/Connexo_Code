Ext.define('Mdc.view.setup.licensedprotocol.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupLicensedProtocols',
    store: 'LicensedProtocols',
    itemId: 'licensedprotocolgrid',
    overflowY: 'auto',
    layout: 'fit',
    initComponent: function () {
        this.columns = [
            {
                text: 'Licensed Protocols',
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                    '{licensedProtocolRuleCode} - {protocolName}' +
                    '</caption>' +
                    '<tr>' +
                    '<td>Java class name: </td> ' +
                    '<td>{protocolJavaClassName}</td>' +
                    '</tr>' +
                    '<tpl for="protocolFamilies">' +
                    '<tr>' +
                    '<tpl if="xindex === 1">' +
                    '<td>Protocol Family: </td>' +
                    '<tpl else>' +
                    '<td></td>' +
                    '</tpl>' +
                    '<td>{protocolFamilyCode} - {protocolFamilyName}</td>' +
                    '</tr>' +
                    '</tpl>' +
                    '</table>',
                flex: 1
            }
        ];

        this.callParent(arguments);
    }
});