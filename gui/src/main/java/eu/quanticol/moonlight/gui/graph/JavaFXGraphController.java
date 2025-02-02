package eu.quanticol.moonlight.gui.graph;

import eu.quanticol.moonlight.gui.chart.ChartVisualization;
import eu.quanticol.moonlight.gui.chart.JavaFXChartController;
import eu.quanticol.moonlight.gui.filter.JavaFXFiltersController;
import eu.quanticol.moonlight.gui.JavaFXMainController;
import eu.quanticol.moonlight.gui.io.*;
import eu.quanticol.moonlight.gui.util.DialogBuilder;
import eu.quanticol.moonlight.gui.util.AttributesLinker;
import eu.quanticol.moonlight.gui.util.PositionsLinker;
import eu.quanticol.moonlight.gui.util.SimpleMouseManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.graphstream.graph.Graph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller of JavaFX for graphs
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class JavaFXGraphController {

    @FXML
    Label graphType;
    @FXML
    BorderPane borderPane;
    @FXML
    Label infoNode;
    @FXML
    AnchorPane labelPane;
    @FXML
    JavaFXNodesTableController nodeTableComponentController;
    @FXML
    JavaFXFiltersController filtersComponentController;
    @FXML
    Slider slider;
    @FXML
    SubScene scene;
    @FXML
    Button linkButton;

    private List<TimeGraph> graphList = new ArrayList<>();
    private Graph currentGraph;
    private String theme;
    private JavaFXChartController chartController;
    private GraphController graphController;
    private JavaFXMainController mainController;
    private boolean positionAssigned = false;
    private GraphType graphVisualization;
    private final ArrayList<FxViewer> viewers = new ArrayList<>();
    private final Label label = new Label();
    private final ArrayList<Double> time = new ArrayList<>();
    private RunnableSlider runnable = null;
    private final FilesLoader jsonFilesLoader = new JsonFilesLoader();
    private ArrayList<String> columnsAttributes = new ArrayList<>();
    private PositionsLinker linkController = null;
    private File csv = null;
    private File tra = null;


    /**
     * Listener for slider that updates the label of slider thumb and the graph visualized
     */
    private final ChangeListener<? super Number> sliderListener = (obs, oldValue, newValue) -> {
        Double value = nearest(time, newValue.doubleValue());
        Platform.runLater(() -> {
            label.setText(String.valueOf(value));
            changeGraphView(String.valueOf(value));
        });
    };

    public GraphType getGraphVisualization() {
        return graphVisualization;
    }

    public JavaFXFiltersController getFiltersComponentController() {
        return filtersComponentController;
    }

    public void setColumnsAttributes(ArrayList<String> columnsAttributes) {
        this.columnsAttributes = columnsAttributes;
        this.graphController.setColumnsAttributes(columnsAttributes);
    }

    public FilesLoader getJsonFilesLoader() {
        return jsonFilesLoader;
    }


    public File getCsv() {
        return csv;
    }

    public File getTra() {
        return tra;
    }

    public void setCsv(File csv) {
        this.csv = csv;
    }

    public void setTra(File tra) {
        this.tra = tra;
    }

    public ArrayList<String> getColumnsAttributes() {
        return columnsAttributes;
    }

    public ArrayList<Double> getTime() {
        return time;
    }

    public Slider getSlider() {
        return slider;
    }

    public boolean getPositionAssigned() {
        return this.positionAssigned;
    }

    public void setPositionAssigned(boolean positionAssigned) {
        this.positionAssigned = positionAssigned;
    }

    public List<TimeGraph> getGraphList() {
        return graphList;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public PositionsLinker getLinkController() {
        return linkController;
    }

    public void setLinkController(PositionsLinker linkController) {
        this.linkController = linkController;
    }

    public void injectMainController(JavaFXMainController mainController, JavaFXChartController chartComponentController) {
        this.mainController = mainController;
        this.chartController = chartComponentController;
        initialize();
    }

    private void initialize() {
        this.graphController = SimpleGraphController.getInstance();
        this.nodeTableComponentController.injectGraphController(graphController);
        this.filtersComponentController.injectGraphController(mainController, this, chartController);
        this.linkController = new PositionsLinker();
        loadPlaySpaceBar();
    }

    /**
     * Open the explorer to choose a file
     *
     * @param description description of file to choose
     * @param extensions  extension of a file to choose
     *
     * @return the file chosen
     */
    private File open(String description, String extensions) {
        if (runnable != null && slider != null)
            runnable.shutdown();
        reset();
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) mainController.getRoot().getScene().getWindow();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Opens explorer with only .tra files
     */
    public void openTRAExplorer() throws IOException {
        System.setProperty("org.graphstream.ui", "javafx");
        File file = open("TRA Files", "*.tra");
        loadTRA(file);
    }

    /**
     * Opens the chosen .tra file from recent
     */
    public void openRecentTRA(File file) throws IOException {
        System.setProperty("org.graphstream.ui", "javafx");
        loadTRA(file);
    }

    /**
     * Loads the .tra file
     *
     * @param file file .tra
     */
    private void loadTRA(File file) throws IOException {
        if (file != null) {
            chartController.clearMenuButton();
            resetAll();
            createGraphFromFile(file);
            tra = file;
            nodeTableComponentController.initTable();
            addRecentFile(file.getPath(), FileType.TRA);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens explorer with only .csv files for pieceWise linear visualization
     */
    public void openCSVExplorer() {
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            chartController.clearMenuButton();
            chartController.setGraphVisualization(ChartVisualization.PIECEWISE);
            chartController.setIndexOfAttributes(0);
            csv = file;
            loadCSV(file);
            linkButton.setDisable(false);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens the chosen .csv file from recent
     */
    public void openRecentCSV(File file) {
        if (file != null) {
            chartController.setIndexOfAttributes(0);
            loadCSV(file);
            chartController.setGraphVisualization(ChartVisualization.PIECEWISE);
            csv = file;
            linkButton.setDisable(false);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Opens the chosen .csv file in the chosen project
     */
    public void openCSVFromProject(File file) {
        if (file != null) {
            loadCSV(file);
            chartController.setGraphVisualization(ChartVisualization.PIECEWISE);
            csv = file;
            linkButton.setDisable(false);
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    /**
     * Loads the .csv file
     *
     * @param file file .csv
     */
    private void loadCSV(File file) {
        try {
            chartController.reset();
            readCSV(file);
            if (graphVisualization.equals(GraphType.DYNAMIC)) {
                resetCharts();
                chartController.createDataFromGraphs(graphList, chartController.getIndexOfAttributes());
                chartController.getAttribute().setText(columnsAttributes.get(chartController.getIndexOfAttributes() + 1));
            }
            addRecentFile(file.getPath(), FileType.CSV);
        } catch (Exception e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed to load chart data.");
        }
    }

    /**
     * Opens explorer with only .csv files for constant stepWise visualization
     */
    public void openConstantCsvExplorer() {
        File file = open("CSV Files", "*.csv");
        if (file != null) {
            try {
                chartController.setIndexOfAttributes(1);
                openConstantCsv(file);
                addRecentFile(file.getPath(), FileType.CSV);
            } catch (Exception e) {
                DialogBuilder d = new DialogBuilder(mainController.getTheme());
                d.error("Failed to load chart data.");
                e.printStackTrace();
            }
        } else {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.info("No file chosen.");
        }
    }

    public void openConstantCsv(File file) throws IOException {
        chartController.clearMenuButton();
        chartController.setGraphVisualization(ChartVisualization.STEPWISE);
        chartController.reset();
        csv = file;
        readConstantCSV(file);
        linkButton.setDisable(false);
    }

    /**
     * Adds the file opened into a json file and into the recent list
     *
     * @param file     path of file
     * @param fileType type of file
     */
    private void addRecentFile(String file, FileType fileType) throws IOException {
        jsonFilesLoader.saveToJson(file, fileType);
        mainController.getHomeController().loadFilesOnList();
    }

    /**
     * Reads a .csv file as a file with constants attributes
     */
    private void readConstantCSV(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        if (line.contains("time")) {
            associateAttributesColumns(line);
            line = br.readLine();
        } else if (this.columnsAttributes.isEmpty())
            openAssociateAttributesWindow(line);
        constantAttributesLink(br, line);
        chartController.initConstantChart(file);
        chartController.loadAttributesList(columnsAttributes);
        chartController.getAttribute().setText(columnsAttributes.get(chartController.getIndexOfAttributes()));
        if (graphVisualization.equals(GraphType.STATIC))
            chartController.addListenerConstantChart();
    }

    /**
     * Associates attributes from constant visualization
     */
    private void constantAttributesLink(BufferedReader br, String line) throws IOException {
        if (linkController.getColumnX() == null && linkController.getColumnY() == null)
            do {
                if (graphVisualization.equals(GraphType.DYNAMIC))
                    graphController.createNodesVector(line, linkController.getColumnX(), linkController.getColumnY(), false);
            }
            while (((line = br.readLine()) != null));
        else
            constantPositionsLink(br, line);
    }

    /**
     * Associates positions from constant visualization
     */
    private void constantPositionsLink(BufferedReader br, String line) throws IOException {
        do {
            if (graphVisualization.equals(GraphType.STATIC))
                graphController.createPositions(line, linkController.getColumnX(), linkController.getColumnY());
            if (graphVisualization.equals(GraphType.DYNAMIC))
                graphController.createNodesVector(line, linkController.getColumnX(), linkController.getColumnY(), true);
        }
        while (((line = br.readLine()) != null));
    }

    /**
     * Reset all lists and info
     */
    private void resetAll() {
        viewers.clear();
        graphList.clear();
        time.clear();
        nodeTableComponentController.resetTable();
        filtersComponentController.resetFiltersNewFile();
        chartController.reset();
        resetSlider();
        infoNode.setText(" ");
        labelPane.setPrefHeight(0);
        linkButton.setDisable(true);
    }

    private void reset() {
        positionAssigned = false;
        filtersComponentController.resetFilters();
        linkController.setColumnX(null);
        linkController.setColumnY(null);
        chartController.setAttributes("");
        chartController.getAttribute().getItems().clear();
        this.columnsAttributes.clear();
        filtersComponentController.getAttribute().getItems().clear();
        chartController.setIndexOfAttributes(1);
    }

    private void resetSlider() {
        slider.valueProperty().removeListener(sliderListener);
        slider.setLabelFormatter(null);
        label.setText("");
        slider.setMin(0);
        slider.setMax(50);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setValue(slider.getMin());
    }

    /**
     * Read a .csv file to get info about nodes
     *
     * @param file .csv file
     */
    private void readCSV(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        if (graphVisualization.equals(GraphType.STATIC))
            getStaticAttributesFromCsv(br);
        else
            getDynamicAttributesFromCsv(br);
    }

    /**
     * Creates vectors for every node in a single instant
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createNodesVector(String line, boolean present) {
        graphController.createNodesVector(line, linkController.getColumnX(), linkController.getColumnY(), present);
    }

    /**
     * Sets positions of nodes
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createPositions(String line) {
        graphController.createPositions(line, linkController.getColumnX(), linkController.getColumnY());
    }

    /**
     * Reads attributes of nodes of a static graph from a file and creates positions and charts
     *
     * @param br bufferedReader
     */
    private void getStaticAttributesFromCsv(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line != null) {
            if (line.contains("time")) {
                associateAttributesColumns(line);
                line = br.readLine();
            } else if (this.columnsAttributes.isEmpty())
                openAssociateAttributesWindow(line);
            linkPositions(br, line);
            chartController.loadAttributesList(columnsAttributes);
            chartController.getAttribute().setText(columnsAttributes.get(chartController.getIndexOfAttributes() + 1));
        }
    }

    /**
     * Associates attributes from linear visualization
     */
    private void linkPositions(BufferedReader br, String line) throws IOException {
        if (linkController.getColumnX() != null && linkController.getColumnY() != null)
            createPositions(line);
        resetCharts();
        createSeriesFromStaticGraph(line);
        do
            addLineDataToSeries(line);
        while (((line = br.readLine()) != null));
        chartController.linearSelected();
        chartController.initStatic();
    }

    /**
     * Opens a window for associate parameters of a csv file
     */
    private void openAssociateAttributesWindow(String line) throws IOException {
        String[] split = line.split(",");
        int totalAttributes = (split.length - 1) / graphController.getTotNodes();
        openWindow(totalAttributes);
        if (linkController.getColumnX() == null && linkController.getColumnY() == null)
            openWindowChoosePosition();
        if (linkController.getColumnX() != null && linkController.getColumnY() != null)
            this.positionAssigned = true;
    }

    private void openWindow(int totalAttributes) {
        try {
            FXMLLoader fxmlLoader;
            fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource("fxml/associateAttributes.fxml"));
            Parent newRoot = fxmlLoader.load();
            Stage stage = new Stage();
            AttributesLinker controller = fxmlLoader.getController();
            controller.setTheme(mainController.getTheme());
            initializeWindow(newRoot, stage, totalAttributes, controller);
            columnsAttributes = controller.getNames();
            graphController.setColumnsAttributes(columnsAttributes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeWindow(Parent newRoot, Stage stage, int totalAttributes, AttributesLinker controller) {
        stage.setTitle("Associate attributes");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnCloseRequest(Event::consume);
        stage.setScene(new Scene(newRoot));
        Image icon = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/ML.png")).toString());
        stage.getIcons().add(icon);
        controller.addLabels(totalAttributes);
        controller.getAnchor().getStylesheets().add(mainController.getTheme());
        stage.setResizable(false);
        stage.showAndWait();
    }

    private void associateAttributesColumns(String line) throws IOException {
        String[] split = line.split(",");
        ArrayList<String> array = new ArrayList<>(Arrays.asList(split));
        columnsAttributes = array;
        graphController.setColumnsAttributes(array);
        if (linkController.getColumnX() == null && linkController.getColumnY() == null)
            openWindowChoosePosition();
        if (linkController.getColumnX() != null && linkController.getColumnY() != null)
            this.positionAssigned = true;

    }

    /**
     * For each line of a file adds data to the charts
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void addLineDataToSeries(String line) {
        chartController.addLineDataToSeries(line, chartController.getIndexOfAttributes() + 1);
    }

    /**
     * Creates the series of charts corresponding to nodes of a static graph
     *
     * @param line a string of a time instant with all info about nodes
     */
    private void createSeriesFromStaticGraph(String line) {
        chartController.createSeriesFromStaticGraph(line, chartController.getIndexOfAttributes() + 1);
    }

    /**
     * Gets attributes of nodes of a dynamic graph from a file
     *
     * @param br bufferedReader
     */
    private void getDynamicAttributesFromCsv(BufferedReader br) throws IOException {
        graphController.setGraphList(graphList);
        String line = br.readLine();
        if (line.contains("time")) {
            associateAttributesColumns(line);
            line = br.readLine();
        } else if (this.columnsAttributes.isEmpty())
            openAssociateAttributesWindow(line);
        linkAttributes(br, line);
        chartController.loadAttributesList(columnsAttributes);
        graphList = graphController.getGraphList();
    }

    private void linkAttributes(BufferedReader br, String line) throws IOException {
        if (linkController.getColumnX() == null && linkController.getColumnY() == null)
            do
                createNodesVector(line, false);
            while (((line = br.readLine()) != null));
        else
            do
                createNodesVector(line, true);
            while (((line = br.readLine()) != null));
    }

    private void resetCharts() {
        chartController.resetCharts();
    }

    /**
     * Create a graph from a file
     *
     * @param file file to read
     */
    private void createGraphFromFile(File file) {
        try {
            graphController.setGraphList(graphList);
            graphVisualization = graphController.createGraphFromFile(file);
            graphList = graphController.getGraphList();
            for (TimeGraph g : graphList)
                g.getGraph().setAttribute("ui.stylesheet", "url('" + this.theme + "')");
            createGraph();
        } catch (Exception e) {
            DialogBuilder dialogBuilder = new DialogBuilder(mainController.getTheme());
            dialogBuilder.error("Failed to generate graph.");
        }
    }

    /**
     * Creates a static or dynamic graph
     */
    private void createGraph() {
        if (graphVisualization.equals(GraphType.STATIC)) {
            slider.setDisable(true);
            showStaticGraph(graphController.getStaticGraph());
        } else if (graphVisualization.equals(GraphType.DYNAMIC)) {
            createViews();
            createTimeSlider();
            changeGraphView(String.valueOf(graphList.get(0).getTime()));
        }
    }

    /**
     * Shows the static graph
     *
     * @param staticGraph static Graph
     */
    private void showStaticGraph(Graph staticGraph) {
        if (staticGraph.hasAttribute("ui.stylesheet"))
            staticGraph.removeAttribute("ui.stylesheet");
        staticGraph.setAttribute("ui.stylesheet", "url('" + this.theme + "')");
        this.currentGraph = staticGraph;
        FxViewer v = new FxViewer(staticGraph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        v.addDefaultView(false, new FxGraphRenderer());
        if (this.positionAssigned)
            v.disableAutoLayout();
        else v.enableAutoLayout();
        setSceneProperties((FxViewPanel) v.getDefaultView());
        SimpleMouseManager sm = new SimpleMouseManager(staticGraph, chartController);
        sm.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("LabelProperty")) {
                infoNode.setText(evt.getNewValue().toString());
                labelPane.setPrefHeight(0);
            }
        });
        v.getDefaultView().setMouseManager(sm);
    }

    /**
     * Sets the scene's properties
     *
     * @param panel fxViewPanel
     */
    private void setSceneProperties(FxViewPanel panel) {
        scene.setRoot(panel);
        borderPane.setCenter(scene);
        scene.setVisible(true);
        scene.heightProperty().bind(borderPane.heightProperty());
        scene.widthProperty().bind(borderPane.widthProperty());
    }

    private void createTimeSlider() {
        setSlider();
        slider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                int index = n.intValue();
                return String.valueOf(time.get(index));
            }

            @Override
            public Double fromString(String string) {
                return Double.parseDouble(string);
            }
        });
        addListenersToSlider();
    }

    /**
     * Returns the nearest element of a double in an arraylist
     *
     * @param time  arraylist of double
     * @param index double to compare
     *
     * @return the nearest number in the arraylist
     */
    public double nearest(ArrayList<Double> time, double index) {
        if (!time.isEmpty()) {
            double nearest = time.get(0);
            double current = Double.MAX_VALUE;
            for (Double d : time) {
                if (Math.abs(d - index) <= current) {
                    nearest = d;
                    current = Math.abs(d - index);
                }
            }
            return nearest;
        } else return 0;
    }

    /**
     * Sets values of slider
     */
    private void setSlider() {
        slider.setDisable(false);
        time.clear();
        for (TimeGraph t : graphList)
            time.add(t.getTime());
        slider.setMin(time.get(0));
        slider.setMax(time.get(time.size() - 1));
        slider.setValue(time.get(0));
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        toolTipSlider();
    }

    /**
     * Initialize tooltip for slider animation
     */
    private void toolTipSlider() {
        Tooltip t = new Tooltip("Press space bar to start/stop slider animation");
        t.setShowDelay(Duration.seconds(0));
        Tooltip.install(slider, t);
    }

    /**
     * Loads listener on spaceBar pressed
     */
    private void loadPlaySpaceBar() {
        runnable = new RunnableSlider(slider);
        AtomicBoolean pressed = new AtomicBoolean(false);
        Runnable run = () -> Platform.runLater(runnable);
        mainController.getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (!pressed.get()) {
                    runnable.restart();
                    Thread thread = new Thread(run);
                    thread.start();
                    pressed.set(true);
                } else {
                    runnable.shutdown();
                    pressed.set(false);
                }
            }
        });
    }

    /**
     * Listener for the slider. Changes view of graph with the selected time of slider
     */
    private void addListenersToSlider() {
        slider.applyCss();
        slider.layout();
        Pane thumb = (Pane) slider.lookup(".thumb");
        if (!thumb.getChildren().contains(label)) {
            thumb.getChildren().add(label);
            label.setTextAlignment(TextAlignment.CENTER);
            thumb.setPrefHeight(20);
        }
        slider.valueProperty().addListener(sliderListener);
        slider.setOnMousePressed(event -> borderPane.getScene().setCursor(Cursor.CLOSED_HAND));
        slider.setOnMouseReleased(event -> borderPane.getScene().setCursor(Cursor.DEFAULT));
    }


    /**
     * Changes visualization of a dynamic graph in time
     *
     * @param time instant chosen
     */
    private void changeGraphView(String time) {
        Optional<TimeGraph> g = graphList.stream().filter(timeGraph -> timeGraph.getTime() == Double.parseDouble(time)).findFirst();
        g.ifPresent(timeGraph -> changeView(timeGraph.getGraph(), Double.parseDouble(time)));
    }

    /**
     * @return current graph displayed
     */
    public Graph getCurrentGraph() {
        return currentGraph;
    }

    /**
     * Creates a viewer for each graph
     */
    private void createViews() {
        for (TimeGraph t : graphList) {
            FxViewer viewer = new FxViewer(t.getGraph(), FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
            viewer.addView(String.valueOf(t.getTime()), new FxGraphRenderer());
            viewers.add(viewer);
        }
    }

    /**
     * Change viewer in order to display a different graph
     *
     * @param graph graph to show
     * @param time  instant related to the graph
     */
    private void changeView(Graph graph, Double time) {
        setGraphAttribute(graph, time);
        Optional<FxViewer> fv = viewers.stream().filter(fxViewer -> fxViewer.getView(String.valueOf(time)) != null).findFirst();
        if (fv.isPresent()) {
            FxViewer v = fv.get();
            if (positionAssigned)
                v.disableAutoLayout();
            else v.enableAutoLayout();
            setSceneProperties((FxViewPanel) v.getView(String.valueOf(time)));
            SimpleMouseManager sm = new SimpleMouseManager(graph, time, chartController, this);
            sm.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("LabelProperty")) {
                    infoNode.setText(evt.getNewValue().toString());
                    if (evt.getNewValue().toString().equals(" "))
                        labelPane.setPrefHeight(0);
                    else labelPane.setPrefHeight(35);
                }
            });
            v.getView(String.valueOf(time)).setMouseManager(sm);
        }
    }

    /**
     * Sets attributes to the graph displayed
     *
     * @param graph graph
     * @param time  time instant
     */
    private void setGraphAttribute(Graph graph, Double time) {
        Optional<TimeGraph> g = graphList.stream().filter(timeGraph -> timeGraph.getTime() == time).findFirst();
        g.ifPresent(timeGraph -> currentGraph = g.get().getGraph());
        if (graph.hasAttribute("ui.stylesheet"))
            graph.removeAttribute("ui.stylesheet");
        graph.setAttribute("ui.stylesheet", "url('" + this.theme + "')");
    }

    @FXML
    private void deselectFiltersTable() {
        filtersComponentController.getTableFilters().getSelectionModel().clearSelection();
    }

    @FXML
    private void deselectNodeTable() {
        nodeTableComponentController.nodesTable.getSelectionModel().clearSelection();
    }

    /**
     * Opens a window to choose which parameters use as positions
     */
    private void openWindowChoosePosition() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClassLoader.getSystemClassLoader().getResource("fxml/choosePositions.fxml"));
        fxmlLoader.setController(linkController);
        Parent root = fxmlLoader.load();
        linkController.setAttributes(columnsAttributes);
        linkController.setTheme(mainController.getTheme());
        linkController.addColumnsToMenuButtons();
        Stage stage = new Stage();
        linkController.setStage(stage);
        stage.setScene(new Scene(root));
        stage.setTitle("Choose positions");
        Image icon = new Image(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("images/ML.png")).toString());
        stage.getIcons().add(icon);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getScene().getStylesheets().add(mainController.getTheme());
        stage.setResizable(false);
        stage.showAndWait();
    }

    /**
     * Changes parameters to use as positions
     */
    @FXML
    private void reloadPositions() {
        try {
            openWindowChoosePosition();
            if (csv != null && linkController.getColumnX() != null && linkController.getColumnY() != null) {
                switch (graphVisualization) {
                    case STATIC:
                        reloadStaticPositions();
                        break;
                    case DYNAMIC:
                        reloadDynamicPositions();
                        break;
                }
            }
        } catch (IOException e) {
            DialogBuilder d = new DialogBuilder(mainController.getTheme());
            d.error("Failed link positions");
            e.printStackTrace();
        }
    }

    /**
     * Reloads positions of a dynamic graph
     */
    public void reloadDynamicPositions() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csv));
        String line = br.readLine();
        if (line != null) {
            this.positionAssigned = true;
            if (line.contains("time")) {
                line = br.readLine();
            }
            do
                createNodesVector(line, true);
            while (((line = br.readLine()) != null));
        }
    }

    /**
     * Reloads positions of a static graph
     */
    public void reloadStaticPositions() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csv));
        String line = br.readLine();
        if (line != null) {
            this.positionAssigned = true;
            if (line.contains("time")) {
                line = br.readLine();
            }
            createPositions(line);
        }
    }
}