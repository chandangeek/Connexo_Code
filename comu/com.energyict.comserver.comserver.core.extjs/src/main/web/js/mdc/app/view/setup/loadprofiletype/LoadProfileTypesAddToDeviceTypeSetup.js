Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypesAddToDeviceTypeSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypesAddToDeviceTypeSetup',
    itemId: 'loadProfileTypesAddToDeviceTypeSetup',
    deviceTypeId: null,
    intervalStore: null,

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
                    html: '<h1>' + Uni.I18n.translate('loadprofiletype.addloadprofiletypes', 'MDC', 'Add load profile types') + '</h1>'
                },
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
                    xtype: 'toolbar',
                    border: 0,
                    items: [
                        {
                            xtype: 'button',
                            name: 'addloadprofiletypestodevicetype',
                            text: 'Add',
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            ui: 'link',
                            handler:function(button,event){
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

