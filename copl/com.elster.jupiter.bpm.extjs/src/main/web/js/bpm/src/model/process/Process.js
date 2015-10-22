Ext.define('Bpm.model.process.Process', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'deploymentId',
            type: 'string'
        },
        {
            name: 'version',
            type: 'string'
        },
        {
            name: 'associated',
            type: 'string'
        },
        {
            name: 'active',
            type: 'string'
        },
        {
            name: 'activeDisplay',
            type: 'string',
            convert: function (value, record) {
                switch(value) {
                    case 'ACTIVE':
                        return Uni.I18n.translate('bpm.process.active', 'BPM', 'Active');
                        break;
                    case 'INACTIVE':
                        return Uni.I18n.translate('bpm.process.inactive', 'BPM', 'Inactive');
                        break;
                    case 'UNDEPLOYED':
                        return Uni.I18n.translate('bpm.process.undeployed', 'BPM', 'Undeployed');
                        break;
                    default:
                        return value;
                }
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '/api/bpm/runtime/process/activate',
        reader: {
            type: 'json'
        }
    }
});