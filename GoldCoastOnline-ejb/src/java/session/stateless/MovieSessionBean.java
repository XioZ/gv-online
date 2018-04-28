/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.MovieEntity;
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
public class MovieSessionBean implements MovieSessionBeanLocal {

    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;

    // create movie 
    // TODO: upload pictures? handled in managed bean
    // reference: https://github.com/HanFW/wisdom-new/blob/master/Wisdom-war/src/java/managedBean/AuthorAddNewArticleManagedBean.java
    @Override
    public MovieEntity createMovie(MovieEntity movie)
            throws EntityConflictException {
        if (movie == null
                || movie.getName() == null || movie.getName().isEmpty()
                || movie.getRating() == null || movie.getRating().isEmpty()
                || movie.getDuration() == 0 || movie.getImages() == null || movie.getImages().size() < 1) {
            return null;
        }

        // check if name already exists
        if (this.isMovieNameUnique(movie.getName())) { // name satisfies unique constraint
            MovieEntity newMovie
                    = new MovieEntity(movie.getName(), movie.getRating(), movie.getDuration());
            newMovie.setImages(movie.getImages());
            em.persist(newMovie);
            return newMovie;
        }
        // else, name already exist, failed unique constraint
        throw new EntityConflictException("Movie named " + movie.getName() + " already exists!");
    }

    private boolean isMovieNameUnique(String name) {
        Query q = em.createNamedQuery("MovieEntity.findMovieByName")
                .setParameter("name", name);
        return q.getResultList().isEmpty();
    }

    // update movie
    // TODO: change of movie images?
    @Override
    public MovieEntity updateMovie(MovieEntity movie)
            throws EntityNotFoundException {
        if (movie == null
                || movie.getRating() == null || movie.getRating().isEmpty()
                || movie.getDuration() == 0) {
            return null;
        }

        // check if cinema w such ID exists
        MovieEntity persistedMovie = this.retrieveMovie(movie.getId());
        persistedMovie.setRating(movie.getRating());
        persistedMovie.setDuration(movie.getDuration());

        em.merge(persistedMovie);
        return persistedMovie;
    }

    // view all movies
    @Override
    public List<MovieEntity> retrieveAllMovies() {
        Query q = em.createQuery("SELECT m FROM MovieEntity m ");
        return q.getResultList();
    }

    // view movie details
    @Override
    public MovieEntity retrieveMovie(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return null;
        }

        MovieEntity movie = em.find(MovieEntity.class, id);
        if (movie == null) { // id not found
            throw new EntityNotFoundException("Movie " + id + " not found!");
        } else {
            return movie;
        }
    }

    // delete movie
    @Override
    public Boolean deleteMovie(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return false;
        }

        MovieEntity movie = this.retrieveMovie(id); // throws exception if id not found in db

        // check if the movie has any screening schedules
        if (movie.getSchedules().isEmpty()) { // no schedules added, okay to delete
            // only movies w no screening schedules (before or after NOW) can be deleted
            em.remove(movie);
            em.flush();
            return true;
        } else {
            return false; // must remove all schedules first
        }
    }
}
