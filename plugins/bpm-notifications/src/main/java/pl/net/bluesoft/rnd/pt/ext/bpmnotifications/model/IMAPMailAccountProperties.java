package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.entry.Ext;

/**
 * Created by Maciej on 2014-11-12.
 */
public class IMAPMailAccountProperties
{
    private String complaintType;

    private String mailProtocol;

    private String mail;

    private String mailHost;

    private String mailPort;

    private String mailUser;

    private String mailPass;

    private String mailToProcessFolder;

    private String mailProcessedFolder;

    private String mailErrorFolder;

    private String mailStoreClass;

    private String mailAuthMechanism;

    private String mailNTLMDomain;

    private String mailSocketFactoryClass;

    public String getMailProtocol() {
        return mailProtocol;
    }

    public void setMailProtocol(String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getMailPort() {
        return mailPort;
    }

    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    public String getMailPass() {
        return mailPass;
    }

    public void setMailPass(String mailPass) {
        this.mailPass = mailPass;
    }

    public String getMailToProcessFolder() {
        return mailToProcessFolder;
    }

    public void setMailToProcessFolder(String mailToProcessFolder) {
        this.mailToProcessFolder = mailToProcessFolder;
    }

    public String getMailProcessedFolder() {
        return mailProcessedFolder;
    }

    public void setMailProcessedFolder(String mailProcessedFolder) {
        this.mailProcessedFolder = mailProcessedFolder;
    }

    public String getMailErrorFolder() {
        return mailErrorFolder;
    }

    public void setMailErrorFolder(String mailErrorFolder) {
        this.mailErrorFolder = mailErrorFolder;
    }

    public String getMailStoreClass() {
        return mailStoreClass;
    }

    public void setMailStoreClass(String mailStoreClass) {
        this.mailStoreClass = mailStoreClass;
    }

    public String getMailAuthMechanism() {
        return mailAuthMechanism;
    }

    public void setMailAuthMechanism(String mailAuthMechanism) {
        this.mailAuthMechanism = mailAuthMechanism;
    }

    public String getMailNTLMDomain() {
        return mailNTLMDomain;
    }

    public void setMailNTLMDomain(String mailNTLMDomain) {
        this.mailNTLMDomain = mailNTLMDomain;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setProfileName(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getMailSocketFactoryClass() {
        return mailSocketFactoryClass;
    }

    public void setMailSocketFactoryClass(String mailSocketFactoryClass) {
        this.mailSocketFactoryClass = mailSocketFactoryClass;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}