package it.osm.gtfs.command;

import it.osm.gtfs.input.OSMParser;
import it.osm.gtfs.model.Relation;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSGenerateSQLLiteDB {

	public static void run() throws ParserConfigurationException, SAXException,
	IOException {
		System.err.println("Parsing OSM Stops");
		List<Stop> osmStops = OSMParser.readOSMStops(GTFSImportSetting
				.getInstance().getOSMPath()
				+ GTFSImportSetting.OSM_STOP_FILE_NAME);

		System.err.println("Indexing OSM Stops");
		Map<String, Stop> osmstopsOsmID = OSMParser.applyOSMIndex(osmStops);
		
		System.err.println("Parsing OSM Relation");
		List<Relation> osmRels = OSMParser.readOSMRelations(new File(
				GTFSImportSetting.getInstance().getOSMPath()
				+ GTFSImportSetting.OSM_RELATIONS_FILE_NAME),
				osmstopsOsmID);


		GTFSGenerateSQLLiteDB generator = null;
		try {
			System.err.println("Creating SQLite DB");
			generator = new GTFSGenerateSQLLiteDB("gtt.db");
			generator.createDB();
			System.err.println("Adding Stops to SQLite DB");
			generator.insertStops(osmStops);
			System.err.println("Adding Relations to SQLite DB");
			generator.insertRelations(osmRels);
			System.err.println("Done.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			if (generator != null)
				generator.close();
		}
	}

	private Connection connection = null;

	private GTFSGenerateSQLLiteDB(String file) throws SQLException,
	ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + file);
	}

	private void close() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			// connection close failed.
			System.err.println(e);
		}
	}

	private void createDB() throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.

		statement.executeUpdate("drop table if exists relation_stops");
		statement.executeUpdate("drop table if exists relation");
		statement.executeUpdate("drop table if exists stop");
		statement.executeUpdate("create table stop (id long, ref TEXT, name TEXT, lat double, lon double, tilex long, tiley long)");
		statement.executeUpdate("create table relation (id long, ref TEXT, name TEXT, src TEXT, dst TEXT, type int)");
		statement.executeUpdate("create table relation_stops (relation_id long, stop_id long, pos long," +
		"FOREIGN KEY(relation_id) REFERENCES relation(id), FOREIGN KEY(stop_id) REFERENCES stop(id))");
		statement.executeUpdate("CREATE INDEX relations ON relation_stops(relation_id ASC)");
	}

	private void insertStops(List<Stop> stops) throws SQLException {
		PreparedStatement stm = connection
		.prepareStatement("insert into stop values(?, ?, ?, ?, ?, ?, ?)");
		for (Stop s : stops) {
			stm.setLong(1, Long.valueOf(s.getOSMId()));
			stm.setString(2, s.getCode());
			stm.setString(3, s.getName());
			stm.setDouble(4, s.getLat());
			stm.setDouble(5, s.getLon());
			stm.setLong(6, getTileX(s.getLat(), s.getLon(), 18, 256));
			stm.setLong(7, getTileY(s.getLat(), s.getLon(), 18, 256));
			stm.executeUpdate();
		}
	}

	private void insertRelations(List<Relation> rels) throws SQLException {
		PreparedStatement stm = connection.prepareStatement("insert into relation values(?, ?, ?, ?, ?, ?)");
		for (Relation r : rels) {
			stm.setLong(1, Long.valueOf(r.getId()));
			stm.setString(2, r.getRef());
			stm.setString(3, r.getName());
			stm.setString(4, r.getFrom());
			stm.setString(5, r.getTo());
			stm.setInt(6, r.getType().dbId());
			stm.executeUpdate();
		}
		stm = connection.prepareStatement("insert into relation_stops values(?, ?, ?)");
		for (Relation r : rels) {
			for (Long k:r.getStops().keySet()){
				stm.setLong(1, Long.valueOf(r.getId()));
				stm.setLong(2, Long.valueOf(r.getStops().get(k).getOSMId()));
				stm.setLong(3, k);
				stm.executeUpdate();
			}
		}
	}

	public long getTileX(final double lat, final double lon, final int zoom,
			final int size) {
		return (long) Math.floor((lon + 180) / 360 * (1 << zoom));
	}

	public long getTileY(final double lat, final double lon, final int zoom,
			final int size) {
		return (long) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat))
				+ 1 / Math.cos(Math.toRadians(lat)))
				/ Math.PI)
				/ 2 * (1 << zoom));
	}

}
