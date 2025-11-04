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
    private List<Integer> baseSizes = List.of(26, 32, 36, 34, 28, 22, 17, 12, 7, 4);

    private final Timer timer;
    private long lastTime = System.nanoTime();

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

        drawTarget(g2d);
        drawFish(g2d);
    }

    private void drawTarget(Graphics2D g2d) {
        g2d.setColor(new Color(255, 100, 100, 150));
        g2d.fill(new Ellipse2D.Double(target.position.x - 8, target.position.y - 8, 16, 16));
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new Ellipse2D.Double(target.position.x - 8, target.position.y - 8, 16, 16));

        double arrowAngle = Math.atan2(target.vy, target.vx);
        int arrowLength = 20;
        int arrowX = (int)(target.position.x + Math.cos(arrowAngle) * arrowLength);
        int arrowY = (int)(target.position.y + Math.sin(arrowAngle) * arrowLength);

        g2d.setColor(new Color(255, 150, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine((int)target.position.x, (int)target.position.y, arrowX, arrowY);
    }

    private void drawFish(Graphics2D g2d) {
        if (fish.segments.isEmpty()) return;

        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();

        double headAngle = fish.getHeadAngle();
        double headHalfWidth = fish.segments.get(0).size / 2.0;

        leftPoints.add(new Point(
                (int)(fish.head.x + headHalfWidth * Math.cos(headAngle + Math.PI/2)),
                (int)(fish.head.y + headHalfWidth * Math.sin(headAngle + Math.PI/2))
        ));
        rightPoints.add(new Point(
                (int)(fish.head.x + headHalfWidth * Math.cos(headAngle - Math.PI/2)),
                (int)(fish.head.y + headHalfWidth * Math.sin(headAngle - Math.PI/2))
        ));

        for (int i = 0; i < fish.segments.size(); i++) {
            FishSegment seg = fish.segments.get(i);
            double angle = seg.angle;
            double halfWidth = seg.size / 2.0;

            leftPoints.add(new Point(
                    (int)(seg.position.x + halfWidth * Math.cos(angle + Math.PI/2)),
                    (int)(seg.position.y + halfWidth * Math.sin(angle + Math.PI/2))
            ));
            rightPoints.add(new Point(
                    (int)(seg.position.x + halfWidth * Math.cos(angle - Math.PI/2)),
                    (int)(seg.position.y + halfWidth * Math.sin(angle - Math.PI/2))
            ));
        }

        GeneralPath body = new GeneralPath();
        body.moveTo(leftPoints.get(0).x, leftPoints.get(0).y);
        for (int i = 1; i < leftPoints.size(); i++) {
            body.lineTo(leftPoints.get(i).x, leftPoints.get(i).y);
        }
        for (int i = rightPoints.size() - 1; i >= 0; i--) {
            body.lineTo(rightPoints.get(i).x, rightPoints.get(i).y);
        }
        body.closePath();

        GradientPaint gradient = new GradientPaint(
                (float)fish.head.x, (float)fish.head.y, new Color(0, 180, 255),
                (float)fish.segments.get(fish.segments.size()-1).position.x,
                (float)fish.segments.get(fish.segments.size()-1).position.y, new Color(0, 100, 200)
        );
        g2d.setPaint(gradient);
        g2d.fill(body);

        g2d.setColor(new Color(0, 150, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(body);

        drawEyes(g2d, headAngle, headHalfWidth);
        drawTail(g2d);
    }

    private void drawEyes(Graphics2D g2d, double headAngle, double headHalfWidth) {
        double eyeOffset = headHalfWidth * 0.6;
        double eyeSize = headHalfWidth * 0.3;

        for (int side = -1; side <= 1; side += 2) {
            double ex = fish.head.x + eyeOffset * Math.cos(headAngle + side * Math.PI/4);
            double ey = fish.head.y + eyeOffset * Math.sin(headAngle + side * Math.PI/4);

            g2d.setColor(Color.WHITE);
            g2d.fill(new Ellipse2D.Double(ex - eyeSize, ey - eyeSize, eyeSize * 2, eyeSize * 2));

            g2d.setColor(Color.BLACK);
            double pupilOffset = eyeSize * 0.3;
            double pupilX = ex + pupilOffset * Math.cos(headAngle);
            double pupilY = ey + pupilOffset * Math.sin(headAngle);
            g2d.fill(new Ellipse2D.Double(pupilX - eyeSize * 0.4, pupilY - eyeSize * 0.4, eyeSize * 0.8, eyeSize * 0.8));
        }
    }

    private void drawTail(Graphics2D g2d) {
        if (fish.segments.size() < 2) return;

        FishSegment lastSegment = fish.segments.get(fish.segments.size() - 1);
        double tailAngle = lastSegment.angle;
        double tailSize = lastSegment.size * 1.5;

        GeneralPath tail = new GeneralPath();
        tail.moveTo(lastSegment.position.x, lastSegment.position.y);

        Point2D.Double tip = new Point2D.Double(
                lastSegment.position.x + Math.cos(tailAngle) * tailSize,
                lastSegment.position.y + Math.sin(tailAngle) * tailSize
        );

        Point2D.Double left = new Point2D.Double(
                lastSegment.position.x + Math.cos(tailAngle + Math.PI/2) * tailSize * 0.5,
                lastSegment.position.y + Math.sin(tailAngle + Math.PI/2) * tailSize * 0.5
        );

        Point2D.Double right = new Point2D.Double(
                lastSegment.position.x + Math.cos(tailAngle - Math.PI/2) * tailSize * 0.5,
                lastSegment.position.y + Math.sin(tailAngle - Math.PI/2) * tailSize * 0.5
        );

        tail.lineTo(left.x, left.y);
        tail.lineTo(tip.x, tip.y);
        tail.lineTo(right.x, right.y);
        tail.closePath();

        g2d.setColor(new Color(0, 120, 180));
        g2d.fill(tail);
        g2d.setColor(new Color(0, 100, 160));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(tail);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Autonom fisk med förbättrad simulering");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            SpineSelectiveLegs fishPanel = new SpineSelectiveLegs();
            frame.add(fishPanel, BorderLayout.CENTER);

            JPanel controls = new JPanel();
            controls.setLayout(new GridLayout(10, 2, 5, 5));
            controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            controls.setBackground(Color.DARK_GRAY);

            // Antal segment
            JSlider segmentSlider = new JSlider(3, 20, fishPanel.fish.segments.size());
            JLabel segmentLabel = new JLabel("Antal segment: " + fishPanel.fish.segments.size());
            segmentLabel.setForeground(Color.WHITE);
            segmentSlider.addChangeListener(e -> {
                fishPanel.fish.updateSegments(segmentSlider.getValue(), fishPanel.baseSizes);
                segmentLabel.setText("Antal segment: " + fishPanel.fish.segments.size());
                fishPanel.repaint();
            });
            controls.add(segmentLabel);
            controls.add(segmentSlider);

            // Kroppsstorlek
            JSlider sizeSlider = new JSlider(50, 200, 100);
            JLabel sizeLabel = new JLabel("Kroppsstorlek: " + sizeSlider.getValue() + "%");
            sizeLabel.setForeground(Color.WHITE);
            sizeSlider.addChangeListener(e -> {
                double scale = sizeSlider.getValue() / 100.0;
                fishPanel.fish.scaleSegments(scale, fishPanel.baseSizes);
                sizeLabel.setText("Kroppsstorlek: " + sizeSlider.getValue() + "%");
                fishPanel.repaint();
            });
            controls.add(sizeLabel);
            controls.add(sizeSlider);

            // Segmentavstånd
            JSlider distanceSlider = new JSlider(10, 50, (int)fishPanel.fish.segmentDistance);
            JLabel distanceLabel = new JLabel("Segmentavstånd: " + distanceSlider.getValue() + " px");
            distanceLabel.setForeground(Color.WHITE);
            distanceSlider.addChangeListener(e -> {
                fishPanel.fish.updateSegmentDistance(distanceSlider.getValue());
                distanceLabel.setText("Segmentavstånd: " + distanceSlider.getValue() + " px");
                fishPanel.repaint();
            });
            controls.add(distanceLabel);
            controls.add(distanceSlider);

            // Tjockaste delen
            JSlider peakSlider = new JSlider(0, 100, (int)(fishPanel.fish.peakPosition * 100));
            JLabel peakLabel = new JLabel("Tjockaste delen: " + peakSlider.getValue() + "%");
            peakLabel.setForeground(Color.WHITE);
            peakSlider.addChangeListener(e -> {
                double peakPos = peakSlider.getValue() / 100.0;
                fishPanel.fish.updatePeakPosition(peakPos);
                peakLabel.setText("Tjockaste delen: " + peakSlider.getValue() + "%");
                fishPanel.repaint();
            });
            controls.add(peakLabel);
            controls.add(peakSlider);

            // Styvhet
            JSlider rigiditySlider = new JSlider(0, 100, (int)(fishPanel.fish.rigidity * 100));
            JLabel rigidityLabel = new JLabel("Styvhet: " + rigiditySlider.getValue() + "%");
            rigidityLabel.setForeground(Color.WHITE);
            rigiditySlider.addChangeListener(e -> {
                fishPanel.fish.rigidity = rigiditySlider.getValue() / 100.0;
                rigidityLabel.setText("Styvhet: " + rigiditySlider.getValue() + "%");
            });
            controls.add(rigidityLabel);
            controls.add(rigiditySlider);

            // Sväjghastighet
            JSlider turnSlider = new JSlider(10, 2880, (int)Math.toDegrees(fishPanel.fish.fishTurnSpeed));
            JLabel turnLabel = new JLabel("Sväjghastighet: " + turnSlider.getValue() + "°/s");
            turnLabel.setForeground(Color.WHITE);
            turnSlider.addChangeListener(e -> {
                fishPanel.fish.fishTurnSpeed = Math.toRadians(turnSlider.getValue());
                turnLabel.setText("Sväjghastighet: " + turnSlider.getValue() + "°/s");
            });
            controls.add(turnLabel);
            controls.add(turnSlider);

            // Fiskhastighet
            JSlider speedSlider = new JSlider(20, 200, (int)fishPanel.fish.fishSpeed);
            JLabel speedLabel = new JLabel("Fiskhastighet: " + speedSlider.getValue() + " px/s");
            speedLabel.setForeground(Color.WHITE);
            speedSlider.addChangeListener(e -> {
                fishPanel.fish.fishSpeed = speedSlider.getValue();
                speedLabel.setText("Fiskhastighet: " + speedSlider.getValue() + " px/s");
            });
            controls.add(speedLabel);
            controls.add(speedSlider);

            // Målhastighet
            JSlider targetSpeedSlider = new JSlider(20, 300, (int)fishPanel.target.speed);
            JLabel targetSpeedLabel = new JLabel("Målhastighet: " + targetSpeedSlider.getValue() + " px/s");
            targetSpeedLabel.setForeground(Color.WHITE);
            targetSpeedSlider.addChangeListener(e -> {
                fishPanel.target.speed = targetSpeedSlider.getValue();
                double angle = Math.atan2(fishPanel.target.vy, fishPanel.target.vx);
                fishPanel.target.vx = Math.cos(angle) * fishPanel.target.speed;
                fishPanel.target.vy = Math.sin(angle) * fishPanel.target.speed;
                targetSpeedLabel.setText("Målhastighet: " + targetSpeedSlider.getValue() + " px/s");
            });
            controls.add(targetSpeedLabel);
            controls.add(targetSpeedSlider);

            // Reset-knapp
            JButton resetButton = new JButton("Återställ");
            resetButton.addActionListener(e -> {
                segmentSlider.setValue(10);
                sizeSlider.setValue(100);
                distanceSlider.setValue(25);
                peakSlider.setValue(30);
                rigiditySlider.setValue(80);
                turnSlider.setValue(360);
                speedSlider.setValue(80);
                targetSpeedSlider.setValue(100);
            });
            controls.add(new JLabel());
            controls.add(resetButton);

            frame.add(controls, BorderLayout.SOUTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}