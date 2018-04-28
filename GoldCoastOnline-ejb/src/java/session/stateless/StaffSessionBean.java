/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import entity.AdminStaffEntity;
import entity.OpStaffEntity;
import exception.EntityNotFoundException;
import java.util.logging.Level;
import javax.ejb.NoSuchEntityException;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Chuck
 */
@Stateless
public class StaffSessionBean implements StaffSessionBeanLocal {

    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;

    @Override
    public AdminStaffEntity authenticateAdminStaff(AdminStaffEntity admin)
            throws EntityNotFoundException {
        if (admin == null || admin.getUsername() == null || admin.getPassword() == null) {
            return null;
        }

        Query q = em.createQuery("SELECT a from AdminStaffEntity a "
                + "WHERE a.username = :username")
                .setParameter("username", admin.getUsername());
        AdminStaffEntity staff = null;
        try {
            staff = (AdminStaffEntity) q.getSingleResult();
            if (admin.getPassword().equals(staff.getPassword())) { // pwd match
                return staff; // login success
            } else {
                return null; // wrong password
            }
        } catch (NoResultException e) { // email not found
            throw new EntityNotFoundException("Administrative staff " + admin.getUsername() + " not found!");
        }
    }
    
    @Override
    public OpStaffEntity authenticateOpStaff(OpStaffEntity op)
            throws EntityNotFoundException {
        if (op == null || op.getUsername() == null || op.getPassword() == null) {
            return null;
        }

        Query q = em.createQuery("SELECT o from OpStaffEntity o "
                + "WHERE o.username = :username")
                .setParameter("username", op.getUsername());
        OpStaffEntity staff = null;
        try {
            staff = (OpStaffEntity) q.getSingleResult();
            if (op.getPassword().equals(staff.getPassword())) { // pwd match
                return staff; // login success
            } else {
                return null; // wrong password
            }
        } catch (NoResultException e) { // email not found
            throw new EntityNotFoundException("Operation staff " + op.getUsername() + " not found!");
        }
    }
    
    @Override
    public boolean isAdminStaffUsernameUnique(String name) {
        if (name == null || name.isEmpty()) return false;
        
        Query q = em.createQuery("SELECT a FROM AdminStaffEntity a "
                + "WHERE a.username = :username ")
                .setParameter("username", name);
        return q.getResultList().isEmpty();
    }
    
    @Override
    public boolean isOpStaffUsernameUnique(String name) {
        if (name == null || name.isEmpty()) return false;
        
        Query q = em.createQuery("SELECT o FROM OpStaffEntity o "
                + "WHERE o.username = :username ")
                .setParameter("username", name);
        return q.getResultList().isEmpty();
    }

}
