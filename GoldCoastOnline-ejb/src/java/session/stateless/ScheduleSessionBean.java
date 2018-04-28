/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package session.stateless;

import entity.HallEntity;
import entity.MovieEntity;
import entity.ScheduleEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class ScheduleSessionBean implements ScheduleSessionBeanLocal {

    @PersistenceContext(unitName = "GoldCoastOnline-ejbPU")
    private EntityManager em;
    @EJB
    private MovieSessionBeanLocal movieSessionBean;
    @EJB
    private HallSessionBeanLocal hallSessionBean;

    // add screening schedule: 15 mins buffer time & no overlap to other schedules
    @Override
    public ScheduleEntity createScheduleForMovieAndHall(ScheduleEntity schedule, Long movieId, Long hallId)
            throws EntityNotFoundException, EntityConflictException {
        // does not require an end time (auto-calculated based on movie duration)
        if (schedule == null || schedule.getStartTime() == null
                || movieId == null || hallId == null) {
            return null;
        }

        // throws exception if movie or hall not found in db
        MovieEntity movie = movieSessionBean.retrieveMovie(movieId);
        HallEntity hall = hallSessionBean.retrieveHall(hallId);
        // ensure end time is correct according to start time and movie duration
        schedule.setEndTime(schedule.getStartTime().plusMinutes(movie.getDuration()));
        schedule.setHall(hall);
        // check new screening schedule is not in conflict w existing ones
        if (this.hasScheduleConflict(schedule)) {
            throw new EntityConflictException("Schedule conflict!");
        }
        // no schedule conflict
        ScheduleEntity newSchedule
                = new ScheduleEntity(schedule.getStartTime(), schedule.getEndTime());
        newSchedule.setMovie(movie);
        movie.getSchedules().add(newSchedule);
        newSchedule.setHall(hall);
        hall.getSchedules().add(newSchedule);
        em.persist(newSchedule);
        em.merge(movie);
        em.merge(hall);
        return newSchedule;
    }

    // IMPORTANT: schedule must have hall set
    private Boolean hasScheduleConflict(ScheduleEntity schedule) {
        if (schedule == null || schedule.getHall() == null
                || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return true;
        }
        // find conflict schedules w overlap into movie itself or buffer
        Query q = em.createQuery("SELECT s from ScheduleEntity s "
                + "WHERE s.hall.id = :hall "
                + "AND ((s.endTime > :startTime "
                + "AND s.endTime < :endTime) "
                + "OR (s.startTime < :endTime "
                + "AND s.startTime > :startTime)) ")
                .setParameter("hall", schedule.getHall().getId())
                .setParameter("startTime", schedule.getStartTime().minusMinutes(15))
                .setParameter("endTime", schedule.getEndTime().plusMinutes(15));
        return !q.getResultList().isEmpty();
    }

    // update screening newSchedule: movie & hall cannot be updated (delete n create a new one instead)
    @Override
    public ScheduleEntity updateSchedule(ScheduleEntity newSchedule)
            throws EntityNotFoundException, EntityConflictException {
        // end time not required (auto-calculated from movie duration)
        if (newSchedule == null || newSchedule.getId() == null
                || newSchedule.getStartTime() == null) {
            return null;
        }
        // throws exception if id not found in db
        ScheduleEntity oldSchedule = this.retrieveSchedule(newSchedule.getId());
        // check the schedule to update is after NOW
        if (oldSchedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new EntityConflictException("Past schedule cannot be modified!");
        }
        // check the schedule is not moved up to before NOW
        if (newSchedule.getStartTime().isBefore(LocalDateTime.now())) {
            throw new EntityConflictException("Schedule cannot be moved to the past!");
        }
        // temporarily set old schedule's time to MIN 
        // to check any conflict for the new schedule 
        LocalDateTime oldStartTime = oldSchedule.getStartTime();
        LocalDateTime oldEndTime = oldSchedule.getEndTime();
//      LocalDateTime.MIN cannot be converted into sql.Date (in custom converter)
        oldSchedule.setStartTime(LocalDateTime.of(1999, 1, 1, 0, 0));
        oldSchedule.setEndTime(LocalDateTime.of(1999, 1, 1, 0, 0));
        em.merge(oldSchedule);
        newSchedule.setEndTime(newSchedule.getStartTime().plusMinutes(oldSchedule.getMovie().getDuration()));
//        newSchedule.setHall(hall);
        if (this.hasScheduleConflict(newSchedule)) {
            // new schedule conflicts w existing other schedules
            // reverse old schedule's time and abort
            oldSchedule.setStartTime(oldStartTime);
            oldSchedule.setEndTime(oldEndTime);
            throw new EntityConflictException("Schedule conflict!");
        } else {
            // no conflict, okay to update schedule
            oldSchedule.setStartTime(newSchedule.getStartTime());
            oldSchedule.setEndTime(newSchedule.getEndTime());
            em.merge(oldSchedule);
            return oldSchedule;
        }
    }

    @Override
    public ScheduleEntity retrieveSchedule(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return null;
        }

        ScheduleEntity schedule = em.find(ScheduleEntity.class, id);
        if (schedule == null) { // id not found
            throw new EntityNotFoundException("Schedule " + id + " not found!");
        } else {
            return schedule;
        }
    }

    // view all screening schedules for a hall (1 - 2)
    // 1. retrieve calendar days that have screening schedules
    @Override
    public List<LocalDate> retrieveScheduleDaysForHall(Long hallId)
            throws EntityNotFoundException {
        if (hallId == null) {
            return null;
        }
        // throws exception if hall not found
        HallEntity hall = hallSessionBean.retrieveHall(hallId);
//         return hall.getSchedules();

//        Query q = em.createQuery("SELECT UNIQUE s.day "
//                + "FROM ScheduleEntity s "
//                + "WHERE s.hall.id = :hall ")
//                .setParameter("hall", hallId);
//        return q.getResultList();
        List<LocalDate> days = new ArrayList<>();
        for (ScheduleEntity schedule : hall.getSchedules()) {
            if (!days.contains(schedule.getDay())) {
                days.add(schedule.getDay());
            }
        }
        return days;
    }
    // 2. view daily screening schedule for a hall by a calendar day
    @Override
    public List<ScheduleEntity> retrieveSchedulesForHallAndDay(Long hallId, LocalDate day) 
        throws EntityNotFoundException {
        if (hallId == null || day == null) return null;
        
        HallEntity hall = hallSessionBean.retrieveHall(hallId);

        Query q = em.createQuery("SELECT s "
                + "FROM ScheduleEntity s "
                + "WHERE s.hall.id = :hall "
                + "AND s.day = :day "
                + "ORDER BY s.startTime ASC ")
                .setParameter("hall", hall.getId())
                .setParameter("day", day);
        return q.getResultList();
    }

    // delete schedule
    @Override
    public Boolean deleteSchedule(Long id)
            throws EntityNotFoundException {
        if (id == null) {
            return null;
        }

        ScheduleEntity schedule = this.retrieveSchedule(id);
        // start time before NOW: already screened, okay to delete
        // start time after NOW: no associated tickets sold, okay to delete
        if (schedule.getStartTime().isBefore(LocalDateTime.now())
                || schedule.getTickets().isEmpty()) {
            schedule.getMovie().getSchedules().remove(schedule);
            schedule.getHall().getSchedules().remove(schedule);
            em.remove(schedule);
            em.flush();
            return true;
        } else {
            // start time after NOW w tickets sold, cannot be deleted till screened
            return false;
        }
    }
}
