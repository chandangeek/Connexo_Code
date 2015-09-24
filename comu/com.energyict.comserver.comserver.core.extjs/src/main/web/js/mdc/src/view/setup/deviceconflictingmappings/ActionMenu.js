Ext.define('Mdc.view.setup.deviceconflictingmappings.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-conflicting-mapping-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'btn-solve-device-config',
            text: Uni.I18n.translate('deviceConflictingMappings.solve', 'MDC', 'Solve'),
            action: 'solveConflictingMapping',
            visible: function () {
                return !this.record.get('isSolved');
            }
        },
        {
            itemId: 'btn-edit-device-config',
            text: Uni.I18n.translate('deviceConflictingMappings.edit', 'MDC', 'Edit'),
            action: 'editConflictingMapping',
            visible: function () {
                return this.record.get('isSolved');
            }
        }
    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
            })
        }
    }
});
