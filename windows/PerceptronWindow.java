package windows;

import utils.DataSet;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;

public class PerceptronWindow extends JFrame {

    private ArrayList<Point> points;
    private PerceptronThread.Model model;

    private final double[] defaultWeights = { 0.0, 0.0, 0.0 };

    private final int MAP_WIDTH = 500;
    private final int MAP_HEIGHT = 500;
    private final int RADIUS_POINT = 5;
    private final double MAP_SCALE = 5.0;

    private final Color LEFTCLICK_COLOR = Color.blue;
    private final Color RIGHTCLICK_COLOR = Color.green;

    private double[] weights;

    private Map map;

    private JLabel lblEpochResult;
    private JLabel lblWeightResult0;
    private JLabel lblWeightResult1;
    private JLabel lblWeightResult2;

    private JTextField txtWeight0;
    private JTextField txtWeight1;
    private JTextField txtWeight2;

    private JTextField txtLearningRate;
    private JTextField txtEpochs;

    private JButton btnRandomWeights;
    private JButton btnPerceptron;

    private boolean clickEnable;
    private boolean modelEnable;
    private boolean changeWeights;
    private boolean addInstanceEnable;

    private JMenu jmOptions;
    private JMenu jmPredict;
    private JRadioButtonMenuItem jmiPredict;

    private final String unicodeSubscript0 = "\u2080";
    private final String unicodeSubscript1 = "\u2081";
    private final String unicodeSubscript2 = "\u2082";

    public PerceptronWindow()
    {
        super("Ejemplo de funcionamiento del perceptron");
        setLayout(null);
        setSize(900,625);
        setLocationRelativeTo(null);

        // Inicializamos la lista que contiene los puntos del mapa
        points = new ArrayList<>();
        // Inicializamos los pesos con lo que vamos a trabajar
        weights = new double[3];
        System.arraycopy(weights, 0, defaultWeights, 0, weights.length);

        // Barra de menu
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        // Un menu de la barra
        jmOptions = new JMenu("Opciones");
        menuBar.add(jmOptions);
        // Opciones del menu
        // ELiminar ultima
        JMenuItem jmiDeleteLastInstance = new JMenuItem("Eliminar la ultima instancia");
        jmiDeleteLastInstance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !points.isEmpty() ) {
                    int idxPoint = -1;
                    for ( int i = points.size() - 1; i >= 0; i-- ) {
                        if ( !points.get(i).sweep ) {
                            idxPoint = i;
                            break;
                        }
                    }
                    if ( idxPoint != -1 ) {
                        points.remove(idxPoint);
                        map.repaint();
                    }
                }
            }
        });
        jmOptions.add(jmiDeleteLastInstance);
        // Limpiar instancias
        JMenuItem jmiClearInstances = new JMenuItem("Limpiar instancias");
        jmiClearInstances.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Point> newPoints = new ArrayList<>();
                for ( Point point : points ) {
                    if ( point.sweep ) {
                        newPoints.add(point);
                    }
                }
                points = newPoints;
                map.repaint();
            }
        });
        jmOptions.add(jmiClearInstances);
        // Limpiar el programa
        JMenuItem jmiClearAll = new JMenuItem("Limpiar todo");
        jmiClearAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                points.clear();
                System.arraycopy(defaultWeights, 0, weights, 0, weights.length);
                map.repaint();
                lblWeightResult0.setText("<html>w" + unicodeSubscript0 + " = <b>" + weights[0] + "</b></html>");
                lblWeightResult1.setText("<html>w" + unicodeSubscript1 + " = <b>" + weights[1] + "</b></html>");
                lblWeightResult2.setText("<html>w" + unicodeSubscript2 + " = <b>" + weights[2] + "</b></html>");
                txtWeight0.setText(String.valueOf(weights[0]));
                txtWeight1.setText(String.valueOf(weights[1]));
                txtWeight2.setText(String.valueOf(weights[2]));
                txtLearningRate.setText("0.");
                txtEpochs.setText("");
                lblEpochResult.setText("<html>Epoca: <b>0</b></html>");
                addInstanceEnable = true;
                jmPredict.setVisible(false);
            }
        });
        jmOptions.add(jmiClearAll);
        // Salir
        JMenuItem jmiClose = new JMenuItem("Salir");
        jmiClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PerceptronWindow.this.dispose();
            }
        });
        jmOptions.add(jmiClose);
        // Opciones para predecir
        jmPredict = new JMenu("Modelo");
        menuBar.add(jmPredict);
        ButtonGroup bgPredict = new ButtonGroup();
        // Predecir una instancia
        jmiPredict = new JRadioButtonMenuItem("Predecir", true);
        bgPredict.add(jmiPredict);
        jmPredict.add(jmiPredict);
        jmiPredict.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !changeWeights ) {
                    modelEnable = true;
                    addInstanceEnable = false;
                }
            }
        });

        // Agregar una nueva instancia
        JRadioButtonMenuItem jmiNewInstance = new JRadioButtonMenuItem("Nueva instancia");
        bgPredict.add(jmiNewInstance);
        jmPredict.add(jmiNewInstance);
        jmPredict.setVisible(false);
        jmiNewInstance.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addInstanceEnable = true;
            }
        });

        // Mostrar barrido
        JMenuItem jmiShowSweep = new JMenuItem("Mostrar barrido");
        jmPredict.add(jmiShowSweep);
        jmiShowSweep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !changeWeights ) {
                    for ( int i = 0; i <= map.getWidth(); i+= RADIUS_POINT ) {
                        for ( int j = 0; j <= map.getHeight(); j+= RADIUS_POINT ) {
                            Point point = new Point();
                            point.xMap = i;
                            point.yMap = j;
                            point.x = ( i >= MAP_WIDTH * 0.5 ) ? i - ( MAP_WIDTH * 0.5 ) : -((MAP_WIDTH * 0.5) - i);
                            point.x /= (MAP_WIDTH * 0.5) / MAP_SCALE;
                            point.y = ( j > MAP_HEIGHT * 0.5 ) ? -(j - (MAP_HEIGHT * 0.5)) : (MAP_HEIGHT * 0.5) - j;
                            point.y /= (MAP_HEIGHT * 0.5) / MAP_SCALE;
                            Object[] instance = new Object[2];
                            instance[0] = point.x;
                            instance[1] = point.y;
                            double result = 0;
                            try {
                                result = model.predict(instance);
                                point.leftClick = result == 0.0;
                                point.sweep = true;
                                points.add(point);
                                repaint();
                            } catch (Exception ex) {
                                System.out.println("No se pudo realizar el barrido");
                                break;
                            }
                        }
                    }
                }
            }
        });

        // Eliminar barrido
        JMenuItem jmiHideSweep = new JMenuItem("Ocultar barrido");
        jmPredict.add(jmiHideSweep);
        jmiHideSweep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( !changeWeights ) {
                    ArrayList<Point> newPoints = new ArrayList<>();
                    for ( Point point : points ) {
                        if ( !point.sweep ) {
                            newPoints.add(point);
                        }
                    }
                    points = newPoints;
                    map.repaint();
                }
            }
        });

        // Lienzo princiapal de la ventana
        map = new Map();
        map.setSize(MAP_WIDTH, MAP_HEIGHT);
        map.setLocation(35,30);
        map.setBackground(Color.WHITE);
        add(map);

        // Eventos del mouse
        clickEnable = true;
        modelEnable = false;
        addInstanceEnable = true;
        map.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = new Point();
                // Tratamiento de los datos
                point.xMap = e.getX();
                point.yMap = e.getY();
                point.x = ( e.getX() >= MAP_WIDTH * 0.5 ) ? e.getX() - ( MAP_WIDTH * 0.5 ) : -((MAP_WIDTH * 0.5) - e.getX());
                point.x /= (MAP_WIDTH * 0.5) / MAP_SCALE;
                point.y = ( e.getY() > MAP_HEIGHT * 0.5 ) ? -(e.getY() - (MAP_HEIGHT * 0.5)) : (MAP_HEIGHT * 0.5) - e.getY();
                point.y /= (MAP_HEIGHT * 0.5) / MAP_SCALE;
                // Prediccion de la nueva instancia;
                if ( !addInstanceEnable && modelEnable && e.getButton() == MouseEvent.BUTTON1 ) {
                    Object[] instance = new Object[2];
                    instance[0] = point.x;
                    instance[1] = point.y;
                    try {
                        double result = model.predict(instance);
                        JOptionPane.showMessageDialog(null, "La nueva instancia es: " + ((result == 0.0) ? "circulo azul" : "cuadrado verde"), "Resultado", JOptionPane.INFORMATION_MESSAGE);
                        point.leftClick = result == 0.0;
                        point.sweep = false;
                        points.add(point);
                        repaint();
                    } catch (Exception ex) {
                        System.out.println("No se pudo predecir la instancia");
                    }
                    return;
                }
                // Validacion para agregar un nuevo elemento
                if ( (e.getButton() != MouseEvent.BUTTON1 && e.getButton() != MouseEvent.BUTTON3) || !clickEnable || !addInstanceEnable ) {
                    return;
                }
                point.sweep = false;
                // Boton izquierdo
                if ( e.getButton() == MouseEvent.BUTTON1 ) {
                    point.leftClick = true;
                    points.add(point);
                }
                // Boton derecho
                if ( e.getButton() == MouseEvent.BUTTON3 ) {
                    point.leftClick = false;
                    points.add(point);
                }
                System.out.println("Nuevo punto agregado: " + point);
                jmPredict.setVisible(false);
                if ( modelEnable ) {
                    modelEnable = false;
                    ArrayList<Point> newPoints = new ArrayList<>();
                    for ( Point tmpPoint : points ) {
                        if ( !tmpPoint.sweep ) {
                            newPoints.add(tmpPoint);
                        }
                    }
                    points = newPoints;
                }
                map.repaint();
            }
        });

        /** Titulos, leyendas */
        // Escala del norte del plano
        JLabel lblScaleNorth = new JLabel("+ " + MAP_SCALE);
        lblScaleNorth.setSize(28,10);
        lblScaleNorth.setLocation(map.getX() + ( map.getWidth() / 2 ) - 12, map.getY() - 15);
        add(lblScaleNorth);
        // Escala del sur del plano
        JLabel lblScaleSouth = new JLabel("- " + MAP_SCALE);
        lblScaleSouth.setSize(28,10);
        lblScaleSouth.setLocation(map.getX() + ( map.getWidth() / 2 ) - 12, map.getY() + map.getHeight() + 5);
        add(lblScaleSouth);
        // Escala del este del plano
        JLabel lblScaleEast = new JLabel("+ " + MAP_SCALE);
        lblScaleEast.setSize(28,10);
        lblScaleEast.setLocation(map.getX() + ( map.getWidth() ) + 5, map.getY() + (map.getHeight() / 2) - 5);
        add(lblScaleEast);
        // Escala del este del plano
        JLabel lblScaleWest = new JLabel("- " + MAP_SCALE);
        lblScaleWest.setSize(28,10);
        lblScaleWest.setLocation(map.getX() - 27, map.getY() + (map.getHeight() / 2) - 5);
        add(lblScaleWest);

        // Subtitulo de la ventana
        JLabel lblSubtitle = new JLabel("Configuracion de los parametros");
        lblSubtitle.setLocation(map.getX() + map.getWidth() + 40, map.getY());
        lblSubtitle.setSize(getWidth() - (map.getX() + map.getWidth() + 75), 20);
        lblSubtitle.setHorizontalAlignment(JLabel.CENTER);
        lblSubtitle.setFont(new Font("Dialog", Font.BOLD, 16));
        add(lblSubtitle);

        // Factor de aprendizaje
        JLabel lblLearningRate = new JLabel("Learning rate: ");
        lblLearningRate.setLocation(lblSubtitle.getX(), lblSubtitle.getY() + lblSubtitle.getHeight() + 20);
        lblLearningRate.setSize((int) (lblSubtitle.getWidth() * 0.39), 40);
        lblLearningRate.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(lblLearningRate);
        txtLearningRate = new JTextField("0.");
        txtLearningRate.setLocation(lblLearningRate.getX() + lblLearningRate.getWidth() + (int) (lblSubtitle.getWidth() * 0.02), lblLearningRate.getY());
        txtLearningRate.setSize((int) (lblSubtitle.getWidth() * 0.59), 40);
        txtLearningRate.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if ( e.getKeyChar() < '0' || e.getKeyChar() > '9' || txtLearningRate.getCaretPosition() < 2 ) {
                    e.consume();
                }
                super.keyTyped(e);
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if ( txtLearningRate.getText().length() == 2 && e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
                    e.consume();
                }
                super.keyPressed(e);
            }
        });
        add(txtLearningRate);

        // Epocas
        JLabel lblEpochs = new JLabel("Epocas: ");
        lblEpochs.setSize(lblLearningRate.getSize());
        lblEpochs.setLocation(lblLearningRate.getX(), lblLearningRate.getY() + lblLearningRate.getHeight() + 20);
        lblEpochs.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(lblEpochs);
        txtEpochs = new JTextField();
        txtEpochs.setSize(txtLearningRate.getSize());
        txtEpochs.setLocation(txtLearningRate.getX(), lblEpochs.getY());
        txtEpochs.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if ( e.getKeyChar() < '0' || e.getKeyChar() > '9' ) {
                    e.consume();
                }
                super.keyTyped(e);
            }
        });
        add(txtEpochs);

        /** Pesos */
        // Peso 0
        JLabel lblWeigth0 = new JLabel("w" + unicodeSubscript0);
        lblWeigth0.setLocation(lblLearningRate.getX(), txtEpochs.getY() + txtEpochs.getHeight() + 20);
        lblWeigth0.setSize((lblSubtitle.getWidth() / 3) - 15, 25);
        lblWeigth0.setHorizontalAlignment(JLabel.CENTER);
        lblWeigth0.setFont(new Font("Dialog", Font.BOLD, 20));
        add(lblWeigth0);
        txtWeight0 = new JTextField();
        txtWeight0.setLocation(lblSubtitle.getX(), lblWeigth0.getY() + lblWeigth0.getHeight() + 5);
        txtWeight0.setSize((lblSubtitle.getWidth() / 3) - 15,40);
        txtWeight0.setText(String.valueOf(weights[0]));
        add(txtWeight0);
        // Peso 1
        JLabel lblWeigth1 = new JLabel("w" + unicodeSubscript1);
        lblWeigth1.setLocation(lblSubtitle.getX() + (lblSubtitle.getWidth() / 2) - (txtWeight0.getWidth() / 2), lblWeigth0.getY());
        lblWeigth1.setSize(lblWeigth0.getSize());
        lblWeigth1.setHorizontalAlignment(JLabel.CENTER);
        lblWeigth1.setFont(new Font("Dialog", Font.BOLD, 20));
        add(lblWeigth1);
        txtWeight1 = new JTextField();
        txtWeight1.setLocation(lblWeigth1.getX(), txtWeight0.getY());
        txtWeight1.setSize(txtWeight0.getSize());
        txtWeight1.setText(String.valueOf(weights[1]));
        add(txtWeight1);
        // Peso 2
        txtWeight2 = new JTextField();
        txtWeight2.setLocation(lblSubtitle.getX() + lblSubtitle.getWidth() - (lblSubtitle.getWidth() / 3) + 15, txtWeight1.getY());
        txtWeight2.setSize(txtWeight0.getWidth(),txtWeight0.getHeight());
        txtWeight2.setText(String.valueOf(weights[2]));
        add(txtWeight2);
        JLabel lblWeigth2 = new JLabel("w" + unicodeSubscript2);
        lblWeigth2.setLocation(txtWeight2.getX(), txtWeight1.getY() - 30);
        lblWeigth2.setSize(txtWeight2.getWidth(), 25);
        lblWeigth2.setHorizontalAlignment(JLabel.CENTER);
        lblWeigth2.setFont(new Font("Dialog", Font.BOLD, 20));
        add(lblWeigth2);

        txtWeight0.addKeyListener(new CustomKeyListener(txtWeight0));
        txtWeight1.addKeyListener(new CustomKeyListener(txtWeight1));
        txtWeight2.addKeyListener(new CustomKeyListener(txtWeight2));

        txtWeight0.addCaretListener(new CustomCaretListener(txtWeight0, map, 0));
        txtWeight1.addCaretListener(new CustomCaretListener(txtWeight1, map, 1));
        txtWeight2.addCaretListener(new CustomCaretListener(txtWeight2, map, 2));

        txtWeight0.addFocusListener(new CustomFocusListener(txtWeight0, 0));
        txtWeight1.addFocusListener(new CustomFocusListener(txtWeight1, 1));
        txtWeight2.addFocusListener(new CustomFocusListener(txtWeight2, 2));

        // Boton de pesos aleatorios
        btnRandomWeights = new JButton("Pesos aleatorios");
        btnRandomWeights.setLocation(lblSubtitle.getX(), txtWeight0.getY() + txtWeight0.getHeight() + 20);
        btnRandomWeights.setSize(lblSubtitle.getWidth(), 50);
        add(btnRandomWeights);
        btnRandomWeights.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                weights[0] = getRandom();
                weights[1] = getRandom();
                weights[2] = getRandom();
                txtWeight0.setText(String.valueOf(weights[0]));
                txtWeight1.setText(String.valueOf(weights[1]));
                txtWeight2.setText(String.valueOf(weights[2]));
                map.repaint();
            }
        });

        // Boton para empezar el algoritmo
        btnPerceptron = new JButton("Perceptron");
        btnPerceptron.setSize(getWidth() - (map.getX() + map.getWidth() + 75),50);
        btnPerceptron.setLocation(map.getX() + map.getWidth() + 40, btnRandomWeights.getY() + btnRandomWeights.getHeight() + 20);
        btnPerceptron.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double learningRate;
                int epochs;
                try {
                    learningRate = Double.parseDouble(txtLearningRate.getText());
                    epochs = Integer.parseInt(txtEpochs.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Parametros del modelo no especificados", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ( epochs <= 0 ) {
                    JOptionPane.showMessageDialog(null, "Las epocas no pueden ser 0 o menos", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if ( points.size() < 1 ) {
                    JOptionPane.showMessageDialog(null, "Ingrese por minimo una instancia", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Deshabilitamos la interfaz temporalmente y otras cosas
                changeUIForPerceptron(false);
                clickEnable = false;
                // Eliminamos los puntos de barrido
                ArrayList<Point> newPoints = new ArrayList<>();
                for ( Point tmpPoint : points ) {
                    if ( !tmpPoint.sweep ) {
                        newPoints.add(tmpPoint);
                    }
                }
                points = newPoints;
                map.repaint();
                // Creacion del conjunto de datos
                String[] headers = { "x_1", "x_2", "y" };
                String[] attributeTypes = { DataSet.NUMERIC_TYPE, DataSet.NUMERIC_TYPE, DataSet.NUMERIC_TYPE };
                DataSet dataSet;
                try {
                    dataSet = DataSet.getEmptyDataSetWithHeaders(headers, attributeTypes, "y");
                } catch (Exception ex) {
                    System.out.println("El dataset no pudo ser creado");
                    return;
                }
                for ( Point point : points ) {
                    try {
                        dataSet.addInstance(new ArrayList<>(Arrays.asList("" + point.x,"" + point.y, "" + ((point.leftClick) ? 0 : 1))));
                    } catch (Exception ex) {
                        System.out.println("No se pudo agregar la instancia del punto " + point);
                    }
                }
                System.out.println("Conjunto de datos con el que el algoritmo trabajara");
                System.out.println(dataSet);
                // Generamos los parametros y modelo del perceptron
                PerceptronThread.Params params = new PerceptronThread.Params();
                params.setEpochs(epochs);
                params.setLearningRate(learningRate);
                params.setWeights(weights);
                try {
                    PerceptronThread perceptronThread = new PerceptronThread();
                    perceptronThread.makeModel(dataSet, params, PerceptronWindow.this);
                } catch (Exception ex) {
                    System.out.println("El modelo no se pudo generar");
                }
            }
        });
        add(btnPerceptron);

        /** Resultados */
        // Titulo
        JLabel lblResults = new JLabel("Resultados");
        lblResults.setLocation(btnRandomWeights.getX(), btnPerceptron.getY() + btnPerceptron.getHeight() + 20);
        lblResults.setSize(lblSubtitle.getWidth(), 18);
        lblResults.setHorizontalAlignment(JLabel.CENTER);
        lblResults.setFont(new Font("Dialog", Font.BOLD, 14));
        add(lblResults);

        // Epoca
        lblEpochResult = new JLabel("<html>Epoca: <b>0</b></html>");
        lblEpochResult.setLocation(btnRandomWeights.getX(), lblResults.getY() + lblResults.getHeight() + 5);
        lblEpochResult.setSize(lblSubtitle.getWidth(), 18);
        lblEpochResult.setHorizontalAlignment(JLabel.LEFT);
        lblEpochResult.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(lblEpochResult);

        // Peso 0
        lblWeightResult0 = new JLabel("<html>w" + unicodeSubscript0 + " = <b>0.0</b></html>");
        lblWeightResult0.setLocation(btnRandomWeights.getX(), lblEpochResult.getY() + lblEpochResult.getHeight() + 5);
        lblWeightResult0.setSize(lblSubtitle.getWidth(), 18);
        lblWeightResult0.setHorizontalAlignment(JLabel.LEFT);
        lblWeightResult0.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(lblWeightResult0);
        // Peso 1
        lblWeightResult1 = new JLabel("<html>w" + unicodeSubscript1 + " = <b>0.0</b></html>");
        lblWeightResult1.setLocation(btnRandomWeights.getX(), lblWeightResult0.getY() + lblWeightResult0.getHeight() + 5);
        lblWeightResult1.setSize(lblSubtitle.getWidth(), 18);
        lblWeightResult1.setHorizontalAlignment(JLabel.LEFT);
        lblWeightResult1.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(lblWeightResult1);
        // Peso 2
        lblWeightResult2 = new JLabel("<html>w" + unicodeSubscript2 + " = <b>0.0</b></html>");
        lblWeightResult2.setLocation(btnRandomWeights.getX(), lblWeightResult1.getY() + lblWeightResult1.getHeight() + 5);
        lblWeightResult2.setSize(lblSubtitle.getWidth(), 18);
        lblWeightResult2.setHorizontalAlignment(JLabel.LEFT);
        lblWeightResult2.setFont(new Font("Dialog", Font.PLAIN, 14));
        add(lblWeightResult2);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void changeUIForPerceptron(boolean enable) {
        btnPerceptron.setEnabled(enable);
        txtWeight0.setEditable(enable);
        txtWeight1.setEditable(enable);
        txtWeight2.setEditable(enable);
        txtLearningRate.setEditable(enable);
        txtEpochs.setEditable(enable);
        btnRandomWeights.setEnabled(enable);
        jmOptions.setEnabled(enable);
    }

    private double getRandom() {
        int random = (int) (Math.random() * (100 * (int) MAP_SCALE));
        int sign = (int) (Math.random() * 10);
        return (double) ((sign % 2 == 0) ? -random : random) / 100;
    }

    public void updateEpoch(int epoch, boolean done, boolean stop) {
        if ( stop ) {
            if ( done ) {
                lblEpochResult.setText("<html>Convergio en la epoca: <b>" + epoch + "</b></html>");
            } else {
                lblEpochResult.setText("<html>No convergio. Epocas: <b>" + epoch + "</b></html>");
            }
        } else {
            lblEpochResult.setText("<html>Epoca: <b>" + epoch + "</b></html>");
        }
    }

    public void updateWeights(double[] weights) {
        this.weights = weights;
        lblWeightResult0.setText("<html>w" + unicodeSubscript0 + " = <b>" + weights[0] + "</b></html>");
        lblWeightResult1.setText("<html>w" + unicodeSubscript1 + " = <b>" + weights[1] + "</b></html>");
        lblWeightResult2.setText("<html>w" + unicodeSubscript2 + " = <b>" + weights[2] + "</b></html>");
        map.repaint();
    }

    public void setModel(PerceptronThread.Model model) {
        this.model = model;
        txtWeight0.setText(String.valueOf(weights[0]));
        txtWeight1.setText(String.valueOf(weights[1]));
        txtWeight2.setText(String.valueOf(weights[2]));
        System.out.println("Modelo obtenido");
        System.out.println(model);
        changeUIForPerceptron(true);
        clickEnable = true;
        modelEnable = true;
        addInstanceEnable = false;
        changeWeights = false;
        jmPredict.setVisible(true);
        jmiPredict.setSelected(true);
    }

    private class Map extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Obtenemos el alto y ancho del componente
            int width = getWidth();
            int height = getHeight();
            // Linea vertical del lienzo
            g.drawLine(width / 2, 0, width / 2, height);
            // Linea horizontal
            g.drawLine(0, height / 2, width, height / 2);
            // ALgunas líneas más de apoyo
            g.setColor(new Color(170, 183, 184));
            for ( int i = 1; i < (width / MAP_SCALE / 10); i++ ) {
                int point = (int) (i * width / MAP_SCALE / 2);
                if ( point == width / 2 )  {
                    continue;
                }
                g.drawLine(point, 0, point, height);
                g.drawLine(0, point, width, point);
            }
            // Dibujamos los puntos hasta ahora obtenidos
            for ( Point point : points ) {
                if ( point.leftClick ) {
                    g.setColor(LEFTCLICK_COLOR);
                    if ( point.sweep ) {
                        g.drawOval(point.xMap - RADIUS_POINT, point.yMap - RADIUS_POINT, RADIUS_POINT * 2, RADIUS_POINT * 2);
                    } else {
                        g.fillOval(point.xMap - RADIUS_POINT, point.yMap - RADIUS_POINT, RADIUS_POINT * 2, RADIUS_POINT * 2);
                    }
                } else {
                    g.setColor(RIGHTCLICK_COLOR);
                    if ( point.sweep ) {
                        g.drawRect(point.xMap - RADIUS_POINT, point.yMap - RADIUS_POINT, RADIUS_POINT * 2, RADIUS_POINT * 2);
                    } else {
                        g.fillRect(point.xMap - RADIUS_POINT, point.yMap - RADIUS_POINT, RADIUS_POINT * 2, RADIUS_POINT * 2);
                    }
                }
            }
            // Dibujamos la línea del perceptron
            g.setColor(Color.RED);
            double x1_1 = MAP_SCALE + 1.0;
            double x2_1 = Double.NaN;
            double x1_2 = -MAP_SCALE - 1.0;
            double x2_2 = Double.NaN;
            if ( weights[1] == 0.0 && weights[2] != 0.0 || weights[1] != 0.0 && weights[2] != 0.0 ) {
                x2_1 = ( -weights[1] * x1_1 + weights[0] ) / weights[2];
                x2_2 = ( -weights[1] * x1_2 + weights[0] ) / weights[2];
            }
            if ( weights[1] != 0.0 && weights[2] == 0.0 ) {
                x2_1 = ( -weights[2] * x1_1 + weights[0] ) / weights[1];
                x2_2 = ( -weights[2] * x1_2 + weights[0] ) / weights[1];
            }
            // Transformamos el punto a las coordenadas del mapa
            x1_1 = ( x1_1 * (MAP_WIDTH * 0.5) / MAP_SCALE ) + (MAP_WIDTH * 0.5);
            x2_1 *= (MAP_HEIGHT * 0.5) / MAP_SCALE;
            x2_1 = ( x2_1 > 0 ) ? (MAP_HEIGHT * 0.5) - x2_1 : (MAP_HEIGHT * 0.5) + Math.abs(x2_1);
            x1_2 = ( x1_2 * (MAP_WIDTH * 0.5) / MAP_SCALE ) + (MAP_WIDTH * 0.5);
            x2_2 *= (MAP_HEIGHT * 0.5) / MAP_SCALE;
            x2_2 = ( x2_2 > 0 ) ? (MAP_HEIGHT * 0.5) - x2_2 : (MAP_HEIGHT * 0.5) + Math.abs(x2_2);
            Graphics2D graphics2D = (Graphics2D) g;
            graphics2D.setStroke(new BasicStroke(2));
            graphics2D.draw(new Line2D.Double(x1_1,x2_1,x1_2,x2_2));
        }

    }

    private static class Point {

        public int xMap;
        public int yMap;
        public double x;
        public double y;
        public boolean leftClick;
        public boolean sweep;

        @Override
        public String toString() {
            return "Point{" +
                    "xMap=" + xMap +
                    ", yMap=" + yMap +
                    ", x=" + x +
                    ", y=" + y +
                    ", leftClick=" + leftClick +
                    ", sweep=" + sweep +
                    '}';
        }
    }

    private class CustomKeyListener extends KeyAdapter {

        private final JTextField txtField;

        public CustomKeyListener(JTextField txtField)
        {
            this.txtField = txtField;
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if ( (e.getKeyChar() < '0' || e.getKeyChar() > '9') && ( e.getKeyChar() != '.' && e.getKeyChar() != '-' ) ) {
                e.consume();
            }
            if ( e.getKeyChar() == '-' && ( txtField.getCaretPosition() != 0 || txtField.getText().contains("-") ) ) {
                e.consume();
            }
            if ( e.getKeyChar() == '.' && !txtField.getText().isEmpty() && ( txtField.getText().contains(".") ) ) {
                e.consume();
            }
            if ( txtField.getText().startsWith("-") && txtField.getCaretPosition() == 0 ) {
                e.consume();
            }
            super.keyTyped(e);
        }
    }

    private class CustomCaretListener implements CaretListener {

        private final JTextField txtField;
        private final JPanel map;
        private final int idxWeight;

        public CustomCaretListener(JTextField txtField, JPanel map, int idxWeight)
        {
            this.txtField = txtField;
            this.map = map;
            this.idxWeight = idxWeight;
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            try {
                double weigth = Double.parseDouble(txtField.getText());
                if ( weigth != weights[idxWeight] ) {
                    weights[idxWeight] = weigth;
                    changeWeights = true;
                }
            } catch (Exception ex) {
                weights[idxWeight] = 0;
                changeWeights = true;
            } finally {
                if ( modelEnable && changeWeights ) {
                    modelEnable = false;
                    jmPredict.setVisible(false);
                    addInstanceEnable = true;
                    ArrayList<Point> newPoints = new ArrayList<>();
                    for ( Point tmpPoint : points ) {
                        if ( !tmpPoint.sweep ) {
                            newPoints.add(tmpPoint);
                        }
                    }
                    points = newPoints;
                }
                map.repaint();
            }
        }

    }

    private class CustomFocusListener implements FocusListener {

        private final JTextField txtField;
        private final int idxWeight;

        public CustomFocusListener(JTextField txtField, int idxWeight)
        {
            this.txtField = txtField;
            this.idxWeight = idxWeight;
        }

        @Override
        public void focusGained(FocusEvent e) {

        }

        @Override
        public void focusLost(FocusEvent e) {
            if ( txtField.getText().isEmpty() ) {
                txtField.setText(String.valueOf(weights[idxWeight]));
            }
        }
    }

}
