Ext.define('Imt.usagepointmanagement.view.forms.attributes.CustomAttributeSetDisplayForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.property.form.Property'
    ],
    alias: 'widget.custom-attribute-set-display-form',

    router: null,
    viewDefaults: {},

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'property-form',
                itemId: 'property-form',
                isEdit: false,
                defaults: {
                    labelWidth: me.viewDefaults.labelWidth
                }
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this,
            startTime,
            endTime,
            isActive,
            period;

        Ext.suspendLayouts();
        if (record.get('isVersioned')) {
            startTime = record.get('startTime');
            endTime = record.get('endTime');
            isActive = record.get('isActive');

            if (isActive) {
                if (startTime || endTime) {
                    if (!endTime) {
                        period = Uni.I18n.translate('general.FromX', 'IMT', 'From {0}', [Uni.DateTime.formatDateTimeShort(new Date(startTime))], false);
                    } else if (!startTime) {
                        period = Uni.I18n.translate('general.untilX', 'IMT', 'Until {0}', [Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
                    } else {
                        period = Uni.I18n.translate('general.fromUntilX', 'IMT', 'From {0} - Until {1}', [Uni.DateTime.formatDateTimeShort(new Date(startTime)), Uni.DateTime.formatDateTimeShort(new Date(endTime))], false);
                    }
                } else {
                    period = Uni.I18n.translate('general.infinite', 'IMT', 'Infinite');
                }

                me.insert(0, {
                    xtype: 'component',
                    itemId: 'custom-attribute-set-version-period',
                    cls: 'instructional-note',
                    margin: '0 0 15 9',
                    html: period
                });

                me.down('#property-form').loadRecord(record);
            } else {
                me.removeAll();
                me.add({
                    xtype: 'component',
                    itemId: 'custom-attribute-set-no-active-version',
                    cls: 'instructional-note',
                    margin: '5 0 15 9',
                    html: '(' + Uni.I18n.translate('general.noActiveVersion', 'IMT', 'No active version') + ')'
                });
            }

            me.add({
                xtype: 'button',
                itemId: 'custom-attribute-set-versions-link',
                text: Uni.I18n.translate('general.versions', 'IMT', 'Versions'),
                ui: 'link',
                href: me.router.getRoute('usagepoints/view/history').buildUrl(me.router.arguments, {customAttributeSetId: record.getId()})
            });
        } else {
            me.down('#property-form').loadRecord(record);
        }
        Ext.resumeLayouts(true);

        me.callParent(arguments);
    }
});