/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.singleton;

import entity.AdminStaffEntity;
import entity.OpStaffEntity;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import session.stateless.StaffSessionBeanLocal;

/**
 *
 * @author Chuck
 */
@Singleton
@LocalBean
@Startup
public class DataInitSessionBean {
    
    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;
    @EJB
    private StaffSessionBeanLocal staffSessionBean;

    // data insertion 
    @PostConstruct
    public void postConstruct() {
        // create staff
        AdminStaffEntity admin1 = new AdminStaffEntity("admin1", "anyone");
        AdminStaffEntity admin2 = new AdminStaffEntity("admin2", "anyone");
        OpStaffEntity op1 = new OpStaffEntity("op1", "anyone");
        OpStaffEntity op2 = new OpStaffEntity("op2", "anyone");
        if (staffSessionBean.isAdminStaffUsernameUnique(admin1.getUsername())) {
            em.persist(admin1);
        }
        if (staffSessionBean.isAdminStaffUsernameUnique(admin2.getUsername())) {
            em.persist(admin2);
        }
        if (staffSessionBean.isOpStaffUsernameUnique(op1.getUsername())) {
            em.persist(op1);
        }
        if (staffSessionBean.isOpStaffUsernameUnique(op2.getUsername())) {
            em.persist(op2);
        }
        em.flush();
    }
    
}
