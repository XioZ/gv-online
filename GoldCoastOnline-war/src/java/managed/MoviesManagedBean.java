/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managed;

import entity.MovieEntity;
import exception.EntityConflictException;
import exception.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import session.stateless.MovieSessionBeanLocal;

/**
 *
 * @author Chuck
 */
@Named(value = "moviesManagedBean")
@ViewScoped
public class MoviesManagedBean implements Serializable {

    private static final Logger LOGGER
            = Logger.getLogger(MoviesManagedBean.class.getName());
    private static ConsoleHandler handler = null;

    private List<MovieEntity> movies = new ArrayList<>();
    private MovieEntity selectedMovie;
    // user inputs
    private UploadedFile movieImage;
    private MovieEntity newMovie;

    @EJB
    private MovieSessionBeanLocal movieSessionBean;

    /**
     * Creates a new instance of MoviesManagedBean
     */
    public MoviesManagedBean() {
        handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(handler);
    }

    @PostConstruct
    public void init() {
        movies = movieSessionBean.retrieveAllMovies();
        newMovie = new MovieEntity();
    }

    public void uploadMovieImage(FileUploadEvent event) throws IOException {
        movieImage = event.getFile();
        String imageName = UUID.randomUUID().toString() + ".png";

        if (movieImage != null) {
            String newFilePath = System.getProperty("user.dir").replace("config", "docroot") + System.getProperty("file.separator");
            OutputStream output = new FileOutputStream(new File(newFilePath, imageName));

            int a;
            int BUFFER_SIZE = 8192;
            byte[] buffer = new byte[BUFFER_SIZE];
            InputStream inputStream = movieImage.getInputstream();
            while (true) {
                a = inputStream.read(buffer);
                if (a < 0) {
                    break;
                }
                output.write(buffer, 0, a);
                output.flush();
            }
            output.close();
            inputStream.close();
            newMovie.getImages().add(imageName);
            displayMessage(FacesMessage.SEVERITY_INFO, "Image uploaded. Repeat to upload more images.", null);
        } else {
            displayMessage(null, "Please upload movie images", null);
        }
    }

    public void addMovie() {
        if (newMovie.getDuration() < 30) {
            displayMessage(null, "Duration should exceed 30mins.", null);
        } else if (newMovie.getImages().size() < 1) {
            displayMessage(null, "Must upload some images.", null);
        } else {
            try {
                newMovie = movieSessionBean.createMovie(newMovie);
                movies.add(newMovie);
                newMovie = new MovieEntity();
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('create-movie-dialog').hide();");
            } catch (EntityConflictException e) {
                displayMessage(null, "Movie name already exists. Choose another one.", null);
            }
        }
    }

    public void openViewMovieDialog(int index) {
        selectedMovie = movies.get(index);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('view-movie-dialog').show();");
//        LOGGER.log(Level.SEVERE, " open view movie dialog: {0}", selectedMovie.toString());
    }

    public void updateMovie() throws IOException {
        try {
            MovieEntity updatedMovie
                    = movieSessionBean.updateMovie(selectedMovie);
            if (selectedMovie != null) { // success
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('view-movie-dialog').hide();");
                displayMessage(FacesMessage.SEVERITY_INFO, "Changes saved.", null);
            } else {
                LOGGER.log(Level.SEVERE, "Movie update failed!");
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Movie not found.", null);
        }
    }
    
    public void deleteMovie() throws IOException {
        try {
            if (movieSessionBean.deleteMovie(selectedMovie.getId())) {
                // delete success, update cinemas list
                movies.remove(selectedMovie);
                selectedMovie = null;
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('view-movie-dialog').hide();");
            } else {
                displayMessage(null, "Movie with schedules cannot be deleted. Please delete its schedules first.", null);
            }
        } catch (EntityNotFoundException e) {
            displayMessage(null, "Deletion failed. Movie was not found.", null);
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

    public List<MovieEntity> getMovies() {
        return movies;
    }

    public void setMovies(List<MovieEntity> movies) {
        this.movies = movies;
    }

    public UploadedFile getMovieImage() {
        return movieImage;
    }

    public void setMovieImage(UploadedFile movieImage) {
        this.movieImage = movieImage;
    }

    public MovieEntity getNewMovie() {
        return newMovie;
    }

    public void setNewMovie(MovieEntity newMovie) {
        this.newMovie = newMovie;
    }

    public MovieEntity getSelectedMovie() {
        return selectedMovie;
    }

    public void setSelectedMovie(MovieEntity selectedMovie) {
        this.selectedMovie = selectedMovie;
    }

}
