/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.AdminStaffEntity;
import entity.OpStaffEntity;
import exception.EntityNotFoundException;
import javax.ejb.Local;

/**
 *
 * @author Chuck
 */
@Local
public interface StaffSessionBeanLocal {

    public AdminStaffEntity authenticateAdminStaff(AdminStaffEntity admin) throws EntityNotFoundException;

    public OpStaffEntity authenticateOpStaff(OpStaffEntity op) throws EntityNotFoundException;

    public boolean isOpStaffUsernameUnique(String name);

    public boolean isAdminStaffUsernameUnique(String name);
    
}
