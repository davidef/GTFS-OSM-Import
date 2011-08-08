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
package it.osm.gtfs.model;

import java.util.HashSet;
import java.util.Set;

public class Route {
	private String id;
	private String shortName;
	private String longName;
	private Set<String> shapesIDs;
	
	public Route(String id, String shortName, String longName) {
		super();
		this.id = id;
		this.shortName = shortName;
		this.longName = longName;
		shapesIDs = new HashSet<String>();
	}

	public String getId() {
		return id;
	}

	public String getShortName() {
		return shortName;
	}

	public String getLongName() {
		return longName;
	}

	public Set<String> getShapesIDs() {
		return shapesIDs;
	}
	
	public void putShape(String id){
		shapesIDs.add(id);
	}
}
