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



public class Trip implements Comparable<Trip> {
	private String routeID;
	private String shapeID;
	private String tripID;
	private String name;
	private StopsList stopList;

	public Trip(String tripID, String routeID, String shapeID, String name, StopsList stopList) {
		super();
		this.routeID = routeID;
		this.shapeID = shapeID;
		this.tripID = tripID;
		this.name = name;
		this.stopList = stopList;
	}

	public String getTripID() {
		return tripID;
	}

	public String getRouteID() {
		return routeID;
	}

	public String getShapeID() {
		return shapeID;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		Trip other = (Trip) obj;
		return (other.routeID.equals(routeID) && 
		        other.shapeID.equals(shapeID) && 
		       ((other.stopList == null && stopList == null) || 
		        (other.stopList != null && other.stopList.equalsStops(stopList))));
	}
	@Override
	public int hashCode() {
		return routeID.hashCode() + shapeID.hashCode();
	}

	@Override
	public int compareTo(Trip o) {
		int a = routeID.compareTo(o.routeID);
		if (a == 0){
			a = shapeID.compareTo(o.shapeID);
			if (a == 0 && stopList != null && o.getStopTime() != null){
				return stopList.getId().compareTo(o.getStopTime().getId());
			}else{
				return a;
			}
		}else{
			return a;
		}
	}

	private StopsList getStopTime() {
		return stopList;
	}
}
