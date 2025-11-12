package org.example;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Fish {
    public List<FishSegment> segments = new ArrayList<>();
    private List<Integer> baseSizes;

    public double fishSpeed = 80;
    public double fishTurnSpeed = Math.toRadians(360);
    public double rigidity = 0.8;
    public double flexibility = 0.2;
    public double selfAvoidance = 0.5;
    public double maxAngleDiff = Math.toRadians(45);  // Max vinkelskillnad mellan segment
    public double segmentDistance = 25;
    public double peakPosition = 0.5;
    public double sizeScale = 1.0;
    public double bellyScale = 1.0;
    public double taperStrength = 1.0;

    public Fish(int segmentCount, List<Integer> sizes, double startX, double startY) {
        this.baseSizes = new ArrayList<>(sizes);

        for (int i = 0; i < segmentCount; i++) {
            int size = i < sizes.size() ? sizes.get(i) : 20;
            segments.add(new FishSegment(startX - (i + 1) * segmentDistance, startY, size));
        }
        updateSegmentSizes();
    }

    // 🔹 Enklare anrop från main: uppdaterar mot mål
    public void update(Point2D.Double target, double deltaTime) {
        moveTowards(target, deltaTime);
    }

    public void moveTowards(Point2D.Double target, double deltaTime) {
        if (segments.isEmpty()) return;

        FishSegment head = segments.get(0);
        double dx = target.x - head.position.x;
        double dy = target.y - head.position.y;
        double targetAngle = Math.atan2(dy, dx);

        // --- mjuk, begränsad rotation på huvudet ---
        double angleDiff = targetAngle - head.angle;
        angleDiff = normalizeAngle(angleDiff);

        double maxTurn = fishTurnSpeed * deltaTime;
        if (angleDiff > maxTurn) angleDiff = maxTurn;
        if (angleDiff < -maxTurn) angleDiff = -maxTurn;

        head.angle += angleDiff;
        head.position.x += Math.cos(head.angle) * fishSpeed * deltaTime;
        head.position.y += Math.sin(head.angle) * fishSpeed * deltaTime;

        updateSegmentsPosition(deltaTime);
    }

    public double getHeadAngle() {
        if (segments.isEmpty()) return 0;
        return segments.get(0).angle;
    }

    private void updateSegmentsPosition(double deltaTime) {
        if (segments.isEmpty()) return;

        // Första segmentet styrs av huvudmålet
        FishSegment head = segments.get(0);
        // Head har redan uppdaterats av moveTowards, så vi börjar med resten
        for (int i = 1; i < segments.size(); i++) {
            FishSegment prev = segments.get(i - 1);
            FishSegment seg = segments.get(i);

            // Måldistans mellan segment
            double dx = prev.position.x - seg.position.x;
            double dy = prev.position.y - seg.position.y;
            double dist = Math.hypot(dx, dy);

            if (dist > 0.01) {
                double targetX = prev.position.x - (dx / dist) * segmentDistance;
                double targetY = prev.position.y - (dy / dist) * segmentDistance;

                // Flytta segment mot target med rigid/flex
                seg.position.x += (targetX - seg.position.x) * rigidity;
                seg.position.y += (targetY - seg.position.y) * rigidity;

                // Ny vinkel baserat på föregående segment
                double angle = Math.atan2(prev.position.y - seg.position.y,
                        prev.position.x - seg.position.x);
                // Begränsa vinkeln så den inte vrider sig för mycket
                double deltaAngle = angle - seg.angle;
                while (deltaAngle > Math.PI) deltaAngle -= 2 * Math.PI;
                while (deltaAngle < -Math.PI) deltaAngle += 2 * Math.PI;
                deltaAngle = Math.max(-maxAngleDiff, Math.min(maxAngleDiff, deltaAngle));

                seg.angle += deltaAngle * (1 + flexibility * (1.0 - (double)i / segments.size()));
            }
        }
    }


    private double normalizeAngle(double a) {
        while (a > Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    // 🔹 Samma kod som du hade för att ändra storlek m.m.
    public void updateSegments(int newCount, List<Integer> newBaseSizes) {
        this.baseSizes = new ArrayList<>(newBaseSizes);
        int currentCount = segments.size();

        if (newCount > currentCount) {
            for (int i = currentCount; i < newCount; i++) {
                Point2D.Double lastPos;
                double lastAngle;

                if (segments.isEmpty()) {
                    lastPos = new Point2D.Double(0, 0);
                    lastAngle = 0;
                } else {
                    FishSegment last = segments.get(segments.size() - 1);
                    lastPos = last.position;
                    lastAngle = last.angle;
                }

                int size = i < baseSizes.size() ? baseSizes.get(i) : 20;
                double newX = lastPos.x - Math.cos(lastAngle) * segmentDistance;
                double newY = lastPos.y - Math.sin(lastAngle) * segmentDistance;
                segments.add(new FishSegment(newX, newY, size));
            }
        } else if (newCount < currentCount) {
            segments.subList(newCount, currentCount).clear();
        }
        updateSegmentSizes();
    }

    public void scaleSegments(double scale, List<Integer> newBaseSizes) {
        this.baseSizes = new ArrayList<>(newBaseSizes);
        this.sizeScale = scale;
        updateSegmentSizes();
    }

    public void updateSegmentDistance(double newDistance) {
        this.segmentDistance = newDistance;
    }

    public void updatePeakPosition(double newPeakPosition) {
        this.peakPosition = Math.max(0.0, Math.min(1.0, newPeakPosition));
        updateSegmentSizes();
    }

    public void updateSegmentSizes() {
        if (segments.isEmpty() || baseSizes.isEmpty()) return;

        for (int i = 0; i < segments.size(); i++) {
            int baseSize = i < baseSizes.size() ? baseSizes.get(i) : 20;

            double progress = segments.size() > 1 ? (double) i / (segments.size() - 1) : 0;
            double peakFactor;
            if (progress <= peakPosition) {
                peakFactor = peakPosition > 0 ? progress / peakPosition : 1.0;
            } else {
                double tailProgress = (progress - peakPosition) / (1.0 - peakPosition);
                peakFactor = 1.0 - Math.pow(tailProgress, taperStrength);
            }

            peakFactor = smoothStep(peakFactor);

            double bellyFactor = 1.0 - Math.abs(progress - 0.5) * 2.0;
            bellyFactor = bellyFactor * bellyFactor;
            double bellyBonus = (bellyScale - 1.0) * bellyFactor;

            int size = (int) (baseSize * peakFactor * (1.0 + bellyBonus) * sizeScale);
            segments.get(i).size = Math.max(2, size);
        }
    }

    private double smoothStep(double x) {
        x = Math.max(0.0, Math.min(1.0, x));
        return x * x * (3 - 2 * x);
    }
}
