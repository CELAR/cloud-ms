import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.probepack.Probe;
import eu.celarcloud.jcatascopia.probepack.ProbeMetric;
import eu.celarcloud.jcatascopia.probepack.ProbePropertyType;

/*
 * NOTE:
 * Postgres must be configured to accept remote connections in pg_hba.conf file
 * and listening address in postgress.conf file must be set to * 
 */
public class PostgresProbe extends Probe{
	
	private static int DEFAULT_SAMPLING_PERIOD = 20;
	private static String DEFAULT_PROBE_NAME = "PostgresProbe";
	
	private static final String CONFIG_PATH = "postgres.properties";
	private Properties config;
	
	private Connection conn;
	private PreparedStatement ps;

	public PostgresProbe(String name, int freq) {
		super(name, freq);
		
		parseConfig();
		String host = config.getProperty("postgres.host", "localhost");
		String port = config.getProperty("postgres.port", "5432");
		String username = config.getProperty("postgres.username","");
		String password = config.getProperty("postgres.password", "");
		String database = config.getProperty("postgres.database", "");
		
		this.addProbeProperty(0, "pgActiveConnections", ProbePropertyType.INTEGER, "#", "total number of active connections to database");
		this.addProbeProperty(1, "pgDBsizeKB", ProbePropertyType.INTEGER, "KB", "total disk usage of database in KB");
		this.addProbeProperty(2, "pgBlocksDiskRead", ProbePropertyType.INTEGER, "#", "blocks read directly from disk or operating system since last checkpoint");
		this.addProbeProperty(3, "pgBlocksCacheHit", ProbePropertyType.INTEGER, "#", "Blocks read from PostgreSQL Cache per second since last checkpoint");

		this.conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+database+"",username, password);
			this.ps = conn.prepareStatement("SELECT numbackends, pg_database_size('"+database+"') as dbsize, blks_read, blks_hit " +
											 "FROM pg_stat_database WHERE datname='"+database+"'");
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			this.writeToProbeLog(Level.SEVERE, e.getMessage());
		} 
		catch (SQLException e) {
			e.printStackTrace();
			this.writeToProbeLog(Level.SEVERE, e.getMessage());
		}
	}
	
	public PostgresProbe(){
		this(DEFAULT_PROBE_NAME, DEFAULT_SAMPLING_PERIOD);
	}

	@Override
	public ProbeMetric collect() {
		HashMap<Integer,Object> values = null;
		try {
			ResultSet rs = this.ps.executeQuery();
			if (rs.next()){
				System.out.println("pgActiveConnections: "+rs.getString("numbackends")+", pgDBsizeKB: "+rs.getInt("dbsize")/1024+", pgBlocksDiskRead: "+rs.getString("blks_read")+", pgBlocksCacheHit: "+rs.getString("blks_hit"));
				values = new HashMap<Integer,Object>();
				values.put(0, Integer.parseInt(rs.getString("numbackends")));
				values.put(1, rs.getInt("dbsize")/1024);
				values.put(2, Integer.parseInt(rs.getString("blks_read")));
				values.put(3, Integer.parseInt(rs.getString("blks_hit")));
			}	
		} 
		catch (SQLException e) {
			e.printStackTrace();
			this.writeToProbeLog(Level.SEVERE, e.getMessage());
		}
		return new ProbeMetric(values);
	}

	@Override
	public String getDescription() {
		return "Probe that collects performance metrics from Postgres database";
	}
	
	//parse the configuration file
	private void parseConfig(){
		this.config = new Properties();
		//load config properties file
		try {				
			InputStream fis = getClass().getResourceAsStream(CONFIG_PATH);
			config.load(fis);
			if (fis != null)
	    		fis.close();
		} 
		catch (FileNotFoundException e){
			this.writeToProbeLog(Level.SEVERE,"config file not found");
		} 
		catch (IOException e){
			this.writeToProbeLog(Level.SEVERE,"config file parsing error");
		}
	}
	
	public static void main(String[] args) {
		PostgresProbe p = new PostgresProbe();
		p.activate();
	}
}
