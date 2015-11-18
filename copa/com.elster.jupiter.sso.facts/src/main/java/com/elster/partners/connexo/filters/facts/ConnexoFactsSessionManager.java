package com.elster.partners.connexo.filters.facts;

/**
 * Created by dragos on 11/17/2015.
 */
public class ConnexoFactsSessionManager {
    /*
    // Internal API
    private boolean isInitialized(){
        if(!DBConnectionManager.getInstance().isAppPoolInitialised()) {
            return false;
        }

        return true;
    }

    private void configureSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if(session.getAttribute("SessionData") == null) {

            try {
                DBAction dbAction = new DBAction();

                PersonBean personBean = createUser(dbAction);

                OrganisationManager organisationManager = new OrganisationManager();
                Integer ipOrg = organisationManager.findOrganisationByName(dbAction, "Default");
                OrganisationDisplayBean organisationBean = organisationManager.selectOrgDisplay(dbAction, ipOrg);

                // create personorgrltshp
                //createSession(personBean);

                StaffMemberRoleManager roleManager = new StaffMemberRoleManager();
                roleManager.loadUserRoles();

                StaffHierarchyManager hierarchyManager = new StaffHierarchyManager();
                //hierarchyManager

                SessionBean sessionBean = new SessionBean();
                sessionBean.setPrsnBean(personBean);
                sessionBean.setLoggedOn();
                sessionBean.setCurrRole();
                sessionBean.setUserid("Dragos");
                sessionBean.setOrgBean(organisationBean);
            session.setAttribute("SessionData", sessionBean);
            } catch (ActionErrorsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        HttpSession session = request.getSession();
        if(session.getAttribute("SessionData") == null) {

            SessionBean sessionBean = new SessionBean();
            //sessionBean.set

            session.setAttribute("SessionData", sessionBean);
        }
    }

    private void createSession(PersonBean personBean) {
        DBAction dbAction = null;
        try {
            dbAction = new DBAction();

            //SessionBean sessionBean = new SessionBean();

            ActiveSessionBean activeSession = new ActiveSessionBean();
            activeSession.setCreationTime(Timestamp.from(Instant.now()));
            activeSession.setDeletionTime(Timestamp.from(Instant.now().plusSeconds(60 * 60)));
            activeSession.setHost("LOCAL");
            activeSession.setSessionData("HOST=127.0.0.1");
            activeSession.setEntityId(personBean.getIpPerson());
            activeSession.setEntityName(personBean.getLastName());
            activeSession.setEntityTypeCode("PERSON");
            activeSession.setSessionTypeCode("SESSION");
            activeSession.setSessionCode("FRONTEND");
            activeSession.setDeletionCode("STANDARD");
            activeSession.setParentId(0);
            //activeSession.setSessionId(UUID.randomUUID().toString());
            activeSession.setSessionId(new UUID(8, 8).toString());

            ActiveSessionManager sessionManager = new ActiveSessionManager();
            sessionManager.insertActiveSession(dbAction, activeSession);

            dbAction.commit();

            //authentication.createUniqueUser();

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(dbAction != null) {
                    dbAction.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }



    private PersonBean createUser(DBAction dbAction) {
        PersonBean personBean = null;
        try {
            new IpClassManager();

            personBean = new PersonBean();
            personBean.setIpPerson(SeqManager.getSystemSequence(dbAction));
            personBean.setLastName("Dragos");
            personBean.setFirstName("Sinca");
            personBean.setFullName("Dragos Sinca");
            personBean.setPreferredName("Dragos Sinca");
            personBean.setSoundex("ATMN KNKS");
            personBean.setPreferredLanguageCode("EN");
            personBean.setExposureLevelCode("PRIVATE");
            personBean.setPrivacyLevelCode("4");
            personBean.setPreferredContactMethodCode("EMAIL");
            personBean.setLocalTimeZoneCode("AUSTRALIA/SYDNEY");

            PersonManager personManager = new PersonManager();
            List<PersonBean> foundPersons = personManager.findPerson(dbAction, personBean);

            if(foundPersons.isEmpty()) {

                personManager.insertPerson(dbAction, personBean);
                dbAction.commit();
            }
            else {
                return foundPersons.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(dbAction != null) {
                    dbAction.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return personBean;
    }*/
}
