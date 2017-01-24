Ext.define('Mdc.view.setup.deviceconflictingmappings.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-conflicting-mapping-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'btn-solve-device-config',
                text: Uni.I18n.translate('deviceConflictingMappings.solve', 'MDC', 'Solve'),
                action: 'solveConflictingMapping',
                visible: function () {
                    return !this.record.get('isSolved');
                },
                section: this.SECTION_ACTION
            },
            {
                itemId: 'btn-edit-device-config',
                text: Uni.I18n.translate('deviceConflictingMappings.edit', 'MDC', 'Edit'),
                action: 'editConflictingMapping',
                visible: function () {
                    return this.record.get('isSolved');
                },
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
            })
        }
    }
});
