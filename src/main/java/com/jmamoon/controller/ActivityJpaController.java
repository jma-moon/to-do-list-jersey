/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jmamoon.controller;

import com.jmamoon.Activity;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.jmamoon.AppUser;
import com.jmamoon.controller.exceptions.NonexistentEntityException;
import com.jmamoon.controller.exceptions.PreexistingEntityException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author Jose Arandia Luna https://github.com/jma-moon
 */
public class ActivityJpaController implements Serializable {

    public ActivityJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Activity activity) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            AppUser userId = activity.getUserId();
            if (userId != null) {
                userId = em.getReference(userId.getClass(), userId.getId());
                activity.setUserId(userId);
            }
            em.persist(activity);
            if (userId != null) {
                userId.getActivityCollection().add(activity);
                userId = em.merge(userId);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findActivity(activity.getId()) != null) {
                throw new PreexistingEntityException("Activity " + activity + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Activity activity) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Activity persistentActivity = em.find(Activity.class, activity.getId());
            AppUser userIdOld = persistentActivity.getUserId();
            AppUser userIdNew = activity.getUserId();
            if (userIdNew != null) {
                userIdNew = em.getReference(userIdNew.getClass(), userIdNew.getId());
                activity.setUserId(userIdNew);
            }
            activity = em.merge(activity);
            if (userIdOld != null && !userIdOld.equals(userIdNew)) {
                userIdOld.getActivityCollection().remove(activity);
                userIdOld = em.merge(userIdOld);
            }
            if (userIdNew != null && !userIdNew.equals(userIdOld)) {
                userIdNew.getActivityCollection().add(activity);
                userIdNew = em.merge(userIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = activity.getId();
                if (findActivity(id) == null) {
                    throw new NonexistentEntityException("The activity with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Activity activity;
            try {
                activity = em.getReference(Activity.class, id);
                activity.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The activity with id " + id + " no longer exists.", enfe);
            }
            AppUser userId = activity.getUserId();
            if (userId != null) {
                userId.getActivityCollection().remove(activity);
                userId = em.merge(userId);
            }
            em.remove(activity);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Activity> findActivityEntities() {
        return findActivityEntities(true, -1, -1);
    }

    public List<Activity> findActivityEntities(int maxResults, int firstResult) {
        return findActivityEntities(false, maxResults, firstResult);
    }

    private List<Activity> findActivityEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Activity.class));
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

    public Activity findActivity(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Activity.class, id);
        } finally {
            em.close();
        }
    }

    public int getActivityCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Activity> rt = cq.from(Activity.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
