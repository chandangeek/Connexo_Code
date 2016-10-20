Ext.define('Cal.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'activateDeactivate',
            text: Uni.I18n.translate('general.deActivate', 'CAL', 'Deactivate'),
            action: 'activateDeactivate',
            privileges: Cal.privileges.Calendar.admin,
            activateDeactivate: function(){
                return this.record.get('status').id;
            }
        },
        {
            itemId: 'view-preview-cal',
            text: Uni.I18n.translate('general.viewPreview', 'CAL', 'View preview'),
            action: 'viewpreview'
        },
        {
            itemId: 'remove-preview-cal',
            text: Uni.I18n.translate('general.remove', 'CAL', 'Remove'),
            privileges: Cal.privileges.Calendar.admin,
            action: 'remove'
        }
    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if(item.activateDeactivate !== undefined){
                    item.text = item.activateDeactivate.call(me)==='ACTIVE'?Uni.I18n.translate('general.deActivate', 'CAL', 'Deactivate'):Uni.I18n.translate('general.activate', 'CAL', 'Activate');
                }
            })
        }
    }
});