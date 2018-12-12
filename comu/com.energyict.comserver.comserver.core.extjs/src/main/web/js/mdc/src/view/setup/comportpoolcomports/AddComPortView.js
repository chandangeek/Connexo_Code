/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortView', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComportToComportPoolView',
    itemId: 'addComportToComportPoolView',
    requires: [
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],
    config:{
        poolId: null,
        comportPoolStore: null
    },
    initComponent: function () {
        var me = this;
        this.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'comportpoolsidemenu',
                    itemId: 'me',
                    poolId: me.poolId
                }
            ]
        };
        this.content = [
                {
                    xtype: 'panel',
                    ui: 'large',
                    title: Uni.I18n.translate('comPortPoolComPort.addComPort', 'MDC', 'Add communication port'),
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items: [
                        {
                            xtype: 'comport-selection-grid',
                            itemid: 'addComportToComportPoolGrid',
                            comportPoolStore: me.comportPoolStore,
                            maxHeight: 450,
                            flex: 3
                        },
                        {
                            xtype: 'container',
                            defaults: {
                                labelWidth: 80,
                                xtype: 'button',
                                flex: 1
                            },
                            items: [
                                {
                                    itemId: 'addButton',
                                    text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                    ui: 'action',
                                    disabled: true
                                },
                                {
                                    itemId: 'cancelButton',
                                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                    ui: 'link',
                                    href: '#/administration/comportpools/'+ me.poolId +'/comports'
                                }
                            ]
                        }
                    ]
                }
            ];

        me.callParent(arguments);
        var grid = me.down('#addComportToComportPoolGrid'),
            addButton = me.down('#addButton');

        grid.setSelectionListeners([{
            onSelectChange : function(view, selections, options) {
                var toggleFn = selections.length === 0 ? 'disable' : 'enable';
                addButton[toggleFn]();
            }
        }]) ;
        addButton.on('click', me.onAddButtonClick, me);

    },
    onAddButtonClick: function () {
        this.fireEvent('selecteditemsadd', this.down('#addComportToComportPoolGrid').getSelection());
    },
    noItemsAvailable: function () {
        this.down('#addComportToComportPoolGrid').getLayout().setActiveItem(0);
        this.down('#addButton').setVisible(false);
    },

    showPoolColumn: function() {
        this.down('#addComportToComportPoolGrid').showPoolColumn();
    }
});