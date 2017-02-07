/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.object.CancelAllActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.cancel-all-action-menu',
    record: null,
    items: [
        {
            itemId: 'cancel-all-scs',
            text: Uni.I18n.translate('general.cancelAll', 'SCS', 'Cancel all'),
            privileges: Scs.privileges.ServiceCall.admin,
            action: 'cancel-all',
            section: this.SECTION_ACTION
        }
    ],
    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ?  item.show() : item.hide();
                }
            })
        }
    }
});