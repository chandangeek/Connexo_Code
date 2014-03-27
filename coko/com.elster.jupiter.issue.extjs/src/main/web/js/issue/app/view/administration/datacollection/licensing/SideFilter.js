Ext.define('Isu.view.administration.datacollection.licensing.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.licensing-side-filter',
    title: 'Filter',
    cls: 'filter-form',
    width: 180,

    requires: [
        'Uni.component.filter.view.Filter'
    ],

    items: [
        {
            xtype: 'filter-form',
            items: [
                {
                    xtype: 'combobox',
                    name: 'license',
                    fieldLabel: 'License',
                    labelAlign: 'top',

                    displayField: 'name',
                    valueField: 'id',
                    forceSelection: true,
                    store: 'Isu.store.Licensing',

                    listConfig: {
                        cls: 'isu-combo-color-list',
                        emptyText: 'No license found'
                    },

                    queryMode: 'remote',
                    queryParam: 'like',
                    queryDelay: 100,
                    queryCaching: false,
                    minChars: 1,

                    triggerAction: 'query',
                    anchor: '100%'
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'filter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ]
});