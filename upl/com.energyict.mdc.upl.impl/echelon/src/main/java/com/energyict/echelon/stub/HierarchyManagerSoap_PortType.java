/**
 * HierarchyManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface HierarchyManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String retrieveList(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sHierarchyID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveLevelList(java.lang.String sKey, java.lang.String sHierarchyID) throws java.rmi.RemoteException;
    public java.lang.String retrieveMemberList(java.lang.String sKey, java.lang.String sHierarchyID) throws java.rmi.RemoteException;
    public java.lang.String retrieveMembersOfLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String retrieveHierarchyOfLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String retrieveLevelOfMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String retrieveChildLevelOfLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String retrieveParentLevelOfLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String retrieveChildMemberListOfMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String retrieveParentMemberOfMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String create(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String createLevel(java.lang.String sKey, java.lang.String sHierarchyID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String createLevelMember(java.lang.String sKey, java.lang.String sHierarchyLevelID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sHierarchyID) throws java.rmi.RemoteException;
    public java.lang.String deleteLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String deleteLevelMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String update(java.lang.String sKey, java.lang.String sHierarchyID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateLevelMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String moveMemberList(java.lang.String sKey, java.lang.String sParentHierarchyLevelMemberID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveInconsistentLevelMemberList(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String retrieveHierarchyPath(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String retrieveLevel(java.lang.String sKey, java.lang.String sHierarchyLevelID) throws java.rmi.RemoteException;
    public java.lang.String retrieveLevelMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String retrieveTopMembers(java.lang.String sKey, java.lang.String sHierarchyID) throws java.rmi.RemoteException;
}
