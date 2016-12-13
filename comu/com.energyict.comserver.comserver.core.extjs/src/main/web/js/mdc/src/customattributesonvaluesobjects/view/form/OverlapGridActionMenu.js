Ext.define('Mdc.customattributesonvaluesobjects.view.form.OverlapGridActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.versions-overlap-grid-action-menu',
    itemId: 'versions-overlap-grid-action-menu-id',
    metaData: null,

    initComponent: function () {
        var me = this,
            recordPosition = me.recordIndex + 1,
            store = me.metaData.record.store,
            storeLength = store.getCount(),
            recordsAfter = storeLength - recordPosition,
            leftRecord,
            rightRecord;

        me.items = [];

        if (recordPosition > 1) {
            leftRecord = store.getAt(me.recordIndex -1);
            me.items.push({
                text: Uni.I18n.translate('customattributesonvaluesobjects.alignleft', 'MDC', 'Align left'),
                itemId: 'custom-attribute-set-version-action-menu-align-left-btn-id',
                handler: function() {
                    Ext.ComponentQuery.query('#custom-attribute-set-versions-overlap-grid-id')[0].fireEvent('alignleft', leftRecord.get('endTime'));
                },
                section: this.SECTION_ACTION
            });
        }

        if (recordsAfter > 0) {
            rightRecord = store.getAt(me.recordIndex +1);
            me.items.push({
                text: Uni.I18n.translate('customattributesonvaluesobjects.alignright', 'MDC', 'Align right'),
                itemId: 'custom-attribute-set-version-action-menu-align-right-btn-id',
                handler: function() {
                    Ext.ComponentQuery.query('#custom-attribute-set-versions-overlap-grid-id')[0].fireEvent('alignright', rightRecord.get('startTime'));
                },
                section: this.SECTION_ACTION
            });
        }

        me.callParent(arguments);
    }
});