package ru.alidi.horeca.jobrunner.service.dataimport;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alidi.horeca.persistence.external.dao.*;
import ru.alidi.horeca.persistence.external.entity.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by shevchenko.ru on 01.11.16.
 */
@Service
public class ExternalDatabaseUpdaterService {

    @Autowired
    private ExternalCategoryDao externalCategoryDao;

    @Autowired
    private ExternalItemDao externalItemDao;

    @Autowired
    private ExternalCustomerDao externalCustomerDao;

    @Autowired
    private ExternalPayTermsDao externalPayTermsDao;

    @Autowired
    private ExternalCustomerPayerDao externalCustomerPayerDao;

    @Autowired
    private ExternalDeliveryPointDao externalDeliveryPointDao;

    @Autowired
    private ExternalCityDao externalCityDao;

    @Autowired
    private ExternalBranchDao externalBranchDao;

    @Autowired
    private ExternalWarehouseDao externalWarehouseDao;

    @Autowired
    private ExternalRestDao externalRestDao;

    @Autowired
    private ExternalOrderHeaderDao externalOrderHeaderDao;

    @Transactional("transactionManagerExt")
    public List<ExternalCategoryEntity> fetchCategories(String uuid, int limit){
        return externalCategoryDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCategoryEntity> fetchFirstCategories(){
        return externalCategoryDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalItemEntity> fetchItems(String uuid, int limit){
        return externalItemDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalItemEntity> fetchFirstItems(int limit){
        return externalItemDao.firstResults(limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCustomerEntity> fetchCustomers(String uuid, int limit){
        return externalCustomerDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCustomerEntity> fetchFirstCustomers() {
        return externalCustomerDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCustomerPayerEntity> fetchCustomerPayers(String uuid, int limit){
        return externalCustomerPayerDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCustomerPayerEntity> fetchFirstCustomerPayers() {
        return externalCustomerPayerDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalDeliveryPointEntity> fetchDeliveryPoints(String uuid, int limit){
        return externalDeliveryPointDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalDeliveryPointEntity> fetchFirstDeliveryPoints() {
        return externalDeliveryPointDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalPayTermEntity> fetchPayTerms(String uuid, int limit){
        return externalPayTermsDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalPayTermEntity> fetchFirstPayTerms(){
        return externalPayTermsDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCityEntity> fetchCities(String uuid, int limit){
        return externalCityDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalCityEntity> fetchFirstCities(){
        return externalCityDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalBranchEntity> fetchBranches(String uuid, int limit){
        return externalBranchDao.fetchStartFrom(uuid, limit);
    }

    @Transactional("transactionManagerExt")
    public List<ExternalBranchEntity> fetchFirstBranches(){
        return externalBranchDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalWarehouseEntity> fetchWarehouses(String uuid, int limit){
        List<ExternalWarehouseEntity> externalWarehouseEntities = externalWarehouseDao.fetchStartFrom(uuid, limit);

        return externalWarehouseEntities;
    }

    @Transactional("transactionManagerExt")
    public List<ExternalWarehouseEntity> fetchFirstWarehouses(){
        return externalWarehouseDao.firstResults();
    }

    @Transactional("transactionManagerExt")
    public List<ExternalRestEntity> fetchRests(int offset, int limit){
        List<ExternalRestEntity> externalWarehouseEntities = externalRestDao.getAll(limit, offset);

        return externalWarehouseEntities;
    }

    @Transactional("transactionManagerExt")
    public List<ExternalOrderHeaderEntity> fetchOrdersInitialized(Long id, int limit){
        List<ExternalOrderHeaderEntity> externalOrderHeaderEntities = externalOrderHeaderDao.fetchStartFrom(id, limit);
        externalOrderHeaderEntities.forEach(e -> Hibernate.initialize(e.getLines()));
        return externalOrderHeaderEntities;
    }

    @Transactional("transactionManagerExt")
    public List<ExternalOrderHeaderEntity> fetchFirstOrders(){
        List<ExternalOrderHeaderEntity> externalOrderHeaderEntities = externalOrderHeaderDao.firstResults();
        externalOrderHeaderEntities.forEach(e -> Hibernate.initialize(e.getLines()));
        return externalOrderHeaderEntities;
    }
}
