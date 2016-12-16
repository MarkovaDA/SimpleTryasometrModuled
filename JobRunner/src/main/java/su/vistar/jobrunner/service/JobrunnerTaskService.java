package ru.alidi.horeca.jobrunner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alidi.horeca.persistence.dao.JobrunnerTaskDao;
import ru.alidi.horeca.persistence.entity.JobrunnerTaskEntity;

import java.util.List;

/**
 * Created by shevchenko.ru on 15.11.16.
 */
@Service
public class JobrunnerTaskService {

    private JobrunnerTaskDao jobrunnerTaskDao;

    @Autowired
    public JobrunnerTaskService(JobrunnerTaskDao jobrunnerTaskDao) {
        this.jobrunnerTaskDao = jobrunnerTaskDao;
    }

    @Transactional("transactionManager")
    public List<JobrunnerTaskEntity> getAllTasks(){
         return jobrunnerTaskDao.getAll();
    }

    @Transactional("transactionManager")
    public JobrunnerTaskEntity get(String id){
         return jobrunnerTaskDao.get(id);
    }

    @Transactional("transactionManager")
    public void save(JobrunnerTaskEntity task){
         jobrunnerTaskDao.save(task);
    }

}
