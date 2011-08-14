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

package it.osm.gtfs;

import it.osm.gtfs.command.GTFSGenerateBusStopsImport;
import it.osm.gtfs.command.GTFSGenerateRoutesBaseRelations;
import it.osm.gtfs.command.GTFSGenerateRoutesDiff;
import it.osm.gtfs.command.GTFSGenerateRoutesGPXs;
import it.osm.gtfs.command.GTFSGetBoundingBox;
import it.osm.gtfs.command.GTFSGetBusStopFromOSM;
import it.osm.gtfs.command.GTFSGetRelationsFromOSM;
import it.osm.gtfs.utils.GTFSImportSetting;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import asg.cliche.CLIException;
import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;

public class GTFSOSMImport {
	
	@Command(description="Get the Bounding Box of the GTFS File and xapi links")
	public void bbox() throws IOException, ParserConfigurationException, SAXException, TransformerException {
		GTFSGetBoundingBox.run();
	}
	
	@Command(description="Generate/update stops.osm file from xapi server")
	public void osmbus() throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
		GTFSGetBusStopFromOSM.run();
	}
	@Command(description="Generate/update relation.osm file from xapi server")
	public void osmrels() throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
		GTFSGetRelationsFromOSM.run();
	}
	
	@Command(description="Generate files to import bus stops into osm merging with existing stops")
	public void stops() throws IOException, ParserConfigurationException, SAXException, TransformerException {
		GTFSGenerateBusStopsImport.run();
	}

	@Command(description="Generate .gpx file for all GTFS Trips")
	public void gpx() throws IOException, ParserConfigurationException, SAXException {
		GTFSGenerateRoutesGPXs.run();
	}

	@Command(description="Generate the base relations (including only stops) to be used only when importing without any existing relation in osm")
	public void rels() throws IOException, ParserConfigurationException, SAXException {
		GTFSGenerateRoutesBaseRelations.run();
	}
	
	@Command(description="Analyze the diff between osm relations and gtfs trips")
	public void reldiff() throws IOException, ParserConfigurationException, SAXException {
		GTFSGenerateRoutesDiff.run();
	}

	@Command(description="Display current configuration")
	public String conf(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("Current Configuration:\n");
		buffer.append("GTFS Path: " + GTFSImportSetting.getInstance().getGTFSPath() + "\n");
		buffer.append("OSM Path: " + GTFSImportSetting.getInstance().getOSMPath() + "\n");
		buffer.append("OUTPUT Path: " + GTFSImportSetting.getInstance().getOutputPath() + "\n");
		buffer.append("Operator: " + GTFSImportSetting.getInstance().getOperator() + "\n");
		buffer.append("RevisitedKey: " + GTFSImportSetting.getInstance().getRevisitedKey() + "\n");
		buffer.append("Plugin Class: " + GTFSImportSetting.getInstance().getPlugin().getClass().getCanonicalName() + "\n");
		return buffer.toString();
	}
	
	@Command(description="Display available commands")
	public String help(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("Available commands:\n");
		for (Method method:this.getClass().getMethods()){
			for(Annotation annotation : method.getDeclaredAnnotations()){
				if(annotation instanceof Command){
					Command myAnnotation = (Command) annotation;
					buffer.append(method.getName() + "\t" + myAnnotation.description() + "\n");
				}
			}
		}
		buffer.append("exit\tExit from GTFSImport\n");
		return buffer.toString();
	}

	public static void main(String[] args) throws IOException, CLIException {
		initChecks();
		System.out.println("GTFS Import\n");
		Shell shell = ShellFactory.createConsoleShell("GTFSImport", "", new GTFSOSMImport());
		shell.processLine("conf");
		shell.processLine("help");
		shell.commandLoop();
	}
	
	private static void initChecks(){
		if (Runtime.getRuntime().maxMemory() < 1000000000){
			throw new IllegalArgumentException("You need to configure JVM to allow al least 1GB ram usage.");
		}
	}
}
