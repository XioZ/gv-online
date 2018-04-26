/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.ScheduleEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author Chuck
 */
@Local
public interface ScheduleSessionBeanLocal {

    public ScheduleEntity createScheduleForMovieAndHall(ScheduleEntity schedule, Long movieId, Long hallId) throws EntityNotFoundException, EntityConflictException;

    public ScheduleEntity retrieveSchedule(Long id) throws EntityNotFoundException;

    public ScheduleEntity updateSchedule(ScheduleEntity newSchedule) throws EntityNotFoundException, EntityConflictException;

    public Boolean deleteSchedule(Long id) throws EntityNotFoundException;

    public List<LocalDate> retrieveScheduleDaysForHall(Long hallId) throws EntityNotFoundException;

    public List<ScheduleEntity> retrieveSchedulesForHallAndDay(Long hallId, LocalDate day) throws EntityNotFoundException;
    
}
