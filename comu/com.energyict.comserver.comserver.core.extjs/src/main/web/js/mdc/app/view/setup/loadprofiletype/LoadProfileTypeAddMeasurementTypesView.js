Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddMeasurementTypesView',
    itemId: 'loadProfileTypeAddMeasurementTypesView',

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: 'Add measurement types',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'radiogroup',
                    name: 'AllOrSelectedMeasurementTypes',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            boxLabel: '<b>All measurement types</b><br/>' +
                                '<span style="color: grey;">Select all items (related to filters on previous screen)</span>',
                            name: 'measurementTypeRange',
                            checked: true,
                            inputValue: 'ALL'
                        },
                        {
                            boxLabel: '<b>Selected measurement types</b><br/><span style="color: grey;">Select items in table</span>',
                            name: 'measurementTypeRange',
                            inputValue: 'SELECTED'
                        }
                    ]
                },
                {
                    xtype: 'loadProfileTypeAddMeasurementTypesDockedItems'
                },
                {
                    xtype: 'loadProfileTypeAddMeasurementTypesGrid'
                }
            ],
            buttons: [
                {
                    name: 'addmeasurementtypestoloadprofiletype',
                    text: 'Add',
                    ui: 'action'
                },
                {
                    text: 'Cancel',
                    handler: function (button, event) {
                        Ext.History.back();
                    },
                    ui: 'link'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

