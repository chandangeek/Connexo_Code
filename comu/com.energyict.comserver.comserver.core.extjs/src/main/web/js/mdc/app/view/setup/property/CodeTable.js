Ext.define('Mdc.view.setup.property.CodeTable', {
    extend: 'Ext.window.Window',
    alias: 'widget.codeTableSelectionWindow',
    title: 'Select a codeTable',
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
        this.items = [
            {
                xtype: 'grid',
                itemId: 'codeTableSelectionGrid',
                autoScroll: true,
                store: codeTables,
                columns: [
                    {
                        text: 'CodeTables',
                        xtype: 'templatecolumn',
                        tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                            '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                            '{id} - {name}' +
                            '</caption>' +
                            '</table>',
                        flex:1
                    }
                ]
            }
        ];

        this.buttons = [
            {
                text: 'Select',
                action: 'select'
            },
            {
                text: 'Cancel',
                action: 'cancel'
            }
        ];

        this.callParent(arguments);
    }
});
