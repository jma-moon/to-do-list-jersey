/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jmamoon;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jose Arandia Luna https://github.com/jma-moon
 */
@Entity
@Table(name = "Activity")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Activity.findAll", query = "SELECT a FROM Activity a"),
    @NamedQuery(name = "Activity.findById", query = "SELECT a FROM Activity a WHERE a.id = :id"),
    @NamedQuery(name = "Activity.findByAccomplishedOn", query = "SELECT a FROM Activity a WHERE a.accomplishedOn = :accomplishedOn"),
    @NamedQuery(name = "Activity.findByCreatedOn", query = "SELECT a FROM Activity a WHERE a.createdOn = :createdOn"),
    @NamedQuery(name = "Activity.findByModifiedOn", query = "SELECT a FROM Activity a WHERE a.modifiedOn = :modifiedOn"),
    @NamedQuery(name = "Activity.findByDeletedOn", query = "SELECT a FROM Activity a WHERE a.deletedOn = :deletedOn")})
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "Id")
    private Integer id;
    @Lob
    @Size(max = 2147483647)
    @Column(name = "Summary")
    private String summary;
    @Column(name = "AccomplishedOn")
    @Temporal(TemporalType.DATE)
    private Date accomplishedOn;
    @Column(name = "CreatedOn")
    @Temporal(TemporalType.DATE)
    private Date createdOn;
    @Column(name = "ModifiedOn")
    @Temporal(TemporalType.DATE)
    private Date modifiedOn;
    @Column(name = "DeletedOn")
    @Temporal(TemporalType.DATE)
    private Date deletedOn;
    @JoinColumn(name = "UserId", referencedColumnName = "Id")
    @ManyToOne(optional = false)
    private AppUser userId;

    public Activity() {
    }

    public Activity(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getAccomplishedOn() {
        return accomplishedOn;
    }

    public void setAccomplishedOn(Date accomplishedOn) {
        this.accomplishedOn = accomplishedOn;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Date getDeletedOn() {
        return deletedOn;
    }

    public void setDeletedOn(Date deletedOn) {
        this.deletedOn = deletedOn;
    }

    public AppUser getUserId() {
        return userId;
    }

    public void setUserId(AppUser userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Activity)) {
            return false;
        }
        Activity other = (Activity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.jmamoon.Activity[ id=" + id + " ]";
    }
    
}
