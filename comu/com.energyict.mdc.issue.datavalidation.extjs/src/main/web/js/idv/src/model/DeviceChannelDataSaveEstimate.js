Ext.define('Idv.model.DeviceChannelDataSaveEstimate', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.property.model.Property'
    ],

    fields: [
        'estimatorImpl',
        'estimateBulk',
        'intervals',
        'readingType'
    ],

    associations: [
        {
            name: 'properties',
            type: 'hasMany',
            model: 'Uni.property.model.Property',
            associationKey: 'properties',
            foreignKey: 'properties',
            getTypeDiscriminator: function (node) {
                return 'Uni.property.model.Property';
            }
        }
    ],

    proxy: {
        type: 'rest',
        urlTpl: '/api/ddr/devices/{mRID}/channels/{channelId}/data/issue/estimate',
        reader: {
            type: 'json'
        },

        setUrl: function (params) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(params.mRID)).replace('{channelId}', params.channelId);
        }
    }
});

