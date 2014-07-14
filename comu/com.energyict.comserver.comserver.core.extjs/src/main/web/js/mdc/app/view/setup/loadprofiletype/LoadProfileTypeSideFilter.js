Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypeSideFilter',
    title: 'Filter',
    width: 200,
    ui: 'filter',

    items: [
        {
            xtype: 'filter-form',
            items: [
                {
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: 'Name',
                            labelAlign: 'top'
                        },
                        {
                            xtype: 'textfield',
                            name: 'obis',
                            fieldLabel: 'OBIS code',
                            labelAlign: 'top'
                        }
                    ]
                }
            ]
        }
    ],

    buttons: [
        {
            text: 'Apply',
            action: 'applyloadprofiletypefilter'
        },
        {
            text: 'Reset',
            action: 'reset'
        }
    ]
});