Ext.define('Mtr.readingtypes.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.reading-types-action-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'reading-types-sorting-menu-activate',
            text: Uni.I18n.translate('readingtypesmanagment.activate', 'MTR', 'Activate'),
            action: 'activate'
        },
        {
            itemId: 'reading-types-sorting-menu-deactivate',
            text: Uni.I18n.translate('readingtypesmanagment.deactivate', 'MTR', 'Deactivate'),
            action: 'deactivate'
        },
        {
            itemId: 'reading-types-action-menu-edit',
            text: Uni.I18n.translate('readingtypesmanagment.edit', 'MTR', 'Edit'),
            action: 'edit'
        }
    ],

    beforeShow: function () {

        var activate = this.down('#reading-types-sorting-menu-activate'),
            deactivate = this.down('#reading-types-sorting-menu-deactivate'),
            active = this.record.get('active');
        if (active) {
            deactivate.show();
            activate.hide();
        } else {
            activate.show();
            deactivate.hide();
        }
    }
});