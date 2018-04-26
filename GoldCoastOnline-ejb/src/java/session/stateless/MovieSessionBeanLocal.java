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
import javax.ejb.Local;

/**
 *
 * @author Chuck
 */
@Local
public interface MovieSessionBeanLocal {

    public MovieEntity createMovie(MovieEntity movie) throws EntityConflictException;

    public MovieEntity retrieveMovie(Long id) throws EntityNotFoundException;

    public MovieEntity updateMovie(MovieEntity movie) throws EntityNotFoundException, EntityConflictException;

    public Boolean deleteMovie(Long id) throws EntityNotFoundException;

    public List<MovieEntity> retrieveAllMovies();
    
}
