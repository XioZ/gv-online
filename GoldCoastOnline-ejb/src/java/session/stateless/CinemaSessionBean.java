/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.CinemaEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author Chuck
 */
@Stateless
public class CinemaSessionBean implements CinemaSessionBeanLocal {

    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;

    @Override
    public CinemaEntity createCinema(CinemaEntity cinema) throws EntityConflictException {
        if (cinema == null || cinema.getName() == null || cinema.getName().isEmpty()) {
            return null;
        }

        // check if name already exists
        if (this.isCinemaNameUnique(cinema.getName())) { // name satisfies unique constraint
            CinemaEntity newCinema = new CinemaEntity(cinema.getName());
            em.persist(newCinema);
            return newCinema;
        }
        // else, name already exist, failed unique constraint
        throw new EntityConflictException("Cinema named " + cinema.getName() + " already exists!");
    }

    @Override
    public List<CinemaEntity> retrieveAllCinemas() {
        Query q = em.createQuery("SELECT c FROM CinemaEntity c ");
        return q.getResultList();
    }

    @Override
    public CinemaEntity retrieveCinema(Long id) throws EntityNotFoundException {
        if (id == null) {
            return null;
        }

        CinemaEntity cinema = em.find(CinemaEntity.class, id);
        if (cinema == null) { // id not found
            throw new EntityNotFoundException("Cinema " + id + " not found!");
        } else {
            return cinema;
        }
    }

    @Override
    public CinemaEntity updateCinema(CinemaEntity cinema)
            throws EntityNotFoundException, EntityConflictException {
        if (cinema == null || cinema.getId() == null) {
            return null;
        }

        // check if cinema w such ID exists
        CinemaEntity persistedCinema = this.retrieveCinema(cinema.getId());
        if (this.isCinemaNameUnique(cinema.getName())) { // name unique, okay to update
            persistedCinema.setName(cinema.getName());
            em.merge(persistedCinema);
            return persistedCinema;
        } else {
            throw new EntityConflictException("Cinema named " + cinema.getName() + " already exists!");
        }
    }

    private boolean isCinemaNameUnique(String name) {
        Query q = em.createNamedQuery("CinemaEntity.findCinemaByName")
                .setParameter("name", name);
        return q.getResultList().isEmpty();
    }

    @Override
    public Boolean deleteCinema(Long id) 
            throws EntityNotFoundException {
        if (id == null) return false;

        CinemaEntity cinema = this.retrieveCinema(id);
        // check if the cinema has any halls
        if (cinema.getHalls().isEmpty()) { // no halls, okay to delete
            em.remove(cinema);
            em.flush();
            return true;
        } else {
            return false; // must delete all halls first
        }
    }

}
