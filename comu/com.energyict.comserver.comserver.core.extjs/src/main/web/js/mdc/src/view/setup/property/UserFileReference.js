Ext.define('Mdc.view.setup.property.UserFileReference', {
    extend: 'Ext.window.Window',
    alias: 'widget.userFileReferenceSelectionWindow',
    title: Uni.I18n.translate('property.selectUserFile','MDC','Select a user File Reference'),
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
        var userFileReferences = Ext.create('Mdc.store.UserFileReferences');
        userFileReferences.load();
        this.items = [
            {
                xtype: 'grid',
                itemId: 'userFileReferenceSelectionGrid',
                autoScroll: true,
                store: userFileReferences,
                columns: [
                    {
                        text: Uni.I18n.translate('userFileReferences.userFileReferences','MDC','User File References'),
                        xtype: 'templatecolumn',
                        tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                            '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                            '{userFileReferenceId} - {name}' +
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
