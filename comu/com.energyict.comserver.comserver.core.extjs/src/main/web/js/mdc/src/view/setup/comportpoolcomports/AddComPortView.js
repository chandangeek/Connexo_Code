Ext.define('Mdc.view.setup.comportpoolcomports.AddComPortView', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'addComportToComportPoolView',
    itemId: 'addComportToComportPoolView',
    requires: [
        'Mdc.view.setup.comportpoolcomports.AddComPortGrid'
    ],
    config:{
        poolId: null
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
                            maxHeight: 450,
                            flex: 3 ,
                            selectionListeners: [this.updateAddButton]
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
                                    text: Uni.I18n.translate('general.add', 'UNI', 'Add'),
                                    ui: 'action',
                                    disabled: true /*,
                                    listeners:[{click: {fn: me.onAddButtonClick}}] */
                                },
                                {
                                    itemId: 'cancelButton',
                                    text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
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
    }
});