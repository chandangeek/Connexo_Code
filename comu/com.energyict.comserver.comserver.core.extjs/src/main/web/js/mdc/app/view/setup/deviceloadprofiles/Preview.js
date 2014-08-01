Ext.define('Mdc.view.setup.deviceloadprofiles.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesPreview',
    itemId: 'deviceLoadProfilesPreview',
    requires: [
        'Uni.form.field.ObisDisplay',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLoadProfilesActionMenu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'deviceLoadProfilesPreviewForm',
        defaults: {
            xtype: 'displayfield',
            labelWidth: 200
        },
        items: [
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.name', 'MDC', 'Name'),
                name: 'name'
            },
            {
                xtype: 'obis-displayfield',
                name: 'obisCode'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                name: 'interval_formatted'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.lastReading', 'MDC', 'Last reading'),
                name: 'lastReading_formatted'
            },
            {
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.channels', 'MDC', 'Channels'),
                name: 'channels',
                renderer: function (value, field) {
                    var result = '',
                        mRID,
                        id;
                    if (value) {
                        mRID = field.up('#deviceLoadProfilesSetup').mRID;
                        id = field.up('#deviceLoadProfilesPreviewForm').getRecord().getId();
                        Ext.isArray(value) && Ext.Array.each(value, function (channel) {
                            result += '<a href="#/devices/' + mRID + '/loadprofiles/' + id + '/channels/' + channel + '"> ' + channel + '</a><br>'
                        });
                    }
                    return result;
                }
            }
        ]
    }

});
