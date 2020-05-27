/*
 * generated by Xtext 2.18.0.M3
 */
package eu.quanticol.moonlight.xtext.tests

import com.google.inject.Inject
import eu.quanticol.moonlight.xtext.moonLightScript.Model
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import eu.quanticol.moonlight.xtext.generator.ScriptToJava
import eu.quanticol.moonlight.compiler.MoonlightCompiler
import eu.quanticol.moonlight.MoonLightScript
import eu.quanticol.moonlight.MoonLightTemporalScript
import eu.quanticol.moonlight.MoonLightSpatialTemporalScript

@ExtendWith(InjectionExtension)
@InjectWith(MoonLightScriptInjectorProvider)
class TestCompiler {
	@Inject
	ParseHelper<Model> parseHelper
	
	@Test
	def void loadModelSpatialModel() {
		val result = parseHelper.parse('''
			type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;		
			
			signal { bool taxi; int people; }
			space { locations {poiType poi; }
			edges { real length; }
			}
			domain boolean;
			formula aFormula = somewhere [0.0, 1.0] ( taxi );
			formula anotherFormula(int v) = (people > v);
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}
	
	@Test
	def void loadTemporalModel() {
		val result = parseHelper.parse('''
			type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;		
			
			signal { bool x; real y; int z; }
			domain boolean;
			formula aFormula(int a,int b) = eventually [a,b] ( y>0 );
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}

	@Test
	def void testErrorSignal() {
		val result = parseHelper.parse('''
            signal { real x; real y; }
            domain boolean;
            formula future = globally [0, 0.2]  (x <= y);
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}
	
	@Test
	def void loadModelWithParameters() {
		val result = parseHelper.parse('''
			type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;		
			
			signal { bool taxi; int peole; }
			space { 
				locations {poiType poi; }
				edges { 
					real length; 
					int hop;
				}
			}
			domain boolean;
			formula aFormula(int hop) = somewhere(hop) [0.0, distance] ( taxi );
		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
	}
	
	
	@Test
	def void compileAndLoadClass() {
		val result = parseHelper.parse('''
			type poiType = BusStop|Hospital|MetroStop|MainSquare|Museum;		
			
			signal { bool taxi; int people; }
			space { 
				edges { real length; 
						int hop;
				}
			}
			domain boolean;
			formula aFormula(int steps, int v) = globally [0.0, 1.0]{somewhere(hop) [0.0, steps] ( taxi ) & anotherFormula(v);
			formula anotherFormula(int v) = (people > v);
		''')
		val scriptToJava = new ScriptToJava();		
		val generatedCode = scriptToJava.getJavaCode(result,"moonlight.test","CityMonitor")
		System.out.println(generatedCode);
		val comp = new MoonlightCompiler();
		val script = comp.getIstance("moonlight.test","CityMonitor",generatedCode.toString,typeof(MoonLightSpatialTemporalScript))
		Assertions.assertEquals(2, script.spatialTemporalMonitors.length)
	}

    @Test
    def void testErrorSignalLoad() {
        val result = parseHelper.parse('''
            signal { real x; real y; }
            domain boolean;
            formula future = globally [0, 0.2]  (x <= y);
        ''')
		val scriptToJava = new ScriptToJava();
		val generatedCode = scriptToJava.getJavaCode(result,"moonlight.test","CityMonitor")
		System.out.println(generatedCode);
		val comp = new MoonlightCompiler();
		val script = comp.getIstance("moonlight.test","CityMonitor",generatedCode.toString,typeof(MoonLightTemporalScript))
		Assertions.assertTrue(true);
    }
	
	@Test
	def void testReachMonitorQualitative() {
		val result = parseHelper.parse('''
                signal { int nodeType; real battery; real temperature; }
             	space {
             	edges { int hop; real dist; }
             	}
             	domain boolean;
             	formula SensNetkBool = everywhere[0.0, 5.0] (  nodeType==2 ) ;
            		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
		val scriptToJava = new ScriptToJava();		
		val generatedCode = scriptToJava.getJavaCode(result,"moonlight.test","CityMonitor")
		System.out.println(generatedCode);
		val comp = new MoonlightCompiler();
		val script = comp.getIstance("moonlight.test","CityMonitor",generatedCode.toString,typeof(MoonLightSpatialTemporalScript))
		Assertions.assertNotNull(script)
	}	
	
	@Test
	def void testReachMonitorQuantitative() {
		val result = parseHelper.parse('''
                signal { int nodeType; real battery; real temperature; }
             	space {
             	edges { int hop; real dist; }
             	}
             	domain minmax;
             	formula Prova = everywhere[0.0, 5.0] (battery > 0.5 ) ;
            		''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
		val scriptToJava = new ScriptToJava();		
		val generatedCode = scriptToJava.getJavaCode(result,"moonlight.test","CityMonitor")
		System.out.println(generatedCode);
		val comp = new MoonlightCompiler();
		val script = comp.getIstance("moonlight.test","CityMonitor",generatedCode.toString,typeof(MoonLightSpatialTemporalScript))
		Assertions.assertNotNull(script)
	}		
	
	
	@Test
	def void testSpatialTemporal() {
		val result = parseHelper.parse('''
		signal { int nodeType; real battery; real temperature; }
		space {
		 	edges { int hop; real dist; }
		}
		domain boolean;
		
		formula SensTemp = somewhere(hop)[0, 3]{globally [0, 0.2]  (battery > 0.5)};

		formula SensTemp2 = somewhere(hop)[0, 3]{eventually [0, 0.2]  (  battery > 0.5 )};

		formula SensTemp3 = somewhere(hop)[0, 3]{once [0, 0.2]  (  battery > 0.5 )};

		formula SensTemp4 = somewhere(hop)[0, 3]{historically [0, 0.2]  (  battery > 0.5 )};

		formula SensTem5 =  somewhere(hop)[0, 3]{(  battery > 0.5 ) until[0, 0.2]  (  battery > 0.5 )};

		formula SensTem6 = somewhere(hop)[0, 3]{(  battery > 0.5 ) since[0, 0.2]  (  battery > 0.5 )};
        ''')
		Assertions.assertNotNull(result)
		val errors = result.eResource.errors
		Assertions.assertTrue(errors.isEmpty, '''Unexpected errors: «errors.join(", ")»''')
		val scriptToJava = new ScriptToJava();		
		val generatedCode = scriptToJava.getJavaCode(result,"moonlight.test","CityMonitor")
		System.out.println(generatedCode);
		val comp = new MoonlightCompiler();
		val script = comp.getIstance("moonlight.test","CityMonitor",generatedCode.toString,typeof(MoonLightSpatialTemporalScript))
		Assertions.assertNotNull(script)
	}		
}
