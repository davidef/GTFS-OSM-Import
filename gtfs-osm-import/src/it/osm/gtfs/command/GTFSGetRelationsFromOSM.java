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

import it.osm.gtfs.utils.DownloadUtils;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.File;
import java.io.IOException;

public class GTFSGetRelationsFromOSM {
	public static void run() throws IOException, InterruptedException {
		String urlrel = "http://open.mapquestapi.com/xapi/api/0.6/relation[network=" + GTFSImportSetting.getInstance().getNetwork() +  "]";
		File filerel = new File(GTFSImportSetting.getInstance().getOSMPath() + GTFSImportSetting.OSM_RELATIONS_FILE_NAME);
		
		DownloadUtils.downlod(urlrel, filerel);
	}
}
