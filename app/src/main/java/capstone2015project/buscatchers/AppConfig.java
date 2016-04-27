package capstone2015project.buscatchers;
/**
 * Created by Waqar on 08-Apr-16.
 * Application constants
 */
public class AppConfig {
    //prevent class instantiation
    private AppConfig(){}
    //URL to fetch stops data from Foli API
    private static final String FOLI_STOPS_URL = "http://data.foli.fi/gtfs/v0/stops";
    //URL to fetch real time stop data
    private static final String FOLI_REALTIME_STOPS_URL = "http://data.foli.fi/siri/sm/";
    //Default latitude
    private static final double DEFAULT_LAT = 60.4491652;
    //Default longitude
    private static final double DEFAULT_LON = 22.2933068;
    //getters, this will not change original strings
    public static String getFoliStopsUrl() { return FOLI_STOPS_URL; }
    public static String getFoliRealtimeStopsUrl() { return FOLI_REALTIME_STOPS_URL; }
    public static double getDefaultLat() { return DEFAULT_LAT; }
    public static double getDefaultLon() { return DEFAULT_LON; }
}