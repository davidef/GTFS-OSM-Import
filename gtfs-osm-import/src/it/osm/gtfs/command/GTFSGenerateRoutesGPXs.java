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
import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Shape;
import it.osm.gtfs.model.StopTimes;
import it.osm.gtfs.model.Trip;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GTFSGenerateRoutesGPXs {
	public static void run() throws IOException, ParserConfigurationException, SAXException {
		Map<String, Route> rt = GTFSParser.readRoutes(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_ROUTES_FILE_NAME);
		Map<String, Shape> sh = GTFSParser.readShapes(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_SHAPES_FILE_NAME);
		Set<Trip> trips = new HashSet<Trip>(GTFSParser.readTrips(GTFSImportSetting.getInstance().getGTFSPath() + GTFSImportSetting.GTFS_TRIPS_FILE_NAME, new HashMap<String, StopTimes>()));
		
		int id = 10000;
		
		new File(GTFSImportSetting.getInstance().getOutputPath() + "gpx").mkdirs();
		
		for (Trip t:trips){
			Route r = rt.get(t.getRouteID());
			Shape s = sh.get(t.getShapeID());
			
			FileOutputStream f = new FileOutputStream(GTFSImportSetting.getInstance().getOutputPath() + "/gpx/r" + id++ + " " + r.getShortName().replace("/", "B") + " " + t.getName().replace("/", "_") + ".gpx");
			f.write(s.getGPX(r.getShortName()).getBytes());
			f.close();
		}
	}
}
