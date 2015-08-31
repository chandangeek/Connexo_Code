Ext.define('Mdc.view.setup.property.CodeTable', {
    extend: 'Ext.window.Window',
    alias: 'widget.codeTableSelectionWindow',
    title: Uni.I18n.translate('property.selectCodeTable','MDc','Select a codeTable'),
    layout: 'fit',
    width: 350,
    height: 400,
    modal: true,
    constrain: true,
    autoShow: true,
    requires: [
        'Ext.grid.*'
    ],

    activeIndex: 0,
    party: null,

    initComponent: function () {
        var codeTables = Ext.create('Mdc.store.CodeTables');
        codeTables.load();
        this.items = [
            {
                xtype: 'grid',
                itemId: 'codeTableSelectionGrid',
                autoScroll: true,
                store: codeTables,
                columns: [
                    {
                        text: Uni.I18n.translate('contables.codeTables','MDC','CodeTables'),
                        xtype: 'templatecolumn',
                        tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                            '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                            '{codeTableId} - {name}' +
                            '</caption>' +
                            '</table>',
                        flex:1
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: Uni.I18n.translate('general.select','MDC','Select'),
                action: 'select'
            },
            {
                text: Uni.I18n.translate('general.cancel','MDC','Cancel'),
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
