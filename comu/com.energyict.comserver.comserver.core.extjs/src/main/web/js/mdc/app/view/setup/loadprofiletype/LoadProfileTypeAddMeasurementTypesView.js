Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddMeasurementTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddMeasurementTypesView',
    itemId: 'loadProfileTypeAddMeasurementTypesView',

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            padding: '0 10 0 10',
            items: [
                {
                    xtype: 'component',
                    margins: '10 10 10 10',
                    html: '<h1>Add measurement types</h1>'
                },
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
                },
                {
                    xtype: 'toolbar',
                    border: 0,
                    items: [
                        {
                            xtype: 'button',
                            name: 'addmeasurementtypestoloadprofiletype',
                            text: 'Add',
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            handler:function(button,event){
                                Ext.History.back();
                            },
                            ui: 'link'
                        }
                    ]
                }


            ]

        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});

