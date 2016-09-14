-- upgrade jbpm 6.1 to 6.2
ALTER TABLE SessionInfo MODIFY ( id NUMBER(19, 0) );
ALTER TABLE AuditTaskImpl MODIFY ( processSessionId NUMBER(19, 0) );
ALTER TABLE AuditTaskImpl MODIFY ( activationTime TIMESTAMP );
ALTER TABLE AuditTaskImpl MODIFY ( createdOn TIMESTAMP );
ALTER TABLE AuditTaskImpl MODIFY ( dueDate TIMESTAMP );
ALTER TABLE ContextMappingInfo MODIFY ( KSESSION_ID NUMBER(19, 0) );
ALTER TABLE Task MODIFY ( processSessionId NUMBER(19, 0) );

CREATE TABLE DeploymentStore (
  id             NUMBER(19, 0) NOT NULL,
  attributes     VARCHAR2(255 CHAR),
  DEPLOYMENT_ID  VARCHAR2(255 CHAR),
  deploymentUnit CLOB,
  state          NUMBER(10, 0),
  updateDate     TIMESTAMP,
  PRIMARY KEY (id)
);
ALTER TABLE DeploymentStore ADD CONSTRAINT UK_DeploymentStore_1 UNIQUE (DEPLOYMENT_ID);
CREATE SEQUENCE DEPLOY_STORE_ID_SEQ;

ALTER TABLE ProcessInstanceLog ADD processInstanceDescription VARCHAR2(255 CHAR);
ALTER TABLE RequestInfo ADD owner VARCHAR2(255 CHAR);
ALTER TABLE Task ADD (
description VARCHAR2(255 CHAR),
name VARCHAR2(255 CHAR),
subject VARCHAR2(255 CHAR)
);

-- update all tasks with its name, subject and description
UPDATE Task t
SET name = (SELECT shortText
            FROM I18NText
            WHERE Task_Names_Id = t.id);
UPDATE Task t
SET subject = (SELECT shortText
               FROM I18NText
               WHERE Task_Subjects_Id = t.id);
UPDATE Task t
SET description = (SELECT shortText
                   FROM I18NText
                   WHERE Task_Descriptions_Id = t.id);

INSERT INTO AuditTaskImpl (id, activationTime, actualOwner, createdBy, createdOn, deploymentId, description, dueDate, name, parentId, priority, processId, processInstanceId, processSessionId, status, taskId)
  SELECT
    AUDIT_ID_SEQ.nextval,
    activationTime,
    actualOwner_id,
    createdBy_id,
    createdOn,
    deploymentId,
    description,
    expirationTime,
    name,
    parentId,
    priority,
    processId,
    processInstanceId,
    processSessionId,
    status,
    id
  FROM Task;

ALTER TABLE TaskEvent ADD workItemId NUMBER(19, 0);
ALTER TABLE TaskEvent ADD processInstanceId NUMBER(19, 0);
UPDATE TaskEvent t
SET workItemId = (SELECT workItemId
                  FROM Task
                  WHERE id = t.taskId);
UPDATE TaskEvent t
SET processInstanceId = (SELECT processInstanceId
                         FROM Task
                         WHERE id = t.taskId);

-- upgrade jbpm 6.2 to 6.3
ALTER TABLE ProcessInstanceLog ADD correlationKey VARCHAR2(255 CHAR);
ALTER TABLE TaskEvent ADD message VARCHAR2(255 CHAR);

ALTER TABLE AuditTaskImpl ADD workItemId NUMBER(19, 0);
UPDATE AuditTaskImpl a
SET workItemId = (SELECT workItemId
                  FROM Task
                  WHERE id = a.taskId);

CREATE INDEX IDX_PInstLog_correlation ON ProcessInstanceLog (correlationKey);

-- upgrade jbpm 6.3 to 6.4
CREATE TABLE TaskVariableImpl (
  id                NUMBER(19, 0) NOT NULL,
  modificationDate  TIMESTAMP,
  name              VARCHAR2(255 CHAR),
  processId         VARCHAR2(255 CHAR),
  processInstanceId NUMBER(19, 0),
  taskId            NUMBER(19, 0),
  type              NUMBER(10, 0),
  value             VARCHAR2(4000 CHAR),
  PRIMARY KEY (id)
);
CREATE SEQUENCE TASK_VAR_ID_SEQ;

CREATE TABLE QueryDefinitionStore (
  id          NUMBER(19, 0) NOT NULL,
  qExpression CLOB,
  qName       VARCHAR2(255 CHAR),
  qSource     VARCHAR2(255 CHAR),
  qTarget     VARCHAR2(255 CHAR),
  PRIMARY KEY (id)
);

ALTER TABLE QueryDefinitionStore
ADD CONSTRAINT UK_4ry5gt77jvq0orfttsoghta2j UNIQUE (qName);

CREATE SEQUENCE QUERY_DEF_ID_SEQ;