Ext.define('Imt.customattributesonvaluesobjects.view.CustomAttributeSetVersionsPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.custom-attribute-set-versions-preview',
    title: '',
    frame: true,
    width: '100%',
    hideAction: false,

    initComponent: function() {
        var me = this;

        me.tools = [
            {
                xtype: 'button',
                hidden: me.hideAction,
                privileges: Imt.privileges.UsagePoint.admin,
                text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'time-sliced-custom-attribute-set-action-menu',
                    itemId: 'time-sliced-custom-attribute-set-action-menu-id',
                    type: me.type
                }
            }
        ];

        me.items = {
            xtype: 'property-form',
            isEdit: false
        };

        me.callParent(arguments);
    },


    loadRecord: function(record) {
        var menu = this.down('#time-sliced-custom-attribute-set-action-menu-id');

        Ext.suspendLayouts();
        this.setTitle(record.get('period'));
        this.down('property-form').loadRecord(record);
        if (menu) {
            menu.record = record;
        }
        Ext.resumeLayouts(true);
    }
});