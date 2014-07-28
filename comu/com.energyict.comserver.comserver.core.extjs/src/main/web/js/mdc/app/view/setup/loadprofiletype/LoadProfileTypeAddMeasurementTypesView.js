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
                            itemId: 'radioAll',
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
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            name: 'addmeasurementtypestoloadprofiletype',
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            handler: function (button, event) {
                                Ext.History.back();
                            }
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

