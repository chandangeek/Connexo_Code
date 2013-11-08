Ext.define('Uni.view.error.Window', {
    extend: 'Ext.window.Window',
    alias: 'widget.errorWindow',

    requires: [
    ],

    modal: true,
    constrain: true,

    items: [
        {
            xtype: 'component',
            html: 'Here be exceptions'
        }
    ]
});