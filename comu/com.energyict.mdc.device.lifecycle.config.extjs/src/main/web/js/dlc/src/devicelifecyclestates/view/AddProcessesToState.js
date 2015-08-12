Ext.define('Dlc.devicelifecyclestates.view.AddProcessesToState', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.AddProcessesToState',
    requires: ['Dlc.devicelifecyclestates.view.AddProcessesToStateGrid',
               'Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses'],
    overflowY: true,
    storeToUpdate: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('transitionBusinessProcess.addToState', 'DLC', 'Add processes'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'preview-container',
                    itemId: 'gridContainer',
                    selectByDefault: false,
                    grid: {
                        xtype: 'add-process-to-state-selection-grid',
                        itemId: 'add-process-grid',
                        store: 'Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses',
                        height: 600
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('transitionBusinessProcess.empty.title', 'DLC', 'No processes found.'),
                        reasons: [
                            Uni.I18n.translate('transitionBusinessProcess.empty.list.item1', 'DLC', 'No processes are defined yet'),
                            Uni.I18n.translate('transitionBusinessProcess.empty.list.item2', 'DLC', 'All processes are already added to the state.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-process',
                            action: 'addTransitionBusinessProcess',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-process',
                            text: Uni.I18n.translate('general.add', 'DLC', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
             ]
        }
    ],
    initComponent: function(){
        var me = this,
           availableProcesses = Ext.data.StoreManager.lookup('Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses');

        me.callParent(arguments);
        if (availableProcesses) {
            availableProcesses.load(function(records, operation, success){
               if (success) {
                   if (me.storeToUpdate) {
                       var usedProcessesIds = Ext.Array.map(me.storeToUpdate.data.items, function(item){
                           return item.get("id");
                       });
                       availableProcesses.filterBy(function (record) {
                           return (Ext.Array.indexOf(usedProcessesIds, record.get("id")) === -1);
                       });
                       // filtering can result to empty grid: gridContainer acts on this by showing 'empty component'
                       me.down('#gridContainer').onChange(availableProcesses);
                       me.down('#btn-add-process').setVisible(availableProcesses.count() !== 0);
                   }
               }else{
                   console.warn("Available business processes could not be loaded");
                   // show gridContainer's 'empty component'
                   me.down('#gridContainer').onLoad(availableProcesses, records);
                   me.down('#btn-add-process').setVisible(false);
               }
            })
        }
    },
    getSelection: function(){
        var grid = this.down('#add-process-grid');
        return grid.getSelectionModel().getSelection();
    }
});