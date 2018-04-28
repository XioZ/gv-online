/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managed;

import entity.CinemaEntity;
import entity.HallEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import session.stateless.CinemaSessionBeanLocal;
import session.stateless.HallSessionBeanLocal;
import util.Constants;

/**
 *
 * @author Chuck
 */
@Named(value = "cinemasManagedBean")
@SessionScoped
public class CinemasManagedBean implements Serializable {

    private static final Logger LOGGER
            = Logger.getLogger(CinemasManagedBean.class.getName());
    private static ConsoleHandler handler = null;

    private List<CinemaEntity> cinemas = new ArrayList<>();
    private CinemaEntity selectedCinema;
    private HallEntity selectedHall;

    // user inputs:
    // create cinema
    private String cinemaName;
    // create hall
    private int numOfRows = 1;
    private int numOfCols = 1;
    private List<String> handicapSeats = new ArrayList<>(); // e.g. '0_0' corresponds to A1
    private int handicapRow = 1;
    private int handicapCol = 1;
    private List<String> emptySeats = new ArrayList<>(); // e.g. '0_0' corresponds to A1
    private int emptyRow = 1;
    private int emptyCol = 1;

    @EJB
    private CinemaSessionBeanLocal cinemaSessionBean;
    @EJB
    private HallSessionBeanLocal hallSessionBean;

    /**
     * Creates a new instance of CinemasManagedBean
     */
    public CinemasManagedBean() {
        handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
    }

    @PostConstruct
    public void init() {
        cinemas = cinemaSessionBean.retrieveAllCinemas();
        if (cinemas != null && !cinemas.isEmpty()) {
            selectedCinema = cinemas.get(0);
        }
    }

    public void onCinemaClicked(int index) throws IOException {
        selectedCinema = cinemas.get(index);
    }

    public void createCinema() throws IOException {
        CinemaEntity newCinema = new CinemaEntity(cinemaName);
        try {
            newCinema = cinemaSessionBean.createCinema(newCinema);
            if (newCinema != null) { // success, add to the data model
                cinemas.add(newCinema);
                selectedCinema = newCinema;
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('create-movie-dialog').hide();");
            } else {
                LOGGER.log(Level.SEVERE, "Cinema creation failed!");
            }
        } catch (EntityConflictException e) {
            displayMessage(null, "Please enter a unique cinema name.", null);
        }
    }

    public void updateCinema() throws IOException {
        try {
            CinemaEntity updatedCinema = cinemaSessionBean.updateCinema(selectedCinema);
            if (updatedCinema != null) { // success
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('create-movie-dialog').hide();");
            } else {
                LOGGER.log(Level.SEVERE, "Cinema creation failed!");
            }
        } catch (EntityConflictException e) {
            displayMessage(null, "Please enter a unique cinema name.", null);
        } catch (EntityNotFoundException e) {
             displayMessage(null, "Cinema not found.", null);
        }
    }

    public void deleteCinema() throws IOException {
        try {
            if (cinemaSessionBean.deleteCinema(selectedCinema.getId())) {
                // delete success, update cinemas list
                cinemas.remove(selectedCinema);
                if (cinemas.isEmpty()) {
                    // no cinemas left
                    selectedCinema = null;
                } else {
                    selectedCinema = cinemas.get(0);
                }
            } else {
                displayMessage(null, "Cinema deletion failed. Please delete its halls first.", null);
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Deletion failed. Cinema was not found.", null);
        }
    }

    public void addSpecialSeat(char type) {
        switch (type) {
            case 'H':
                if (handicapRow >= 1 && handicapRow <= numOfRows
                        && handicapCol >= 1 && handicapCol <= numOfCols) {
                    String seat = handicapRow + "_" + handicapCol;
                    if (!handicapSeats.contains(seat)) {
                        handicapSeats.add(seat); // e.g. 0_0 for A1
                    } // do nothing if seat already added
                } else {
                    displayMessage(null, "Please enter a valid row and column.", null);
                }
                break;
            case 'E':
                if (emptyRow >= 1 && emptyRow <= numOfRows
                        && emptyCol >= 1 && emptyCol <= numOfCols) {
                    String seat = emptyRow + "_" + emptyCol;
                    if (!emptySeats.contains(seat)) {
                        emptySeats.add(seat); // e.g. 0_0 for A1
                    } // do nothing if seat already added
                } else {
                    displayMessage(null, "Please enter a valid row and column.", null);
                }
                break;
        }
    }

    public void clearSpecialSeat(char type) {
        switch (type) {
            case 'H':
                handicapSeats.clear();
                break;
            case 'E':
                emptySeats.clear();
                break;
        }
    }

    public void createHall() {
        if (numOfRows < 1 || numOfCols < 1) {
            displayMessage(null, "Number of rows and columns must exceed 1.", null);
        }
        HallEntity newHall = new HallEntity();
        String[][] seatingPlan = new String[numOfRows][numOfCols];
        for (int row = 1; row <= seatingPlan.length; row++) {
            for (int col = 1; col <= seatingPlan[row-1].length; col++) {
                String seat = row + "_" + col;
                if (handicapSeats.contains(seat)) {
                    seatingPlan[row-1][col-1] = Constants.HANDICAP;
                } else if (emptySeats.contains(seat)) {
                    seatingPlan[row-1][col-1] = Constants.EMPTY;
                } else {
                    seatingPlan[row-1][col-1] = Constants.NORMAL;
                }
            }
        } // end for
        newHall.setSeatingPlan(seatingPlan);
        try {
            newHall = hallSessionBean.createHallInCinema(newHall, selectedCinema.getId());
            if (newHall != null) { // success
                // update the list of cinemas and cinema to show
                int index = cinemas.indexOf(selectedCinema);
                cinemas = cinemaSessionBean.retrieveAllCinemas();
                selectedCinema = cinemas.get(index);
                // close the dialog
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('create-hall-dialog').hide();");
            } else {
                displayMessage(null, "Hall creation failed. See console log for details.", null);
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Hall creation failed. Unable to find cinema.", null);
        }
    }

    public void deleteHall() {
        if (selectedHall == null) {
            displayMessage(null, "Error! Hall not found.", null);
            return;
        }
        try {
            if (hallSessionBean.deleteHall(selectedHall.getId())) {
                // update the list of cinemas and cinema to show
                int index = cinemas.indexOf(selectedCinema);
                cinemas = cinemaSessionBean.retrieveAllCinemas();
                selectedCinema = cinemas.get(index);
                // close the dialog
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('view-hall-dialog').hide();");
            } else {
                displayMessage(null, "Cannot delete hall. There're active screening schedules. (Delete these schedules first.)", null);
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Unable to find hall.", null);
        }
    }

    public void openViewHallDialog(int index) {
        selectedHall = selectedCinema.getHalls().get(index);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('view-hall-dialog').show();");
        LOGGER.log(Level.SEVERE, "open view hall dialog: {0}", selectedHall.toString());
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

    public List<CinemaEntity> getCinemas() {
        return cinemas;
    }

    public void setCinemas(List<CinemaEntity> cinemas) {
        this.cinemas = cinemas;
    }

    public CinemaEntity getSelectedCinema() {
        return selectedCinema;
    }

    public void setSelectedCinema(CinemaEntity selectedCinema) {
        this.selectedCinema = selectedCinema;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    public int getNumOfCols() {
        return numOfCols;
    }

    public void setNumOfCols(int numOfCols) {
        this.numOfCols = numOfCols;
    }

    public List<String> getHandicapSeats() {
        return handicapSeats;
    }

    public void setHandicapSeats(List<String> handicapSeats) {
        this.handicapSeats = handicapSeats;
    }

    public int getHandicapRow() {
        return handicapRow;
    }

    public void setHandicapRow(int handicapRow) {
        this.handicapRow = handicapRow;
    }

    public int getHandicapCol() {
        return handicapCol;
    }

    public void setHandicapCol(int handicapCol) {
        this.handicapCol = handicapCol;
    }

    public List<String> getEmptySeats() {
        return emptySeats;
    }

    public void setEmptySeats(List<String> emptySeats) {
        this.emptySeats = emptySeats;
    }

    public int getEmptyRow() {
        return emptyRow;
    }

    public void setEmptyRow(int emptyRow) {
        this.emptyRow = emptyRow;
    }

    public int getEmptyCol() {
        return emptyCol;
    }

    public void setEmptyCol(int emptyCol) {
        this.emptyCol = emptyCol;
    }

    public HallEntity getSelectedHall() {
        return selectedHall;
    }

    public void setSelectedHall(HallEntity selectedHall) {
        this.selectedHall = selectedHall;
    }

}
