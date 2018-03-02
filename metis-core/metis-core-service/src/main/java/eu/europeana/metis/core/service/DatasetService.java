package eu.europeana.metis.core.service;import eu.europeana.metis.CommonStringValues;import eu.europeana.metis.authentication.user.AccountRole;import eu.europeana.metis.authentication.user.MetisUser;import eu.europeana.metis.core.dao.DatasetDao;import eu.europeana.metis.core.dao.ScheduledWorkflowDao;import eu.europeana.metis.core.dao.WorkflowExecutionDao;import eu.europeana.metis.core.dataset.Dataset;import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;import eu.europeana.metis.core.exceptions.NoDatasetFoundException;import eu.europeana.metis.exception.BadContentException;import eu.europeana.metis.exception.GenericMetisException;import eu.europeana.metis.exception.UserUnauthorizedException;import java.util.Date;import java.util.List;import java.util.UUID;import org.redisson.api.RLock;import org.redisson.api.RedissonClient;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;/** * Contains business logic of how to manipulate datasets in the system using several components. */@Servicepublic class DatasetService {  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";  private final DatasetDao datasetDao;  private final WorkflowExecutionDao workflowExecutionDao;  private final ScheduledWorkflowDao scheduledWorkflowDao;  private final RedissonClient redissonClient;  /**   * Constructs the service.   *   * @param datasetDao {@link DatasetDao}   * @param workflowExecutionDao {@link WorkflowExecutionDao}   * @param scheduledWorkflowDao {@link ScheduledWorkflowDao}   * @param redissonClient {@link RedissonClient}   */  @Autowired  public DatasetService(DatasetDao datasetDao,      WorkflowExecutionDao workflowExecutionDao,      ScheduledWorkflowDao scheduledWorkflowDao, RedissonClient redissonClient) {    this.datasetDao = datasetDao;    this.workflowExecutionDao = workflowExecutionDao;    this.scheduledWorkflowDao = scheduledWorkflowDao;    this.redissonClient = redissonClient;  }  /**   * Creates a dataset for a specific {@link MetisUser}   *   * @param metisUser the user used to create the dataset   * @param dataset the dataset to be created   * @return the created {@link Dataset} including the extra fields generated from the system   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link DatasetAlreadyExistsException} if the dataset for the same organizationId and datasetName already exists in the system.</li>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public Dataset createDataset(MetisUser metisUser, Dataset dataset)      throws GenericMetisException {    if (metisUser.getAccountRole() == null        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    dataset.setOrganizationId(metisUser.getOrganizationId());    dataset.setOrganizationName(metisUser.getOrganizationName());    //Lock required for find in the next empty datasetId    RLock lock = redissonClient.getFairLock(DATASET_CREATION_LOCK);    lock.lock();    Dataset datasetObjectId;    try {      Dataset storedDataset = datasetDao          .getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),              dataset.getDatasetName());      if (storedDataset != null) {        lock.unlock();        throw new DatasetAlreadyExistsException(String            .format("Dataset with organizationId: %s and datasetName: %s already exists..",                dataset.getOrganizationId(), dataset.getDatasetName()));      }      dataset.setCreatedByUserId(metisUser.getUserId());      dataset.setId(null);      dataset.setUpdatedDate(null);      dataset.setCreatedDate(new Date());      //Add fake ecloudDatasetId to avoid null errors in the database      dataset.setEcloudDatasetId(String.format("NOT_CREATED_YET-%s", UUID.randomUUID().toString()));      int nextInSequenceDatasetId = datasetDao.findNextInSequenceDatasetId();      dataset.setDatasetId(nextInSequenceDatasetId);      datasetObjectId = datasetDao.getById(datasetDao.create(dataset));    } finally {      lock.unlock();    }    return datasetObjectId;  }  /**   * Update an already existent dataset.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param dataset the provided dataset with the changes and the datasetId included in the {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link NoDatasetFoundException} if the dataset for datasetId was not found.</li>   * <li>{@link BadContentException} if the dataset has an execution running.</li>   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>   * <li>{@link DatasetAlreadyExistsException} if the request contains a datasetName change and that datasetName already exists for organizationId of metisUser.</li>   * </ul>   */  public void updateDataset(MetisUser metisUser, Dataset dataset)      throws GenericMetisException {    if (metisUser.getAccountRole() == null        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    dataset.setOrganizationId(metisUser.getOrganizationId());    dataset.setOrganizationName(metisUser.getOrganizationName());    Dataset storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());    if (storedDataset == null) {      throw new NoDatasetFoundException(          String.format("Dataset with datasetId: %s does NOT exist", dataset.getDatasetId()));    }    if (!dataset.getOrganizationId().equals(storedDataset.getOrganizationId())) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    if (!storedDataset.getDatasetName().equals(dataset.getDatasetName()) && datasetDao        .getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),            dataset.getDatasetName()) != null) {      throw new DatasetAlreadyExistsException(String.format(          "Trying to change dataset with datasetName: %s but dataset with organizationId: %s and datasetName: %s already exists",          storedDataset.getDatasetName(), dataset.getOrganizationId(), dataset.getDatasetName()));    }    if (workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()) != null) {      throw new BadContentException(          String.format("Workflow execution is active for datasteId %s", dataset.getDatasetId()));    }    dataset.setCreatedByUserId(storedDataset.getCreatedByUserId());    dataset.setEcloudDatasetId(storedDataset.getEcloudDatasetId());    dataset.setCreatedDate(storedDataset.getCreatedDate());    dataset.setOrganizationId(storedDataset.getOrganizationId());    dataset.setOrganizationName(storedDataset.getOrganizationName());    dataset.setCreatedByUserId(storedDataset.getCreatedByUserId());    dataset.setId(storedDataset.getId());    dataset.setUpdatedDate(new Date());    datasetDao.update(dataset);  }  /**   * Delete a dataset from the system   *   * @param metisUser the {@link MetisUser} to authorize with   * @param datasetId the identifier to find the dataset with   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link BadContentException} if the dataset is has an execution running.</li>   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>   * </ul>   */  public void deleteDatasetByDatasetId(MetisUser metisUser,      int datasetId)      throws GenericMetisException {    if (metisUser.getAccountRole() == null        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    Dataset storedDataset = datasetDao.getDatasetByDatasetId(datasetId);    if (storedDataset == null) {      throw new NoDatasetFoundException(          String.format("No dataset found with datasetId: '%s' in METIS", datasetId));    }    if (!metisUser.getOrganizationId().equals(storedDataset.getOrganizationId())) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    if (workflowExecutionDao.existsAndNotCompleted(datasetId) != null) {      throw new BadContentException(          String.format("Workflow execution is active for datasteId %s", datasetId));    }    datasetDao.deleteByDatasetId(datasetId);    //Clean up dataset leftovers    workflowExecutionDao.deleteAllByDatasetId(datasetId);    scheduledWorkflowDao.deleteAllByDatasetId(datasetId);  }  /**   * Get a dataset from the system using a datasetName   *   * @param metisUser the {@link MetisUser} to authorize with   * @param datasetName the string used to find the dataset with   * @return {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link NoDatasetFoundException} if the dataset is not found in the system.</li>   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>   * </ul>   */  public Dataset getDatasetByDatasetName(MetisUser metisUser,      String datasetName) throws GenericMetisException {    if (metisUser.getAccountRole() == null        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    Dataset storedDataset = datasetDao.getDatasetByDatasetName(datasetName);    if (storedDataset == null) {      throw new NoDatasetFoundException(          String.format("No dataset found with datasetName: '%s' in METIS", datasetName));    }    if (!metisUser.getOrganizationId().equals(storedDataset.getOrganizationId())) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return storedDataset;  }  /**   * Get a dataset from the system using a datasetId   *   * @param metisUser the {@link MetisUser} to authorize with   * @param datasetId the identifier to find the dataset with   * @return {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link NoDatasetFoundException} if the dataset was not found.</li>   * <li>{@link UserUnauthorizedException} if the user is unauthorized.</li>   * </ul>   */  public Dataset getDatasetByDatasetId(MetisUser metisUser,      int datasetId) throws GenericMetisException {    if (metisUser.getAccountRole() == null        || metisUser.getAccountRole() == AccountRole.PROVIDER_VIEWER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    Dataset storedDataset = datasetDao.getDatasetByDatasetId(datasetId);    if (storedDataset == null) {      throw new NoDatasetFoundException(          String.format("No dataset found with datasetId: '%s' in METIS", datasetId));    }    if (!metisUser.getOrganizationId().equals(storedDataset.getOrganizationId())) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return storedDataset;  }  /**   * Get all datasets using the provider field.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param provider the provider string used to find the datasets   * @param nextPage the nextPage token or null   * @return {@link List} of {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public List<Dataset> getAllDatasetsByProvider(      MetisUser metisUser, String provider, int nextPage)      throws GenericMetisException {    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return datasetDao.getAllDatasetsByProvider(provider, nextPage);  }  /**   * Get all datasets using the intermediateProvider field.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param intermediateProvider the intermediateProvider string used to find the datasets   * @param nextPage the nextPage token or null   * @return {@link List} of {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public List<Dataset> getAllDatasetsByIntermediateProvider(      MetisUser metisUser, String intermediateProvider,      int nextPage) throws GenericMetisException {    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return datasetDao.getAllDatasetsByIntermediateProvider(intermediateProvider, nextPage);  }  /**   * Get all datasets using the dataProvider field.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param dataProvider the dataProvider string used to find the datasets   * @param nextPage the nextPage token or null   * @return {@link List} of {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public List<Dataset> getAllDatasetsByDataProvider(      MetisUser metisUser, String dataProvider,      int nextPage) throws GenericMetisException {    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return datasetDao.getAllDatasetsByDataProvider(dataProvider, nextPage);  }  /**   * Get all datasets using the organizationId field.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param organizationId the organizationId string used to find the datasets   * @param nextPage the nextPage number or -1   * @return {@link List} of {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public List<Dataset> getAllDatasetsByOrganizationId(      MetisUser metisUser, String organizationId, int nextPage)      throws GenericMetisException {    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage);  }  /**   * Get all datasets using the organizationName field.   *   * @param metisUser the {@link MetisUser} to authorize with   * @param organizationName the organizationName string used to find the datasets   * @param nextPage the nextPage number or -1   * @return {@link List} of {@link Dataset}   * @throws GenericMetisException which can be one of:   * <ul>   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>   * </ul>   */  public List<Dataset> getAllDatasetsByOrganizationName(      MetisUser metisUser, String organizationName, int nextPage)      throws GenericMetisException {    if (metisUser.getAccountRole() != AccountRole.METIS_ADMIN        && metisUser.getAccountRole() != AccountRole.EUROPEANA_DATA_OFFICER) {      throw new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED);    }    return datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage);  }  /**   * Checks if a dataset exists by datasetName   *   * @param datasetName the string used to find a dataset   * @return boolean value of success or not   */  public boolean existsDatasetByDatasetName(String datasetName) {    return datasetDao.existsDatasetByDatasetName(datasetName);  }  public int getDatasetsPerRequestLimit() {    return datasetDao.getDatasetsPerRequest();  }}