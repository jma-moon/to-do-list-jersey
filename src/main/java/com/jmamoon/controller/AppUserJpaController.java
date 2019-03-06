/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jmamoon.controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.jmamoon.Activity;
import com.jmamoon.AppUser;
import com.jmamoon.controller.exceptions.IllegalOrphanException;
import com.jmamoon.controller.exceptions.NonexistentEntityException;
import com.jmamoon.controller.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Jose Arandia Luna https://github.com/jma-moon
 */
public class AppUserJpaController implements Serializable {

    public AppUserJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AppUser appUser) throws PreexistingEntityException, Exception {
        if (appUser.getActivityCollection() == null) {
            appUser.setActivityCollection(new ArrayList<Activity>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Activity> attachedActivityCollection = new ArrayList<Activity>();
            for (Activity activityCollectionActivityToAttach : appUser.getActivityCollection()) {
                activityCollectionActivityToAttach = em.getReference(activityCollectionActivityToAttach.getClass(), activityCollectionActivityToAttach.getId());
                attachedActivityCollection.add(activityCollectionActivityToAttach);
            }
            appUser.setActivityCollection(attachedActivityCollection);
            em.persist(appUser);
            for (Activity activityCollectionActivity : appUser.getActivityCollection()) {
                AppUser oldUserIdOfActivityCollectionActivity = activityCollectionActivity.getUserId();
                activityCollectionActivity.setUserId(appUser);
                activityCollectionActivity = em.merge(activityCollectionActivity);
                if (oldUserIdOfActivityCollectionActivity != null) {
                    oldUserIdOfActivityCollectionActivity.getActivityCollection().remove(activityCollectionActivity);
                    oldUserIdOfActivityCollectionActivity = em.merge(oldUserIdOfActivityCollectionActivity);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findAppUser(appUser.getId()) != null) {
                throw new PreexistingEntityException("AppUser " + appUser + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(AppUser appUser) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AppUser persistentAppUser = em.find(AppUser.class, appUser.getId());
            Collection<Activity> activityCollectionOld = persistentAppUser.getActivityCollection();
            Collection<Activity> activityCollectionNew = appUser.getActivityCollection();
            List<String> illegalOrphanMessages = null;
            for (Activity activityCollectionOldActivity : activityCollectionOld) {
                if (!activityCollectionNew.contains(activityCollectionOldActivity)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Activity " + activityCollectionOldActivity + " since its userId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Activity> attachedActivityCollectionNew = new ArrayList<Activity>();
            for (Activity activityCollectionNewActivityToAttach : activityCollectionNew) {
                activityCollectionNewActivityToAttach = em.getReference(activityCollectionNewActivityToAttach.getClass(), activityCollectionNewActivityToAttach.getId());
                attachedActivityCollectionNew.add(activityCollectionNewActivityToAttach);
            }
            activityCollectionNew = attachedActivityCollectionNew;
            appUser.setActivityCollection(activityCollectionNew);
            appUser = em.merge(appUser);
            for (Activity activityCollectionNewActivity : activityCollectionNew) {
                if (!activityCollectionOld.contains(activityCollectionNewActivity)) {
                    AppUser oldUserIdOfActivityCollectionNewActivity = activityCollectionNewActivity.getUserId();
                    activityCollectionNewActivity.setUserId(appUser);
                    activityCollectionNewActivity = em.merge(activityCollectionNewActivity);
                    if (oldUserIdOfActivityCollectionNewActivity != null && !oldUserIdOfActivityCollectionNewActivity.equals(appUser)) {
                        oldUserIdOfActivityCollectionNewActivity.getActivityCollection().remove(activityCollectionNewActivity);
                        oldUserIdOfActivityCollectionNewActivity = em.merge(oldUserIdOfActivityCollectionNewActivity);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = appUser.getId();
                if (findAppUser(id) == null) {
                    throw new NonexistentEntityException("The appUser with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AppUser appUser;
            try {
                appUser = em.getReference(AppUser.class, id);
                appUser.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The appUser with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Activity> activityCollectionOrphanCheck = appUser.getActivityCollection();
            for (Activity activityCollectionOrphanCheckActivity : activityCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This AppUser (" + appUser + ") cannot be destroyed since the Activity " + activityCollectionOrphanCheckActivity + " in its activityCollection field has a non-nullable userId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(appUser);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<AppUser> findAppUserEntities() {
        return findAppUserEntities(true, -1, -1);
    }

    public List<AppUser> findAppUserEntities(int maxResults, int firstResult) {
        return findAppUserEntities(false, maxResults, firstResult);
    }

    private List<AppUser> findAppUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AppUser.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public AppUser findAppUser(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AppUser.class, id);
        } finally {
            em.close();
        }
    }

    public int getAppUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AppUser> rt = cq.from(AppUser.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
