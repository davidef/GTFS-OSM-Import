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
package it.osm.gtfs.plugins;

import it.osm.gtfs.model.Route;
import it.osm.gtfs.model.Stop;

public class GTTTurinPlugin implements GTFSPlugin {
	public String fixBusStopName(String busStopName){
		busStopName = busStopName.replace('"', '\'')
		    .replaceAll("Fermata [\\d]* - ", "").replaceAll("FERMATA [\\d]* - ", "")
		    .replaceAll("Fermata ST[\\d]* - ", "").replaceAll("Fermata S00[\\d]* - ", "");
		if (Character.isUpperCase(busStopName.charAt(1))){
			return camelCase(busStopName).trim();
		}
		return busStopName;
	}

	@Override
	public String fixTripName(String name) {
		return camelCase(name).trim();
	}
	
	private static String camelCase(String string) {
		String[] words = string.split("\\s");
		StringBuffer buffer = new StringBuffer();
		for (String s : words) {
			buffer.append(capitalize(s) + " ");
		}
		return buffer.toString();
	}

	private static String capitalize(String string) {
		if (string.length() == 0) return string;
		return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
	}

	@Override
	public Boolean isValidStop(Stop gs) {
		/*try{
			Integer.parseInt(gs.getCode());
		}catch(Exception e){
			System.err.println("Warning not numeric ref: " + gs.getCode() + " " + gs.getName() + " " + gs.getGtfsId());
		}*/
		return true;
	}

	@Override
	public boolean isValidRoute(Route route) {
		return !"GTT_E".equals(route.getAgencyId());
	}
}
