/**
   Licensed under the GNU General Public License version 3
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.gnu.org/licenses/gpl-3.0.html

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 **/
package it.osm.gtfs.command;

import it.osm.gtfs.input.GTFSParser;
import it.osm.gtfs.input.OSMParser;
import it.osm.gtfs.model.BoundingBox;
import it.osm.gtfs.model.Relation;
import it.osm.gtfs.model.Stop;
import it.osm.gtfs.model.Stop.GTFSStop;
import it.osm.gtfs.utils.DownloadUtils;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSUpdateDataFromOSM {
	public static void run() throws IOException, InterruptedException, ParserConfigurationException, SAXException {
		new File(GTFSImportSetting.getInstance().getOSMCachePath()).mkdirs();
		updateBusStops();
		updateBaseRels();
		updateFullRels();
	}

	private static void updateBusStops() throws IOException, InterruptedException{
		List<GTFSStop> gtfs = GTFSParser.readBusStop(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_STOP_FILE_NAME);
		BoundingBox bb = new BoundingBox(gtfs);

		String urlbus = GTFSImportSetting.OSM_XAPI_SERVER + "node" + bb.getXAPIQuery() + "[highway=bus_stop]";
		File filebus = new File(GTFSImportSetting.getInstance().getOSMCachePath() + "tmp_nbus.osm");
		DownloadUtils.downlod(urlbus, filebus);

		String urltrm = GTFSImportSetting.OSM_XAPI_SERVER + "node" + bb.getXAPIQuery() + "[railway=tram_stop]";
		File filetrm = new File(GTFSImportSetting.getInstance().getOSMCachePath() + "tmp_ntram.osm");
		DownloadUtils.downlod(urltrm, filetrm);

		String urlmtr = GTFSImportSetting.OSM_XAPI_SERVER + "node" + bb.getXAPIQuery() + "[railway=station]";
		File filemtr = new File(GTFSImportSetting.getInstance().getOSMCachePath() + "tmp_nmetro.osm");
		DownloadUtils.downlod(urlmtr, filemtr);

		List<File> input = new ArrayList<File>();
		input.add(filebus);
		input.add(filetrm);
		input.add(filemtr);

		File fileout = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_STOP_FILE_NAME);
		checkProcessOutput(runOsmosisMerge(input, fileout));
	}

	private static void updateBaseRels() throws MalformedURLException, IOException{
		String urlrel = GTFSImportSetting.OSM_XAPI_SERVER + "relation[network=" + GTFSImportSetting.getInstance().getNetwork() +  "]";
		File filerel = new File(GTFSImportSetting.getInstance().getOSMCachePath() + "tmp_rels.osm");
		DownloadUtils.downlod(urlrel, filerel);
	}

	private static void updateFullRels() throws ParserConfigurationException, SAXException, IOException, InterruptedException{
		List<Stop> osmStops = OSMParser.readOSMStops(GTFSImportSetting.getInstance().getOSMPath() +  GTFSImportSetting.OSM_STOP_FILE_NAME);
		Map<String, Stop> osmstopsOsmID = OSMParser.applyOSMIndex(osmStops);
		List<Relation> osmRels = OSMParser.readOSMRelations(new File(GTFSImportSetting.getInstance().getOSMPath() +  "tmp_rels.osm"), osmstopsOsmID);

		Process previousTask = null;
		List<File> sorted = new ArrayList<File>();
		for (Relation r:osmRels){
			System.out.println("Processing relation " + r.getId() + " " + r.name);
			File filesorted = new File(GTFSImportSetting.getInstance().getOSMPath() + "tmp_s" + r.getId() + ".osm");
			sorted.add(filesorted);
			
			if (!filesorted.exists() || OSMParser.readOSMRelations(filesorted, osmstopsOsmID).get(0).version < r.version){
				File filerelation = new File(GTFSImportSetting.getInstance().getOSMPath() + "tmp_r" + r.getId() + ".osm");
				if (!filerelation.exists() || OSMParser.readOSMRelations(filerelation, osmstopsOsmID).get(0).version < r.version){
					String url = GTFSImportSetting.OSM_XAPI_SERVER + "relation/" + r.getId() + "/full";
					DownloadUtils.downlod(url, filerelation);
				}

				Process current = runOsmosisSort(filerelation, filesorted);
				checkProcessOutput(previousTask);
				previousTask = current;
			}
		}
		checkProcessOutput(previousTask);

		File filestops = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_STOP_FILE_NAME);
		File fileout = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_RELATIONS_FILE_NAME);
		sorted.add(filestops);

		checkProcessOutput(runOsmosisMerge(sorted, fileout));
	}

	private static void checkProcessOutput(Process process) throws InterruptedException{
		if (process != null){
			process.waitFor();
			if (process.exitValue() != 0){
				System.err.println("Error " + process.exitValue() + " occurred while running osmosis.");
				throw new UnknownError("Error while running external process.");
			}
		}
	}

	private static Process runOsmosisSort(File input, File output) throws IOException{
		List<String> commands = new ArrayList<String>();
		commands.add(GTFSImportSetting.getInstance().getOsmosisPath() + "osmosis");
		commands.add("--read-xml");
		commands.add(input.getAbsolutePath());
		commands.add("--sort");
		commands.add("--write-xml");
		commands.add(output.getAbsolutePath());
		return Runtime.getRuntime().exec(commands.toArray(new String []{}));
	}

	private static Process runOsmosisMerge(List<File> input, File output) throws IOException{
		List<String> commands = new ArrayList<String>();
		commands.add(GTFSImportSetting.getInstance().getOsmosisPath() + "osmosis");
		for (File f:input){
			commands.add("--read-xml");
			commands.add(f.getAbsolutePath());
		}
		for (int i = 1; i<input.size(); i++)
			commands.add("--merge");
		commands.add("--write-xml");
		commands.add(output.getAbsolutePath());

		return Runtime.getRuntime().exec(commands.toArray(new String []{}));
	}
}
