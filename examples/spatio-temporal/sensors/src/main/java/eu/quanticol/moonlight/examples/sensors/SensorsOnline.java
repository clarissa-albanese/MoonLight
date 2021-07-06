package eu.quanticol.moonlight.examples.sensors;

import com.mathworks.engine.MatlabEngine;
import eu.quanticol.moonlight.domain.*;
import eu.quanticol.moonlight.formula.AtomicFormula;
import eu.quanticol.moonlight.formula.Formula;
import eu.quanticol.moonlight.formula.Parameters;
import eu.quanticol.moonlight.formula.SomewhereFormula;
import eu.quanticol.moonlight.monitoring.SpatialTemporalMonitoring;
import eu.quanticol.moonlight.monitoring.online.OnlineSpaceTimeMonitor;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.signal.SpatialTemporalSignal;
import eu.quanticol.moonlight.signal.online.*;
import eu.quanticol.moonlight.space.DistanceStructure;
import eu.quanticol.moonlight.space.LocationService;
import eu.quanticol.moonlight.space.SpatialModel;
import eu.quanticol.moonlight.util.Pair;
import eu.quanticol.moonlight.util.Stopwatch;
import eu.quanticol.moonlight.util.Utils;
import eu.quanticol.moonlight.utility.matlab.configurator.MatlabDataConverter;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SensorsOnline {
    private static Map<String, Function<Parameters,
                          Function<Pair<Integer,Integer>, Boolean>>> atomicFormulas;

    private static HashMap<String, Function<SpatialModel<Double>,
                               DistanceStructure<Double, ?>>> distanceFunctions;

    private static LocationService<Double, Double> locSvc;

    private static double nodes;
    private static Double[] nodesType;

    public static void main(String[] args)
            throws InterruptedException, ExecutionException, URISyntaxException
    {
        MatlabEngine eng = MatlabEngine.startMatlab();
        String localPath = Paths.get(SensorsOnline.class
                .getResource("mobility.m")
                .toURI()).getParent().toAbsolutePath().toString();
        eng.eval("addpath(\"" + localPath + "\")");


        Object[] graph = runModel(eng);

        locSvc = Utils.createLocServiceFromSetMatrix(graph);

        atomicFormulas = new HashMap<>();
        atomicFormulas.put("type1", p -> (x -> x.getFirst() == 1));
        atomicFormulas.put("type2", p -> (x -> x.getFirst() == 2));
        atomicFormulas.put("type3", p -> (x -> x.getFirst() == 3));

        distanceFunctions = new HashMap<>();
        distanceFunctions.put("dist",
                m -> new DistanceStructure<>(x -> x , new DoubleDistance(),
                        0.0, 1.0, m));

        Formula isType1 = new AtomicFormula("type1");
        Formula sWhere = new SomewhereFormula("dist", isType1);

        checkOffline(sWhere);
        checkOnline(sWhere);


        System.out.println("test");
    }

    private static Object[] runModel(MatlabEngine eng)
            throws ExecutionException, InterruptedException
    {
        /// Trace generation
        eng.eval("mobility");

        // Reading results
        nodes = eng.getVariable("num_nodes");
        nodesType = MatlabDataConverter.getArray(eng
                                                  .getVariable("nodes_type"),
                                                 Double.class);

        Object[] cgraph1 = eng.getVariable("cgraph1");
        Object[] cgraph2 = eng.getVariable("cgraph2");

        return cgraph1;
    }

    private static void checkOffline(Formula f)
    {
        SpatialTemporalSignal<Pair<Integer, Integer>> stSignal =
                                  new SpatialTemporalSignal<>(nodesType.length);

        IntStream.range(0, (int) nodes - 1)
                 .forEach(i -> stSignal
                                    .add(i, (location ->
                                            new Pair<>(nodesType[location]
                                                        .intValue(), i)
                                            )
                                        )
                         );

        // Actual monitoring...
        Stopwatch rec = Stopwatch.start();
        SpatialTemporalMonitor<Double, Pair<Integer, Integer>, Boolean>
                m = new SpatialTemporalMonitoring<>(atomicFormulas,
                                                    distanceFunctions,
                                                    new BooleanDomain(),
                                                    false)
                                .monitor(f, null);
        rec.stop();

        SpatialTemporalSignal<Boolean> sout = m.monitor(locSvc, stSignal);
        List<Signal<Boolean>> signals = sout.getSignals();
        System.out.println(signals.get(0));
    }

    private static void checkOnline(Formula f)
    {
        Map<String, Function<Pair<Integer,Integer>, AbstractInterval<Boolean>>> atoms = new HashMap<>();
        atoms.put("type1", x -> booleanInterval(x.getFirst() == 1));
        atoms.put("type2", x -> booleanInterval(x.getFirst() == 2));
        atoms.put("type3", x -> booleanInterval(x.getFirst() == 3));

        List<Update<Double, List<Pair<Integer, Integer>>>> updates = new ArrayList<>();

                Stopwatch rec = Stopwatch.start();
        OnlineSpaceTimeMonitor<Double, Pair<Integer, Integer>, Boolean> m =
                new OnlineSpaceTimeMonitor<>(
                        f, 0, new BooleanDomain(), locSvc, atoms, distanceFunctions);

        for(Update<Double, List<Pair<Integer, Integer>>> u : updates) {
            m.monitor(u);
            //LOG.info(() -> "Monitoring for " + u + " completed!");
        }
        rec.stop();

        //System.out.println(out.getValueAt(0.0).get(0));
    }

    private static AbstractInterval<Boolean> booleanInterval(boolean cond) {
        return cond ? new AbstractInterval<>(true, true) :
                      new AbstractInterval<>(false, false);
    }
}
