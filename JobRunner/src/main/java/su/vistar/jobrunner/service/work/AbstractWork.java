package ru.alidi.horeca.jobrunner.service.work;

import java.util.Date;

/**
 * Abstract work task
 * @author Aleksandr Gorovoi<alexander.gorovoy@vistar.su>
 */
public abstract class AbstractWork implements Runnable{
    
    protected int attemptCount;
    
    protected Throwable lastError;
    
    protected Date lastAttempt;

    protected boolean skipped;
    
    public int getAttemptCount() {
        return attemptCount;
    }
    
    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }
    
    public Throwable getLastError() {
        return lastError;
    }
    
    public Date getLastAttempt() {
        return lastAttempt;
    }
    
    public void setLastAttempt(Date date) {
        this.lastAttempt = date;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
    
    @Override
    public abstract void run();
    
    public void onError(Throwable error) {
        lastError = error;
        this.errorProcessing(error);
    }
    
    public abstract void errorProcessing(Throwable error);
    
    public abstract void onSuccess();

    public abstract void onSkip();
    
}
