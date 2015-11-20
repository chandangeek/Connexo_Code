Ext.define('Dbp.processes.view.AddPrivilegesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'dbp-add-privileges-setup',
    overflowY: true,
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dbp.processes.view.AddPrivileges'
    ],

    editProcessRecord: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('editProcess.addPrivileges', 'DBP', 'Add privileges'),
                itemId: 'pnl-select-privileges',
                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'dbp-add-privileges-grid',
                            itemId: 'grd-add-privileges',
                            hrefCancel: '',
                            listeners: {
                                selectionchange: {
                                    fn: Ext.bind(me.onSelectionChange, me)
                                }
                            }
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-add-device-states',
                            title: Uni.I18n.translate('editProcess.addPrivileges.empty.title', 'DBP', 'No privileges found'),
                            reasons: [
                                Uni.I18n.translate('editProcess.addPrivileges.empty.list.item1', 'DBP', 'All privileges have been added to the process.')
                            ]
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    onSelectionChange: function (selectionModel, selected) {
        this.down('[action=addSelectedPrivileges]').setDisabled(!selected.length);
    }
});