Ext.define('Mdc.view.setup.protocolfamily.List', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.setupProtocolFamilies',
    itemId: 'protocolfamilygrid',
    overflowY: 'auto',
    layout: 'fit',
    initComponent: function () {
        this.columns = [
            {
                text: Uni.I18n.translate('protocolFamilies.protocolFamilies','MDC','Protocol Families'),
                xtype: 'templatecolumn',
                tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                    '<caption style="color:black;font-size:small;line-height:200%;text-align:left;caption-side: left">' +
                    '{protocolFamilyCode} - {protocolFamilyName}' +
                    '</caption>' +
                    '</table>',
                flex: 1
            }
        ];

        this.callParent(arguments);
    }
});