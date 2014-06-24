Ext.define('Mdc.model.CommunicationSchedule',{
      extend: 'Ext.data.Model',
      fields: [
          {name: 'id', type: 'int', useNull: true},
          {name:'name', type: 'string', useNull: true},
          {name:'mRID', type: 'string', useNull: true},
          {name: 'nextExecutionSpecs'},
          {name: 'plannedDate'},
          {name: 'isInUse',type: 'boolean'},
          {name: 'schedulingStatus', type: 'string'},
          {name: 'startDate'},
          {name: 'comTaskUsages'}
      ],
    associations: [
        {name: 'comTaskUsages', type: 'hasMany', model: 'Mdc.model.CommunicationTask', associationKey: 'comTaskUsages',
            getTypeDiscriminator: function (node) {
                return 'Mdc.model.CommunicationTask';
            }
        }
    ],
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json'
        }
    }
});
