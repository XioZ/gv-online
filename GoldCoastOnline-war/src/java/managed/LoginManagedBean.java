/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managed;

import entity.AdminStaffEntity;
import entity.OpStaffEntity;
import exception.EntityNotFoundException;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import session.stateless.StaffSessionBeanLocal;

/**
 *
 * @author Chuck
 */
@Named(value = "loginManagedBean")
@RequestScoped
public class LoginManagedBean {

    private static final Logger LOGGER
            = Logger.getLogger(LoginManagedBean.class.getName());
    private static ConsoleHandler handler = null;

    private String username;
    private String password;
    private String role = "admin";

    @EJB
    private StaffSessionBeanLocal staffSessionBean;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public LoginManagedBean() {
        handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
    }

    public void doLogin() throws IOException {
        try {
            LOGGER.log(Level.SEVERE, "inputs: " + username + password + role);
            if ("admin".equals(role)) { // admin staff
                AdminStaffEntity admin
                        = staffSessionBean.authenticateAdminStaff(
                                new AdminStaffEntity(username, password));
                if (admin == null) { // wrong pwd
                    String msg = "Login failed. Please check your username/password.";
                    displayMessage(FacesMessage.SEVERITY_ERROR, msg, "");
                } else {
                    ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                    ec.getSessionMap().put("user", admin);
                    LOGGER.log(Level.WARNING, "admin staff logged in: {0}", admin.toString());
                    ec.redirect(ec.getRequestContextPath() + "/cinemas.xhtml?faces-redirect=true");
                }
            } else { // operation staff
                OpStaffEntity op = staffSessionBean.authenticateOpStaff(
                        new OpStaffEntity(username, password));
                if (op == null) { // wrong pwd
                    String msg = "Login failed. Please check your username/password.";
                    displayMessage(FacesMessage.SEVERITY_ERROR, msg, "");
                } else {
                    ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                    ec.getSessionMap().put("user", op);
                    LOGGER.log(Level.WARNING, "op staff logged in: {0}", op.toString());
                    ec.redirect(ec.getRequestContextPath() + "/halls.xhtml?faces-redirect=true");
                }
            }
        } catch (EntityNotFoundException e) {
            // username not found in db
            String msg = "Login failed. Please make sure your username is correct.";
            displayMessage(FacesMessage.SEVERITY_ERROR, msg, "");
        }
    }

    public void doLogout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.getSessionMap().clear();
        LOGGER.log(Level.WARNING, "staff logged out: {0}");
        ec.redirect(ec.getRequestContextPath() + "/web/index.xhtml?faces-redirect=true");
    }

    public void displayMessage(FacesMessage.Severity severity, String msg, String detail) {
        if (severity == null) {
            severity = FacesMessage.SEVERITY_ERROR;
        }
        if (detail == null) {
            detail = " ";
        }
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(severity, msg, detail));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
