/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managed;

import entity.HallEntity;
import entity.MovieEntity;
import entity.ScheduleEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import org.primefaces.context.RequestContext;
import session.stateless.MovieSessionBeanLocal;
import session.stateless.ScheduleSessionBeanLocal;

/**
 *
 * @author Chuck
 */
@Named(value = "schedulesManagedBean")
@ViewScoped
public class SchedulesManagedBean implements Serializable {

    private static final Logger LOGGER
            = Logger.getLogger(SchedulesManagedBean.class.getName());
    private static ConsoleHandler handler = null;

    private HallEntity hall;
    // days that this hall has screening schedules arranged
    private List<MovieEntity> movies = new ArrayList<>();
    private List<LocalDate> days = new ArrayList<>();
    private List<List<ScheduleEntity>> dailySchedules = new ArrayList<>();
    private ScheduleEntity selectedSchedule;
    // user inputs
    private Date newScheduleDay; // day of new schedule
    private Date newScheduleStart; // start time
    private MovieEntity newScheduleMovie;

    @EJB
    private ScheduleSessionBeanLocal scheduleSessionBean;
    @EJB
    private MovieSessionBeanLocal movieSessionBean;

    /**
     * Creates a new instance of SchedulesManagedBean
     */
    public SchedulesManagedBean() {
        handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
    }

    @PostConstruct
    public void init() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        hall = (HallEntity) ec.getFlash().get("hall");
        if (hall == null) {
//            displayMessage(null, "Hall not passed in. Nothing to show.", null);
            return;
        }
        movies = movieSessionBean.retrieveAllMovies();
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("MovieConverter.movies", movies);
        retrieveDaysAndDailySchedules();
    }

    public void retrieveDaysAndDailySchedules() {
        try {
            days = scheduleSessionBean.retrieveScheduleDaysForHall(hall.getId());
            dailySchedules.clear();
            for (LocalDate day : days) {
                List<ScheduleEntity> dailySchedule
                        = scheduleSessionBean.retrieveSchedulesForHallAndDay(hall.getId(), day);
                dailySchedules.add(dailySchedule);
            }
        } catch (EntityNotFoundException ex) {
            displayMessage(null, "Hall " + hall.getNumber() + " not found.", null);
        }
    }

    public void viewSchedule(ScheduleEntity schedule) {
        selectedSchedule = schedule;
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('view-schedule-dialog').show();");
    }

    public void addSchedule() {
        LocalDate day = newScheduleDay.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDateTime start = LocalDateTime.of(
                day, 
                LocalTime.of(newScheduleStart.getHours(), newScheduleStart.getMinutes()));
        ScheduleEntity newSchedule
                = new ScheduleEntity(start, LocalDateTime.now());
        try {
            newSchedule = scheduleSessionBean.createScheduleForMovieAndHall(
                    newSchedule, newScheduleMovie.getId(), hall.getId());
            if (newSchedule != null) { // success, add to the data model
                // retrieve days and schedule again
                retrieveDaysAndDailySchedules();
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('create-schedule-dialog').hide();");
            } else {
                displayMessage(null, "Schedule creation failed. Try again.", null);
            }
        } catch (EntityConflictException e) {
            displayMessage(null, "New schedule is in conflict with existing schedule. Try a different day or time.", null);
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Movie or hall not found.", null);
        }
    }
    
    public void updateSchedule() {
        // change schedule start time's format from Date to LocalDateTime
        LocalDate day = newScheduleDay.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDateTime start = LocalDateTime.of(
                day, 
                LocalTime.of(newScheduleStart.getHours(), newScheduleStart.getMinutes()));
        ScheduleEntity updateSchedule
                = new ScheduleEntity(start, LocalDateTime.now());
        updateSchedule.setId(selectedSchedule.getId());
        updateSchedule.setHall(selectedSchedule.getHall());
        try {
            selectedSchedule = 
                    scheduleSessionBean.updateSchedule(updateSchedule);
            if (selectedSchedule != null) { // success
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('view-schedule-dialog').hide();");
            } else {
                displayMessage(null, "Update schedule failed. Try again.", null);
            }
        } catch (EntityConflictException e) {
            displayMessage(null, e.getMessage(), null);
        } catch (EntityNotFoundException e) {
             displayMessage(null, "Schedule not found.", null);
        }
    }
    
    public void deleteSchedule() {
        if (selectedSchedule == null) {
            displayMessage(null, "Error! Schedule not found.", null);
            return;
        }
        try {
            if (scheduleSessionBean.deleteSchedule(selectedSchedule.getId())) {
                retrieveDaysAndDailySchedules();
                // close the dialog
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('view-schedule-dialog').hide();");
            } else {
                displayMessage(null, "Cannot delete schedule that is not yet screened and has sold tickets.", null);
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Unable to find schedule.", null);
        }
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

    public HallEntity getHall() {
        return hall;
    }

    public void setHall(HallEntity hall) {
        this.hall = hall;
    }

    public List<LocalDate> getDays() {
        return days;
    }

    public void setDays(List<LocalDate> days) {
        this.days = days;
    }

    public List<List<ScheduleEntity>> getDailySchedules() {
        return dailySchedules;
    }

    public void setDailySchedules(List<List<ScheduleEntity>> dailySchedules) {
        this.dailySchedules = dailySchedules;
    }

    public Date getNewScheduleDay() {
        return newScheduleDay;
    }

    public void setNewScheduleDay(Date newScheduleDay) {
        this.newScheduleDay = newScheduleDay;
    }

    public Date getNewScheduleStart() {
        return newScheduleStart;
    }

    public void setNewScheduleStart(Date newScheduleStart) {
        this.newScheduleStart = newScheduleStart;
    }

    public List<MovieEntity> getMovies() {
        return movies;
    }

    public void setMovies(List<MovieEntity> movies) {
        this.movies = movies;
    }

    public MovieEntity getNewScheduleMovie() {
        return newScheduleMovie;
    }

    public void setNewScheduleMovie(MovieEntity newScheduleMovie) {
        this.newScheduleMovie = newScheduleMovie;
    }

    public ScheduleEntity getSelectedSchedule() {
        return selectedSchedule;
    }

    public void setSelectedSchedule(ScheduleEntity selectedSchedule) {
        this.selectedSchedule = selectedSchedule;
    }

}
