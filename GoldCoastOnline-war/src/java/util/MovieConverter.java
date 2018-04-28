package util;

import entity.MovieEntity;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "movieConverter", forClass = MovieEntity.class)
public class MovieConverter implements Converter {

    public MovieConverter() {
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().length() == 0 || value.equals("null")) {
            return null;
        }

        try {
            List<MovieEntity> movies = (List<MovieEntity>) context.getExternalContext().getSessionMap().get("MovieConverter.movies");

            for (MovieEntity movie : movies) {
                if (movie.getName().equals(value)) {
                    return movie;
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Please select a valid value");
        }

        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String) {
            return "";
        }

        if (value instanceof MovieEntity) {
            MovieEntity movie = (MovieEntity) value;

            try {
                return movie.getName();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid value");
            }
        } else {
            throw new IllegalArgumentException("Invalid value");
        }
    }
}
