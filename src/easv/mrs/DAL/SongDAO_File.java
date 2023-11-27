package easv.mrs.DAL;

import easv.mrs.BE.Song;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import static java.nio.file.StandardOpenOption.APPEND;


public class SongDAO_File implements ISongDataAccess {

    private static final String MOVIES_FILE = "data/movie_titles.txt";

    public List<Song> getAllSongs() throws IOException {

        // Read all lines from file
        List<String> lines = Files.readAllLines(Path.of(MOVIES_FILE));
        List<Song> songs = new ArrayList<>();

        // Parse each line as movie
        for (String line: lines) {
            String[] separatedLine = line.split(",");

            int id = Integer.parseInt(separatedLine[0]);
            int year = Integer.parseInt(separatedLine[1]);
            String title = separatedLine[2];
            String artist = separatedLine[3];
            if(separatedLine.length > 3)
            {
                for(int i = 3; i < separatedLine.length; i++)
                {
                    title += "," + separatedLine[i];
                }
            }
            Song song = new Song(id, year, title, artist);
            songs.add(song);
        }

        return songs;
    }

    @Override
    public Song createSong(Song song) throws Exception {

        List<String> movies = Files.readAllLines(Path.of(MOVIES_FILE));

        if (movies.size() > 0) {
            // get next id
            String[] separatedLine = movies.get(movies.size() - 1).split(",");
            int nextId = Integer.parseInt(separatedLine[0]) + 1;
            String newMovieLine = nextId + "," + song.getYear() + "," + song.getTitle();
            Files.write(Path.of(MOVIES_FILE), (newMovieLine + "\r\n").getBytes(), APPEND);

            return new Song(nextId, song.getYear(), song.getTitle(),song.getArtist());
        }

        return null;

    }

    @Override
    public void updateSong(Song song) throws Exception {

    }

    @Override
    public void deleteSong(Song song) throws Exception {

    }






    /*
    public List<Movie> getAllMovies() {
        List<Movie> allMovieList = new ArrayList<>();

        File moviesFile = new File(MOVIES_FILE);


        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(moviesFile))) {
            boolean hasLines = true;
            while (hasLines) {
                String line = bufferedReader.readLine();
                hasLines = (line != null);
                if (hasLines && !line.isBlank())
                {
                    String[] separatedLine = line.split(",");

                    int id = Integer.parseInt(separatedLine[0]);
                    int year = Integer.parseInt(separatedLine[1]);
                    String title = separatedLine[2];
                    if(separatedLine.length > 3)
                    {
                        for(int i = 3; i < separatedLine.length; i++)
                        {
                            title += "," + separatedLine[i];
                        }
                    }
                    Movie movie = new Movie(id, title, year);
                    allMovieList.add(movie);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allMovieList;
    }
    */


}