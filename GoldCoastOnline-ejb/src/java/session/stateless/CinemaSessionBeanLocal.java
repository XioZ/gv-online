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
import javax.ejb.Local;

/**
 *
 * @author Chuck
 */
@Local
public interface CinemaSessionBeanLocal {

    public List<CinemaEntity> retrieveAllCinemas();

    public CinemaEntity updateCinema(CinemaEntity cinema) throws EntityNotFoundException, EntityConflictException;

    public CinemaEntity createCinema(CinemaEntity cinema) throws EntityConflictException;

    public Boolean deleteCinema(Long id) throws EntityNotFoundException;

    public CinemaEntity retrieveCinema(Long id) throws EntityNotFoundException;

}
