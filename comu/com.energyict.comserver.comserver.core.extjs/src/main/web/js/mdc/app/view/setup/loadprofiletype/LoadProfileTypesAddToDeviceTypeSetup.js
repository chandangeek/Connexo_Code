Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypesAddToDeviceTypeSetup',
    itemId: 'loadProfileTypesAddToDeviceTypeSetup',
    deviceTypeId: null,
    intervalStore: null,

    content: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            ui: 'large',
            title: Uni.I18n.translate('loadprofiletype.addloadprofiletypes', 'MDC', 'Add load profile types'),
            items: [
                {
                    xtype: 'radiogroup',
                    name: 'allOrSelectedLoadProfileTypes',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            boxLabel: '<b>All load profile types</b><br/>' +
                                '<span style="color: grey;">Select all items (related to filters on previous screen)</span>',
                            name: 'loadProfileTypeRange',
                            checked: true,
                            inputValue: 'ALL'
                        },
                        {
                            boxLabel: '<b>Selected load profile types</b><br/><span style="color: grey;">Select items in table</span>',
                            name: 'loadProfileTypeRange',
                            inputValue: 'SELECTED'
                        }
                    ]
                },
                {
                    xtype: 'loadProfileTypesAddToDeviceTypeDockedItems'
                },
                {
                    xtype: 'container',
                    itemId: 'loadProfileTypesAddToDeviceTypeGridContainer'
                },
                {
                    xtype: 'container',
                    items: [
                        {
                            xtype: 'button',
                            name: 'addloadprofiletypestodevicetype',
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
        this.down('#loadProfileTypesAddToDeviceTypeGridContainer').add(
            {
                xtype: 'loadProfileTypesAddToDeviceTypeGrid',
                intervalStore: this.intervalStore
            }
        );
    }
});

