Ext.define('Mtr.view.person.Browse', {
    extend: 'Ext.container.Container',
    alias: 'widget.personBrowse',
    cls: 'content-wrapper',
    overflowY: 'auto',
    requires: [
        'Mtr.view.person.List'
    ],

    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>Persons</h1>'
                },
                {
                    xtype: 'personList'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});