/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managed;

import entity.CinemaEntity;
import entity.HallEntity;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import session.stateless.CinemaSessionBeanLocal;

/**
 *
 * @author Chuck
 */
@Named(value = "hallsManagedBean")
@ViewScoped
public class HallsManagedBean implements Serializable {

    private List<CinemaEntity> cinemas = new ArrayList<>();
    
    @EJB
    private CinemaSessionBeanLocal cinemaSessionBean;
    
    /**
     * Creates a new instance of HallsManagedBean
     */
    public HallsManagedBean() {
    }
    
    @PostConstruct
    public void init() {
        cinemas = cinemaSessionBean.retrieveAllCinemas();
    }
    
    public void viewSchedules(HallEntity hall) throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.getFlash().put("hall", hall);
        ec.redirect(ec.getRequestContextPath() + "/schedules.xhtml?faces-redirect=true");
    }

    public List<CinemaEntity> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<CinemaEntity> cinemas) {
        this.cinemas = cinemas;
    }
    
}
