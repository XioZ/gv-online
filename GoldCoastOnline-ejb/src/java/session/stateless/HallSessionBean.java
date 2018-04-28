/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.CinemaEntity;
import entity.HallEntity;
import exception.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Chuck
 */
@Stateless
public class HallSessionBean implements HallSessionBeanLocal {

    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;
    @EJB
    private CinemaSessionBeanLocal cinemaSessionBean;

    // create hall
    @Override
    public HallEntity createHallInCinema(HallEntity hall, Long cinemaId)
            throws EntityNotFoundException {
        if (hall == null || hall.getSeatingPlan() == null
                || cinemaId == null) {
            return null;
        }

        CinemaEntity cinema
                = cinemaSessionBean.retrieveCinema(cinemaId);
        HallEntity newHall
                = new HallEntity(cinema.getHalls().size() + 1,
                        hall.getSeatingPlan());
        cinema.getHalls().add(newHall);
        newHall.setCinema(cinema);

        em.persist(newHall);
        em.flush();
        em.merge(cinema);

        return newHall;
    }

    // update hall
    @Override
    public HallEntity updateHall(HallEntity hall)
            throws EntityNotFoundException {
        if (hall == null || hall.getId() == null) {
            return null;
        }

        HallEntity persistedHall = this.retrieveHall(hall.getId());
        // can only update the seating plan (number is given upon creation)
        persistedHall.setSeatingPlan(hall.getSeatingPlan());

        em.merge(persistedHall);
        return persistedHall;
    }

    // delete hall
    @Override
    public Boolean deleteHall(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return false;
        }

        HallEntity hall = this.retrieveHall(id); // throw exception if id not found in db

        // check if this hall has any active screening schedule
        Query q = em.createNamedQuery("ScheduleEntity.findSchedulesByHallAfter")
                .setParameter("hall", hall.getId())
                .setParameter("startTime", (LocalDateTime.now()));
        if (q.getResultList().isEmpty()) { 
            // no screening schedules later than NOW, okay to remove hall
            hall.getCinema().getHalls().remove(hall);
            em.remove(hall);
            em.flush();
            return true;
        } else {
            // need to remove screening schedules later than NOW first
            return false;
        }
    }

    // view hall details
    @Override
    public HallEntity retrieveHall(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return null;
        }

        HallEntity hall = em.find(HallEntity.class, id);
        if (hall == null) { // id not found
            throw new EntityNotFoundException("Hall " + id + " not found!");
        } else {
            return hall;
        }
    }
    
    // list all cinema halls
    @Override
    public List<HallEntity> retrieveHallsOfCinema(Long cinemaId) 
            throws EntityNotFoundException {
        if (cinemaId == null) {
            return null;
        }
        
        CinemaEntity cinema = cinemaSessionBean.retrieveCinema(cinemaId);
        return cinema.getHalls();
    }
}
