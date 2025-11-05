package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SpineSelectiveLegs extends JPanel implements ActionListener {

    private Fish fish;
    private Target target;
    private final List<Integer> baseSizes = List.of(30, 32, 34, 35, 34, 32, 28, 24, 18, 12);

    private final Timer timer;
    private long lastTime = System.nanoTime();

    private float hue = 200f;
    private float opacity = 1.0f;

    private boolean showTarget = true;


    // SLIDER KONFIGURATION - Enkelt att ändra alla inställningar här
    private static class SliderConfig {
        String name;
        int min, max, defaultValue;
        String unit;

        SliderConfig(String name, int min, int max, int defaultValue, String unit) {
            this.name = name;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
            this.unit = unit;
        }
    }

    // Alla slider-konfigurationer samlade på ett ställe
    private static final SliderConfig[] SLIDER_CONFIGS = {
            new SliderConfig("Segment", 3, 20, 10, "st"),
            new SliderConfig("Storlek", 50, 300, 100, "%"),
            new SliderConfig("Bukmåga", 50, 150, 100, "%"),
            new SliderConfig("Segmentavstånd", 5, 50, 25, "px"),
            new SliderConfig("Tjockast vid", 0, 100, 50, "%"),
            new SliderConfig("Avsmalnande", 10, 300, 100, "%"),
            new SliderConfig("Styvhet", 10, 100, 80, "%"),
            new SliderConfig("Böjlighet", 0, 100, 20, "%"),
            new SliderConfig("Vinkel-begränsning", 0, 90, 45, "°"),
            new SliderConfig("Själv-undvikande", 0, 100, 50, "%"),
            new SliderConfig("Sväng", 10, 1440, 360, "°/s"),
            new SliderConfig("Hastighet", 20, 400, 80, "px/s"),
            new SliderConfig("Målhastighet", 20, 600, 100, "px/s"),
            new SliderConfig("Färg (Hue)", 0, 360, 200, "°"),
            new SliderConfig("Opacitet", 20, 100, 100, "%")
    };

    public SpineSelectiveLegs() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

        fish = new Fish(10, baseSizes, 300, 300);
        target = new Target(400, 300, 80, 50, 100);

        timer = new Timer(16, this);
        timer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                target.position.setLocation(e.getX(), e.getY());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (showTarget) {
            drawTarget(g2d);
        }
        drawCreature(g2d);

    }

    private void drawTarget(Graphics2D g2d) {
        g2d.setColor(new Color(255, 100, 100, 150));
        g2d.fill(new Ellipse2D.Double(target.position.x - 8, target.position.y - 8, 16, 16));
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(target.position.x - 8, target.position.y - 8, 16, 16));

        double currentSpeed = Math.sqrt(target.vx * target.vx + target.vy * target.vy);
        if (currentSpeed > 0.1) {
            double arrowAngle = Math.atan2(target.vy, target.vx);
            int arrowLength = 20;
            int arrowX = (int)(target.position.x + Math.cos(arrowAngle) * arrowLength);
            int arrowY = (int)(target.position.y + Math.sin(arrowAngle) * arrowLength);

            g2d.setColor(new Color(255, 150, 150));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine((int)target.position.x, (int)target.position.y, arrowX, arrowY);
        }
    }

    private void drawCreature(Graphics2D g2d) {
        if (fish.segments.isEmpty()) return;

        List<Point2D.Double> leftPoints = new ArrayList<>();
        List<Point2D.Double> rightPoints = new ArrayList<>();

        for (FishSegment seg : fish.segments) {
            double angle = seg.angle;
            double halfWidth = seg.size / 2.0;

            leftPoints.add(new Point2D.Double(
                    seg.position.x + halfWidth * Math.cos(angle + Math.PI / 2),
                    seg.position.y + halfWidth * Math.sin(angle + Math.PI / 2)
            ));
            rightPoints.add(new Point2D.Double(
                    seg.position.x + halfWidth * Math.cos(angle - Math.PI / 2),
                    seg.position.y + halfWidth * Math.sin(angle - Math.PI / 2)
            ));
        }

        GeneralPath body = new GeneralPath();

        // --- Vänster sida ---
        body.moveTo(leftPoints.get(0).x, leftPoints.get(0).y);
        for (int i = 1; i < leftPoints.size() - 1; i++) {
            Point2D.Double p0 = leftPoints.get(i);
            Point2D.Double p1 = leftPoints.get(i + 1);
            double cx = (p0.x + p1.x) / 2;
            double cy = (p0.y + p1.y) / 2;
            body.quadTo(p0.x, p0.y, cx, cy);
        }

        // --- Höger sida (bakifrån framåt) ---
        int n = rightPoints.size();
        body.lineTo(rightPoints.get(n - 1).x, rightPoints.get(n - 1).y);
        for (int i = n - 2; i > 0; i--) {
            Point2D.Double p0 = rightPoints.get(i);
            Point2D.Double p1 = rightPoints.get(i - 1);
            double cx = (p0.x + p1.x) / 2;
            double cy = (p0.y + p1.y) / 2;
            body.quadTo(p0.x, p0.y, cx, cy);
        }

        body.closePath();

        // --- Färg och fyllning ---
        Color baseColor = Color.getHSBColor(hue / 360f, 0.8f, 1.0f);
        Color darkColor = Color.getHSBColor(hue / 360f, 0.9f, 0.7f);

        int alpha = (int) (opacity * 255);
        Color gradientStart = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
        Color gradientEnd = new Color(darkColor.getRed(), darkColor.getGreen(), darkColor.getBlue(), alpha);

        GradientPaint gradient = new GradientPaint(
                (float) fish.segments.get(0).position.x, (float) fish.segments.get(0).position.y,
                gradientStart,
                (float) fish.segments.get(fish.segments.size() - 1).position.x,
                (float) fish.segments.get(fish.segments.size() - 1).position.y,
                gradientEnd
        );

        g2d.setPaint(gradient);
        g2d.fill(body);

        // --- Kontur ---
        Color outlineColor = new Color(darkColor.getRed(), darkColor.getGreen(), darkColor.getBlue(), alpha);
        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.draw(body);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double deltaTime = (now - lastTime) / 1e9;
        lastTime = now;
        deltaTime = Math.min(deltaTime, 0.1);

        target.update(deltaTime, getWidth(), getHeight());
        target.randomChange();

        fish.moveTowards(target.position, deltaTime);

        repaint();
    }

    private String formatSliderValue(int index, int value) {
        SliderConfig config = SLIDER_CONFIGS[index];
        return config.name + ": " + value + config.unit;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ProcAniCreature");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            SpineSelectiveLegs fishPanel = new SpineSelectiveLegs();
            frame.add(fishPanel, BorderLayout.CENTER);

            JPanel controls = new JPanel();
            controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
            controls.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            controls.setBackground(new Color(40, 40, 40));

            // Lagra alla sliders och labels för reset
            List<JSlider> sliders = new ArrayList<>();
            List<JLabel> labels = new ArrayList<>();

            // 0: Antal segment
            SliderConfig cfg0 = SLIDER_CONFIGS[0];
            JSlider segmentSlider = new JSlider(cfg0.min, cfg0.max, cfg0.defaultValue);
            JLabel segmentLabel = new JLabel(fishPanel.formatSliderValue(0, cfg0.defaultValue));
            segmentSlider.addChangeListener(evt -> {
                fishPanel.fish.updateSegments(segmentSlider.getValue(), fishPanel.baseSizes);
                segmentLabel.setText(fishPanel.formatSliderValue(0, segmentSlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(segmentLabel, segmentSlider));
            sliders.add(segmentSlider);
            labels.add(segmentLabel);

            // 1: Kroppsstorlek
            SliderConfig cfg1 = SLIDER_CONFIGS[1];
            JSlider sizeSlider = new JSlider(cfg1.min, cfg1.max, cfg1.defaultValue);
            JLabel sizeLabel = new JLabel(fishPanel.formatSliderValue(1, cfg1.defaultValue));
            sizeSlider.addChangeListener(evt -> {
                double scale = sizeSlider.getValue() / 100.0;
                fishPanel.fish.scaleSegments(scale, fishPanel.baseSizes);
                sizeLabel.setText(fishPanel.formatSliderValue(1, sizeSlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(sizeLabel, sizeSlider));
            sliders.add(sizeSlider);
            labels.add(sizeLabel);

            // 2: Bukmåga
            SliderConfig cfg2 = SLIDER_CONFIGS[2];
            JSlider bellySlider = new JSlider(cfg2.min, cfg2.max, cfg2.defaultValue);
            JLabel bellyLabel = new JLabel(fishPanel.formatSliderValue(2, cfg2.defaultValue));
            bellySlider.addChangeListener(evt -> {
                fishPanel.fish.bellyScale = bellySlider.getValue() / 100.0;
                fishPanel.fish.updateSegmentSizes();
                bellyLabel.setText(fishPanel.formatSliderValue(2, bellySlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(bellyLabel, bellySlider));
            sliders.add(bellySlider);
            labels.add(bellyLabel);

            // 3: Segmentavstånd
            SliderConfig cfg3 = SLIDER_CONFIGS[3];
            JSlider distanceSlider = new JSlider(cfg3.min, cfg3.max, cfg3.defaultValue);
            JLabel distanceLabel = new JLabel(fishPanel.formatSliderValue(3, cfg3.defaultValue));
            distanceSlider.addChangeListener(evt -> {
                fishPanel.fish.updateSegmentDistance(distanceSlider.getValue());
                distanceLabel.setText(fishPanel.formatSliderValue(3, distanceSlider.getValue()));
            });
            controls.add(createSliderPanel(distanceLabel, distanceSlider));
            sliders.add(distanceSlider);
            labels.add(distanceLabel);

            // 4: Tjockaste delen
            SliderConfig cfg4 = SLIDER_CONFIGS[4];
            JSlider peakSlider = new JSlider(cfg4.min, cfg4.max, cfg4.defaultValue);
            JLabel peakLabel = new JLabel(fishPanel.formatSliderValue(4, cfg4.defaultValue));
            peakSlider.addChangeListener(evt -> {
                double peakPos = peakSlider.getValue() / 100.0;
                fishPanel.fish.updatePeakPosition(peakPos);
                peakLabel.setText(fishPanel.formatSliderValue(4, peakSlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(peakLabel, peakSlider));
            sliders.add(peakSlider);
            labels.add(peakLabel);

            // 5: Kroppsavsmalnande
            SliderConfig cfg5 = SLIDER_CONFIGS[5];
            JSlider taperSlider = new JSlider(cfg5.min, cfg5.max, cfg5.defaultValue);
            JLabel taperLabel = new JLabel(fishPanel.formatSliderValue(5, cfg5.defaultValue));
            taperSlider.addChangeListener(evt -> {
                fishPanel.fish.taperStrength = taperSlider.getValue() / 100.0;
                fishPanel.fish.updateSegmentSizes();
                taperLabel.setText(fishPanel.formatSliderValue(5, taperSlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(taperLabel, taperSlider));
            sliders.add(taperSlider);
            labels.add(taperLabel);

            // 6: Styvhet
            SliderConfig cfg6 = SLIDER_CONFIGS[6];
            JSlider rigiditySlider = new JSlider(cfg6.min, cfg6.max, cfg6.defaultValue);
            JLabel rigidityLabel = new JLabel(fishPanel.formatSliderValue(6, cfg6.defaultValue));
            rigiditySlider.addChangeListener(evt -> {
                fishPanel.fish.rigidity = rigiditySlider.getValue() / 100.0;
                rigidityLabel.setText(fishPanel.formatSliderValue(6, rigiditySlider.getValue()));
            });
            controls.add(createSliderPanel(rigidityLabel, rigiditySlider));
            sliders.add(rigiditySlider);
            labels.add(rigidityLabel);

            // 7: Böjlighet
            SliderConfig cfg7 = SLIDER_CONFIGS[7];
            JSlider flexSlider = new JSlider(cfg7.min, cfg7.max, cfg7.defaultValue);
            JLabel flexLabel = new JLabel(fishPanel.formatSliderValue(7, cfg7.defaultValue));
            flexSlider.addChangeListener(evt -> {
                fishPanel.fish.flexibility = flexSlider.getValue() / 100.0;
                flexLabel.setText(fishPanel.formatSliderValue(7, flexSlider.getValue()));
            });
            controls.add(createSliderPanel(flexLabel, flexSlider));
            sliders.add(flexSlider);
            labels.add(flexLabel);

            // 8: Vinkel-begränsning
            SliderConfig cfg8 = SLIDER_CONFIGS[8];
            JSlider angleSlider = new JSlider(cfg8.min, cfg8.max, cfg8.defaultValue);
            JLabel angleLabel = new JLabel(fishPanel.formatSliderValue(8, cfg8.defaultValue));
            angleSlider.addChangeListener(evt -> {
                fishPanel.fish.maxAngleDiff = Math.toRadians(angleSlider.getValue());
                angleLabel.setText(fishPanel.formatSliderValue(8, angleSlider.getValue()));
            });
            controls.add(createSliderPanel(angleLabel, angleSlider));
            sliders.add(angleSlider);
            labels.add(angleLabel);

            // 9: Själv-undvikande
            SliderConfig cfg9 = SLIDER_CONFIGS[9];
            JSlider avoidSlider = new JSlider(cfg9.min, cfg9.max, cfg9.defaultValue);
            JLabel avoidLabel = new JLabel(fishPanel.formatSliderValue(9, cfg9.defaultValue));
            avoidSlider.addChangeListener(evt -> {
                fishPanel.fish.selfAvoidance = avoidSlider.getValue() / 100.0;
                avoidLabel.setText(fishPanel.formatSliderValue(9, avoidSlider.getValue()));
            });
            controls.add(createSliderPanel(avoidLabel, avoidSlider));
            sliders.add(avoidSlider);
            labels.add(avoidLabel);

            // 10: Sväjghastighet
            SliderConfig cfg10 = SLIDER_CONFIGS[10];
            JSlider turnSlider = new JSlider(cfg10.min, cfg10.max, cfg10.defaultValue);
            JLabel turnLabel = new JLabel(fishPanel.formatSliderValue(10, cfg10.defaultValue));
            turnSlider.addChangeListener(evt -> {
                fishPanel.fish.fishTurnSpeed = Math.toRadians(turnSlider.getValue());
                turnLabel.setText(fishPanel.formatSliderValue(10, turnSlider.getValue()));
            });
            controls.add(createSliderPanel(turnLabel, turnSlider));
            sliders.add(turnSlider);
            labels.add(turnLabel);

            // 13: Färg (Hue)
            SliderConfig cfg13 = SLIDER_CONFIGS[13];
            JSlider hueSlider = new JSlider(cfg13.min, cfg13.max, cfg13.defaultValue);
            JLabel hueLabel = new JLabel(fishPanel.formatSliderValue(13, cfg13.defaultValue));
            hueSlider.addChangeListener(evt -> {
                fishPanel.hue = hueSlider.getValue();
                hueLabel.setText(fishPanel.formatSliderValue(13, hueSlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(hueLabel, hueSlider));

// 14: Opacitet
            SliderConfig cfg14 = SLIDER_CONFIGS[14];
            JSlider opacitySlider = new JSlider(cfg14.min, cfg14.max, cfg14.defaultValue);
            JLabel opacityLabel = new JLabel(fishPanel.formatSliderValue(14, cfg14.defaultValue));
            opacitySlider.addChangeListener(evt -> {
                fishPanel.opacity = opacitySlider.getValue() / 100.0f;
                opacityLabel.setText(fishPanel.formatSliderValue(14, opacitySlider.getValue()));
                fishPanel.repaint();
            });
            controls.add(createSliderPanel(opacityLabel, opacitySlider));

// 11: Fiskhastighet
            SliderConfig cfg11 = SLIDER_CONFIGS[11];
            JSlider speedSlider = new JSlider(cfg11.min, cfg11.max, cfg11.defaultValue);
            JLabel speedLabel = new JLabel(fishPanel.formatSliderValue(11, cfg11.defaultValue));
            speedSlider.addChangeListener(evt -> {
                fishPanel.fish.fishSpeed = speedSlider.getValue();
                speedLabel.setText(fishPanel.formatSliderValue(11, speedSlider.getValue()));
            });
            controls.add(createSliderPanel(speedLabel, speedSlider));

// 12: Målhastighet
            SliderConfig cfg12 = SLIDER_CONFIGS[12];
            JSlider targetSpeedSlider = new JSlider(cfg12.min, cfg12.max, cfg12.defaultValue);
            JLabel targetSpeedLabel = new JLabel(fishPanel.formatSliderValue(12, cfg12.defaultValue));
            targetSpeedSlider.addChangeListener(evt -> {
                fishPanel.target.speed = targetSpeedSlider.getValue();
                double currentSpeed = Math.sqrt(fishPanel.target.vx * fishPanel.target.vx +
                        fishPanel.target.vy * fishPanel.target.vy);
                if (currentSpeed > 0) {
                    double angle = Math.atan2(fishPanel.target.vy, fishPanel.target.vx);
                    fishPanel.target.vx = Math.cos(angle) * fishPanel.target.speed;
                    fishPanel.target.vy = Math.sin(angle) * fishPanel.target.speed;
                }
                targetSpeedLabel.setText(fishPanel.formatSliderValue(12, targetSpeedSlider.getValue()));
            });
            controls.add(createSliderPanel(targetSpeedLabel, targetSpeedSlider));
            sliders.add(opacitySlider);
            labels.add(opacityLabel);

            // Knappar
            controls.add(Box.createVerticalStrut(5));

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
            buttonPanel.setOpaque(false);
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

            JButton resetButton = new JButton("Återställ");
            resetButton.setFont(new Font("Arial", Font.PLAIN, 11));
            resetButton.setFocusable(false);
            resetButton.addActionListener(evt -> {
                for (int i = 0; i < sliders.size(); i++) {
                    sliders.get(i).setValue(SLIDER_CONFIGS[i].defaultValue);
                }
            });

            JButton randomButton = new JButton("Slumpa");
            randomButton.setFont(new Font("Arial", Font.PLAIN, 11));
            randomButton.setFocusable(false);
            randomButton.addActionListener(evt -> {
                for (int i = 0; i < sliders.size(); i++) {
                    SliderConfig cfg = SLIDER_CONFIGS[i];
                    int randomValue = cfg.min + (int)(Math.random() * (cfg.max - cfg.min + 1));
                    sliders.get(i).setValue(randomValue);
                }
            });

            buttonPanel.add(resetButton);
            buttonPanel.add(randomButton);
            controls.add(buttonPanel);

            JCheckBox showTargetBox = new JCheckBox("Visa mål");
            showTargetBox.setSelected(true);
            showTargetBox.setFocusable(false);
            showTargetBox.setForeground(new Color(200, 200, 200));
            showTargetBox.setBackground(new Color(40, 40, 40));
            showTargetBox.setFont(new Font("Arial", Font.PLAIN, 11));
            showTargetBox.addActionListener(evt -> fishPanel.showTarget = showTargetBox.isSelected());
            controls.add(showTargetBox);


            JScrollPane scrollPane = new JScrollPane(controls);
            scrollPane.setBorder(null);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setPreferredSize(new Dimension(280, 600));

            frame.add(scrollPane, BorderLayout.EAST);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createSliderPanel(JLabel label, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        label.setForeground(new Color(200, 200, 200));
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setPreferredSize(new Dimension(140, 20));

        slider.setOpaque(false);
        slider.setPreferredSize(new Dimension(120, 20));
        slider.setFocusable(false);

        panel.add(label, BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);

        return panel;
    }
}