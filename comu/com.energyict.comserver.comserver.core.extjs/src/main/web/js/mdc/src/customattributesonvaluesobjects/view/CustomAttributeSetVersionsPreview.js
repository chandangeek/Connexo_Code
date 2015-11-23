Ext.define('Mdc.customattributesonvaluesobjects.view.CustomAttributeSetVersionsPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.custom-attribute-set-versions-preview',
    itemId: 'custom-attribute-set-versions-preview-id',
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
                text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'time-sliced-custom-attribute-set-action-menu',
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
        Ext.suspendLayouts();
        this.setTitle(record.get('period'));
        this.down('property-form').loadRecord(record);
        this.down('time-sliced-custom-attribute-set-action-menu').record = record;
        Ext.resumeLayouts(true);
    }
});