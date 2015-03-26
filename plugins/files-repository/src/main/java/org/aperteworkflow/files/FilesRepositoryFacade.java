package org.aperteworkflow.files;

import org.aperteworkflow.files.dao.FilesRepositoryItemDAO;
import org.aperteworkflow.files.dao.FilesRepositoryItemDAOImpl;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAO;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAOImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactory;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactoryImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryFacade implements IFilesRepositoryFacade {

    private static Logger logger = Logger.getLogger(FilesRepositoryFacade.class.getName());
    private ProcessInstanceDAO customProcessInstanceDAO;

    private FilesRepositoryConfigFactory configFactory;
    private Session customSession;

    public FilesRepositoryFacade() {
        this(null, new FilesRepositoryConfigFactoryImpl(), null);
    }


    public FilesRepositoryFacade(Session customSession, FilesRepositoryConfigFactory configFactory, ProcessInstanceDAO customProcessInstanceDAO) {
        this.customSession = customSession;
        this.configFactory = configFactory;
        this.customProcessInstanceDAO = customProcessInstanceDAO;
    }

    private FilesRepositoryItemDAO getFilesRepositoryItemDAO() {
        Session sessionToUse = getSession();
        ProcessInstanceDAO piDaoToUse = getProcessInstanceDAO();
        return new FilesRepositoryItemDAOImpl(sessionToUse, piDaoToUse);
    }

    private ProcessInstanceDAO getProcessInstanceDAO() {
        return customProcessInstanceDAO != null ? customProcessInstanceDAO : ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO();
    }

    private Session getSession() {
        return customSession != null ? customSession : ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
    }

    private FilesRepositoryStorageDAO getFilesRepositoryStorageDAO() {
        return new FilesRepositoryStorageDAOImpl(getStorageConfig());
    }

    private FilesRepositoryStorageConfig getStorageConfig() {
        return configFactory.createFilesRepositoryStorageConfig();
    }

    private Long getProcessInstanceRoot(Long processInstanceId){
        ProcessInstanceDAO dao = getProcessInstanceDAO();
        return dao.getProcessInstance(processInstanceId).getRootProcessInstance().getId();
    }

    @Override
    public IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, Long processInstanceId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException {
        FilesRepositoryItem result;
       processInstanceId= getProcessInstanceRoot(processInstanceId);
        String filePath = prepareFilePath(processInstanceId, fileName);

        try {
            getFilesRepositoryStorageDAO().uploadFileToStorage(inputStream, filePath);
        } catch (IOException e) {
            throw new UploadFileException("Cannot write file to storage", e);
        }
        result = getFilesRepositoryItemDAO().addItem(processInstanceId, fileName, fileDescription, filePath, contentType, creatorLogin);
        return result;
    }

    private String prepareFilePath(Long processInstanceId, String fileName) {
        processInstanceId= getProcessInstanceRoot(processInstanceId);
        return processInstanceId + File.separator +  System.currentTimeMillis() + "_" + fileName;
    }

    @Override
    public void deleteFile(Long processInstanceId, String filesRepositoryItemId) throws DeleteFileException
    {
        Long fileId = Long.parseLong(filesRepositoryItemId);
        processInstanceId= getProcessInstanceRoot(processInstanceId);
        FilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(fileId);
        if (filesRepositoryItem == null) {
            throw new DeleteFileException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        if (filesRepositoryItem.getProcessInstance() == null || !filesRepositoryItem.getProcessInstance().getId().equals(processInstanceId)) {
            throw new DeleteFileException("File from repository. File item is not connected to processInstanceId=[" + processInstanceId + "].");
        }
        getFilesRepositoryItemDAO().deleteById(fileId);
        try {
            getFilesRepositoryStorageDAO().deleteFileFromStorage(new File(filesRepositoryItem.getRelativePath()));
        } catch (IOException e) {
            throw new DeleteFileException("File from path=[" + filesRepositoryItem.getRelativePath() + "].", e);
        }
    }

    @Override
    public FileItemContent downloadFile(Long processInstanceId, String filesRepositoryItemId) throws DownloadFileException {
        processInstanceId= getProcessInstanceRoot(processInstanceId);
        FilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(Long.parseLong(filesRepositoryItemId));
        if (filesRepositoryItem == null) {
            throw new DownloadFileException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        if (filesRepositoryItem.getProcessInstance() == null || !filesRepositoryItem.getProcessInstance().getId().equals(processInstanceId)) {
            throw new DownloadFileException("File item is not connected to process instance. ProcessInstanceId=[" + processInstanceId + "] and filesRepositoryItemId=["+filesRepositoryItemId+"].");
        }
        try {
            FileItemContent content = getFilesRepositoryStorageDAO().loadFileFromStorage(filesRepositoryItem.getRelativePath());
            content.setName(filesRepositoryItem.getName());
            content.setContentType(filesRepositoryItem.getContentType());
            return content;
        } catch (IOException e) {
            throw new DownloadFileException("File item download problem for processInstanceId=[" + processInstanceId + "] and filesRepositoryItemId=["+filesRepositoryItemId+"].", e);
        }
    }

    @Override
    public Collection<IFilesRepositoryItem> getFilesList(Long processInstanceId) {
        processInstanceId= getProcessInstanceRoot(processInstanceId);
        return new ArrayList<IFilesRepositoryItem>(getFilesRepositoryItemDAO().getItemsFor(processInstanceId));
    }

    @Override
    public void updateDescription(Long processInstanceId, String filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException {
        Long fileId = Long.parseLong(filesRepositoryItemId);
        processInstanceId= getProcessInstanceRoot(processInstanceId);
        FilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(fileId);
        if (filesRepositoryItem == null) {
            throw new UpdateDescriptionException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        if (filesRepositoryItem.getProcessInstance() == null || !filesRepositoryItem.getProcessInstance().getId().equals(processInstanceId)) {
            throw new UpdateDescriptionException("File item is not connected to process instance. ProcessInstanceId=[" + processInstanceId + "] and fileRepositoryItemId=["+filesRepositoryItemId+"].");
        }
        getFilesRepositoryItemDAO().updateDescriptionById(fileId, fileDescription);
    }

}
