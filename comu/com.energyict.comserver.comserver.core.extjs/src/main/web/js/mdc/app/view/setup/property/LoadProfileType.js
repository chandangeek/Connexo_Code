Ext.define('Mdc.view.setup.property.LoadProfileType', {
    extend: 'Ext.window.Window',
    alias: 'widget.loadProfileTypeSelectionWindow',
    title: 'Select a load profile type',
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
        var loadProfileTypes = Ext.create('Mdc.store.LoadProfileTypes');
        this.items = [
            {
                xtype: 'grid',
                itemId: 'loadProfileTypeSelectionGrid',
                autoScroll: true,
                store: loadProfileTypes,
                columns: [
                    {
                        text: 'Load Profile Types',
                        xtype: 'templatecolumn',
                        tpl: '<table width="100%" border = "0" style="color:dimgrey;font-size:x-small;line-height:110%">' +
                            '<caption style="color:black;font-size:small;line-height:200%;font-weight:bold;text-align:left;caption-side: left">' +
                            '{loadProfileTypeId} - {name}' +
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
