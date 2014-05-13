Ext.define('Mdc.model.CommunicationSchedule',{
      extend: 'Ext.data.Model',
      fields: [
          {name: 'id', type: 'int', useNull: true},
          {name:'name', type: 'string', useNull: true},
          {name: 'temporalExpression'},
          {name: 'plannedDate'},
          {name: 'isInUse',type: 'boolean'},
          {name: 'schedulingStatus', type: 'string'},
          {name: 'startDate'}
      ],
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json'
        }
    }
});
