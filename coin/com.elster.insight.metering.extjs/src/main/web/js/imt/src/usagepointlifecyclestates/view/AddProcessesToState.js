Ext.define('Imt.usagepointlifecyclestates.view.AddProcessesToState', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-processes-to-state',
    xtype: 'add-processes-to-state',
    requires: ['Imt.usagepointlifecyclestates.view.AddProcessesToStateGrid',
        'Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses'],
    overflowY: true,
    storeToUpdate: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('transitionBusinessProcess.addProcesses', 'IMT', 'Add processes'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    itemId: 'add-process-to-state-errors',
                    xtype: 'uni-form-error-message',
                    hidden: true
                },
                {
                    xtype: 'preview-container',
                    itemId: 'gridContainer',
                    selectByDefault: false,
                    grid: {
                        xtype: 'add-process-to-state-selection-grid',
                        itemId: 'add-process-grid',
                        store: 'Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        margin: '15 0 20 0',
                        title: Uni.I18n.translate('transitionBusinessProcess.empty.title', 'IMT', 'No processes found.'),
                        reasons: [
                            Uni.I18n.translate('transitionBusinessProcess.empty.list.item1', 'IMT', 'No processes are defined yet'),
                            Uni.I18n.translate('transitionBusinessProcess.empty.list.item2', 'IMT', 'All processes are already added to the state.')
                        ]
                    }
                },
                {
                    xtype: 'container',
                    margin: '-20 0 10 0',
                    itemId: 'add-process-to-state-selection-error',
                    hidden: true,
                    html: '<span style="color: #eb5642">' + Uni.I18n.translate('transitionBusinessProcess.no.processes.selected', 'IMT', 'Select at least 1 process') + '</span>'
                },
                {
                    xtype: 'toolbar',
                    fieldLabel: '&nbsp',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    itemId: 'buttonsContainer',
                    defaults: {
                        xtype: 'button'
                    },
                    items: [
                        {
                            text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                            name: 'add',
                            itemId: 'btn-add-process',
                            action: 'addTransitionBusinessProcess',
                            ui: 'action'
                        },
                        {
                            name: 'cancel',
                            itemId: 'lnk-cancel-process',
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ],
    initComponent: function () {
        var me = this,
            availableProcesses = Ext.data.StoreManager.lookup('Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses');

        me.callParent(arguments);
        if (availableProcesses) {
            availableProcesses.load(function (records, operation, success) {
                if (success) {
                    if (me.storeToUpdate) {
                        var usedProcessesIds = Ext.Array.map(me.storeToUpdate.data.items, function (item) {
                            return item.get("id");
                        });
                        availableProcesses.filterBy(function (record) {
                            return (Ext.Array.indexOf(usedProcessesIds, record.get("id")) === -1);
                        });
                        // filtering can result to empty grid: gridContainer acts on this by showing 'empty component'
                        me.down('#gridContainer').onChange(availableProcesses);
                        me.down('#btn-add-process').setVisible(availableProcesses.count() !== 0);
                        me.down('#lnk-cancel-process').setVisible(availableProcesses.count() !== 0);
                    }
                } else {
                    // show gridContainer's 'empty component'
                    me.down('#gridContainer').onLoad(availableProcesses, records);
                    Ext.suspendLayouts();
                    me.down('#btn-add-process').setVisible(false);
                    me.down('#lnk-cancel-process').setVisible(false);
                    Ext.resumeLayouts(true);
                }
            })
        }
    },
    getSelection: function () {
        var grid = this.down('#add-process-grid');
        return grid.getSelectionModel().getSelection();
    }
});