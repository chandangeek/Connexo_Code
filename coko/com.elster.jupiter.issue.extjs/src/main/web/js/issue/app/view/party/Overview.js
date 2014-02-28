Ext.define('Mtr.view.party.Overview', {
    extend: 'Ext.container.Container',
    alias: 'widget.partyOverview',
    title: 'Party overview',

    layout: 'border',

    requires: [
        'Uni.view.breadcrumb.Trail'
    ],

    items: [
        {
            xtype: 'breadcrumbTrail',
            region: 'north',
            padding: 6
        },
        {
            xtype: 'container',
            region: 'west',
            cls: 'filter-form-wrapper',
            items: [
                {
                    xtype: 'partyFilter',
                    name: 'filter'
                }
            ]
        },
        {
            xtype: 'partyBrowse',
            name: 'list',
            region: 'center'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});